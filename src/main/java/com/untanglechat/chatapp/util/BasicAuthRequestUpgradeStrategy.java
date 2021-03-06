package com.untanglechat.chatapp.util;

 

import java.util.function.Supplier;

import com.untanglechat.chatapp.models.AuthenticationStatusEnum;
import com.untanglechat.chatapp.security.JwtTokenProvider;

import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.adapter.NettyWebSocketSessionSupport;
import org.springframework.web.reactive.socket.adapter.ReactorNettyWebSocketSession;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.server.ServerWebExchange;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.WebsocketServerSpec;

@Slf4j
@Component
@RequiredArgsConstructor
public class BasicAuthRequestUpgradeStrategy implements RequestUpgradeStrategy {

    private int maxFramePayloadLength = NettyWebSocketSessionSupport.DEFAULT_FRAME_MAX_SIZE;

    private final JwtTokenProvider jwtTokenProvider;

    private final UtilityService utilityService;

    @Override
    public Mono<Void> upgrade(ServerWebExchange exchange, WebSocketHandler webSocketHandler, @Nullable String subProtocol,
            Supplier<HandshakeInfo> handshakeInfoFactory) {
        
        ServerHttpResponse response = exchange.getResponse();
        HttpServerResponse reactorResponse = getNativeResponse(response);
        HandshakeInfo handshakeInfo = handshakeInfoFactory.get();
        NettyDataBufferFactory bufferFactory = (NettyDataBufferFactory) response.bufferFactory();
    
        // to be changed later on
        // System.err.println(handshakeInfo);
        // System.err.println(handshakeInfo.getAttributes());
        
        var authResult = validateAuth(handshakeInfo);
        if (authResult == AuthenticationStatusEnum.UNAUTHORIZED) return Mono.just(reactorResponse.status(HttpResponseStatus.FORBIDDEN))
                                                   .flatMap(HttpServerResponse::send);

        if(authResult == AuthenticationStatusEnum.UNAUTHENTICATED) return Mono.just(reactorResponse.status(HttpResponseStatus.UNAUTHORIZED)).flatMap(HttpServerResponse::send);

        final WebsocketServerSpec websocketServerSpec = WebsocketServerSpec.builder()
                .maxFramePayloadLength(this.maxFramePayloadLength)    
                // .protocols(subProtocol)
                .build();

        return reactorResponse.sendWebsocket( (in, out) -> {

                ReactorNettyWebSocketSession session = new ReactorNettyWebSocketSession(in, out, handshakeInfo, bufferFactory, this.maxFramePayloadLength);

                return webSocketHandler.handle(session);
        }, websocketServerSpec);
    }

    private static HttpServerResponse getNativeResponse(ServerHttpResponse response) {
        if (response instanceof AbstractServerHttpResponse) {
            return ((AbstractServerHttpResponse) response).getNativeResponse();
        } else if (response instanceof ServerHttpResponseDecorator) {
            return getNativeResponse(((ServerHttpResponseDecorator) response).getDelegate());
        } else {
            throw new IllegalArgumentException("Couldn't find native response in " + response.getClass()
                                                                                             .getName());
        }
    }

    private AuthenticationStatusEnum validateAuth(final HandshakeInfo handshakeInfo) {

        String token = utilityService.getTokenFromHandshakeInfo(handshakeInfo);

        boolean isAuthenticated = false;
        try {
            isAuthenticated = jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
           log.error(e.getMessage());
        }

        return isAuthenticated? AuthenticationStatusEnum.AUTHENTICATED: AuthenticationStatusEnum.UNAUTHENTICATED;
    }
}