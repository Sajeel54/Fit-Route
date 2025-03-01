package com.fyp.fitRoute.inventory.Config;

import com.fyp.fitRoute.posts.Utilities.postsWebsocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer {

    private final postsWebsocketHandler websocketHandler;

    // Inject the handler bean
    public WebsocketConfig(postsWebsocketHandler websocketHandler) {
        this.websocketHandler = websocketHandler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(websocketHandler, "/newsFeed");
    }
}