package com._project._project.Project.ProjectChat;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSocket
public class ChatSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatSocket chatSocket;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatSocket, "/project-chat/{projectId}/{username}")
                .setAllowedOrigins("*");
    }
}
