package com.untanglechat.chatapp.configuration;

import java.util.Map;

import com.untanglechat.chatapp.services.ChatWebsocketHandler;
import com.untanglechat.chatapp.util.BasicAuthRequestUpgradeStrategy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

@Configuration
public class WebsocketConfig {

    @Bean
	public HandlerMapping handlerMapping(ChatWebsocketHandler myWebSocketHandler) {
		// Map<String, WebSocketHandler> map = new HashMap<>();
		// map.put("/websocket", new MyWebSocketHandler());
		int order = -1; // before annotated controllers

		return new SimpleUrlHandlerMapping(Map.of("/websocket", myWebSocketHandler), order);
	}

	@Bean
	public WebSocketHandlerAdapter handlerAdapter(WebSocketService webSocketService) {
		return new WebSocketHandlerAdapter(webSocketService);
	}

    @Bean
    public WebSocketService webSocketService(BasicAuthRequestUpgradeStrategy basicAuthRequestUpgradeStrategy) {
        return new HandshakeWebSocketService(basicAuthRequestUpgradeStrategy);
    }
}
