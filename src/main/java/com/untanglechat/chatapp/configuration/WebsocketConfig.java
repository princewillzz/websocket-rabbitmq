package com.untanglechat.chatapp.configuration;

import java.util.Map;

import com.untanglechat.chatapp.services.ChatWebsocketHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

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
	public WebSocketHandlerAdapter handlerAdapter() {
		return new WebSocketHandlerAdapter();
	}

    
}
