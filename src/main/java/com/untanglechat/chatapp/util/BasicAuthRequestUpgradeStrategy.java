package com.untanglechat.chatapp.util;

 

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.WebsocketServerSpec;

import java.util.function.Supplier;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BasicAuthRequestUpgradeStrategy implements RequestUpgradeStrategy {

    private int maxFramePayloadLength = NettyWebSocketSessionSupport.DEFAULT_FRAME_MAX_SIZE;

    // private final AuthenticationService service;

    // public BasicAuthRequestUpgradeStrategy(AuthenticationService service) {
    //     this.service = service;
    // }

    @Override
    public Mono<Void> upgrade(ServerWebExchange exchange, WebSocketHandler webSocketHandler, @Nullable String subProtocol,
            Supplier<HandshakeInfo> handshakeInfoFactory) {
        
        ServerHttpResponse response = exchange.getResponse();
        HttpServerResponse reactorResponse = getNativeResponse(response);
        HandshakeInfo handshakeInfo = handshakeInfoFactory.get();
        NettyDataBufferFactory bufferFactory = (NettyDataBufferFactory) response.bufferFactory();
    
        // var authResult = validateAuth(handshakeInfo);
        // if (authResult == unauthorised) return Mono.just(reactorResponse.status(rejectedStatus))
        //                                            .flatMap(HttpServerResponse::send);

        // if(NOT_AUTHENTICATED) return Mono.just(reactorResponse.status(HttpResponseStatus.FORBIDDEN)).flatMap(HttpServerResponse::send);

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
}