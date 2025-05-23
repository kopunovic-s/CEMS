package com.example.companyloginapp;

import android.util.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class LiveProjectWebSocketManager {
    private static LiveProjectWebSocketManager instance;
    private WebSocketClient webSocketClient;
    private ProjectWebSocketListener webSocketListener;

    private LiveProjectWebSocketManager() {}

    public static synchronized LiveProjectWebSocketManager getInstance() {
        if (instance == null) {
            instance = new LiveProjectWebSocketManager();
        }
        return instance;
    }

    public void setWebSocketListener(ProjectWebSocketListener listener) {
        this.webSocketListener = listener;
    }

    public void removeWebSocketListener() {
        this.webSocketListener = null;
    }

    public void connect(String fullWebSocketUrl) {
        try {
            URI uri = new URI(fullWebSocketUrl);
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d("WebSocket", "Connected to: " + uri);
                    if (webSocketListener != null) {
                        webSocketListener.onOpen(handshakedata);
                    }
                }

                @Override
                public void onMessage(String message) {
                    Log.d("WebSocket", "Message received: " + message);
                    if (webSocketListener != null) {
                        webSocketListener.onMessage(message);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d("WebSocket", "Closed: " + reason);
                    if (webSocketListener != null) {
                        webSocketListener.onClose(code, reason, remote);
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e("WebSocket", "Error: " + ex.getMessage());
                    if (webSocketListener != null) {
                        webSocketListener.onError(ex);
                    }
                }
            };

            webSocketClient.connect();
        } catch (Exception e) {
            Log.e("WebSocket", "Connection error: " + e.getMessage());
        }
    }

    public void sendMessage(int projectId, String senderName, String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            String fullMessage = senderName + ": " + message;
            webSocketClient.send(fullMessage);
        } else {
            Log.w("WebSocket", "WebSocket is not connected. Message not sent.");
        }
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
}
