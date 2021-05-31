package com.untanglechat.chatapp.util;

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

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

@Slf4j
@Component
public class BasicAuthRequestUpgradeStrategy implements RequestUpgradeStrategy {

    private int maxFramePayloadLength = NettyWebSocketSessionSupport.DEFAULT_FRAME_MAX_SIZE;

    // private final AuthenticationService service;

    // public BasicAuthRequestUpgradeStrategy(AuthenticationService service) {
    //     this.service = service;
    // }

    @Override
    public Mono<Void> upgrade(ServerWebExchange exchange, //
                              WebSocketHandler handler, //
                              @Nullable String subProtocol, //
                              Supplier<HandshakeInfo> handshakeInfoFactory) {

        ServerHttpResponse response = exchange.getResponse();
        
        HttpServerResponse reactorResponse = getNativeResponse(response);
        HandshakeInfo handshakeInfo = handshakeInfoFactory.get();
        NettyDataBufferFactory bufferFactory = (NettyDataBufferFactory) response.bufferFactory();

        String originHeader = handshakeInfo.getHeaders()
                                           .getOrigin();// you will get ws://user:pass@localhost:8080

        return Mono.just(null);
        // return service.authenticate(originHeader)//returns Mono<Boolean>
        //               .filter(Boolean::booleanValue)// filter the result
        //               .doOnNext(a -> log.info("AUTHORIZED"))
        //               .flatMap(a -> reactorResponse.sendWebsocket(subProtocol, this.maxFramePayloadLength, (in, out) -> {

        //                   ReactorNettyWebSocketSession session = //
        //                           new ReactorNettyWebSocketSession(in, out, handshakeInfo, bufferFactory, this.maxFramePayloadLength);

        //                   return handler.handle(session);
        //               }))
        //               .switchIfEmpty(Mono.just("UNATHORIZED")
        //                                  .doOnNext(log::info)
        //                                  .then());

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