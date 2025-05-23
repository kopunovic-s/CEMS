package com.example.companyloginapp;

import org.java_websocket.handshake.ServerHandshake;

public interface ProjectWebSocketListener {

    // Called when the WebSocket connection opens
    void onOpen(ServerHandshake handshakedata);

    // Called when a new raw message is received (for logging/debugging or fallback)
    void onMessage(String rawMessage);

    // Optionally, you can use this if you're parsing ChatMessage objects
    default void onChatMessage(ChatMessage chatMessage) {
        // Optional to override
    }

    // Called when the WebSocket is closed
    void onClose(int code, String reason, boolean remote);

    // Called when there is an error
    void onError(Exception ex);
}
