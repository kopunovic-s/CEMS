package com._project._project.Project.ProjectChat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSocket extends TextWebSocketHandler {

    private static final Map<Long, Map<WebSocketSession, String>> projectSessions = new ConcurrentHashMap<>();

    private final MessageRepository msgRepo;

    @Autowired
    public ChatSocket(MessageRepository msgRepo) {
        this.msgRepo = msgRepo;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        // Extract projectId and username from URI
        String[] pathParts = session.getUri().getPath().split("/");
        String projectId = pathParts[pathParts.length - 2];
        String username = pathParts[pathParts.length - 1];
        
        projectSessions.computeIfAbsent(Long.parseLong(projectId), k -> new ConcurrentHashMap<>());
        projectSessions.get(Long.parseLong(projectId)).put(session, username);

        // First send chat history
        List<Message> history = msgRepo.findByProjectId(Long.parseLong(projectId));
        for (Message msg : history) {
            session.sendMessage(new TextMessage(msg.getUserName() + ": " + msg.getContent()));
        }
        
        // Then send join message after history
        broadcast(Long.parseLong(projectId), "User " + username + " has joined the chat.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Map<String, String> params = getPathParams(session);
        Long projectId = Long.parseLong(params.get("projectId"));
        String username = projectSessions.get(projectId).get(session);
        String message = textMessage.getPayload();

        if (message.startsWith("@")) {
            String destUsername = message.split(" ")[0].substring(1);
            sendPrivateMessage(projectId, username, destUsername, message);
        } else {
            broadcast(projectId, username + ": " + message);
        }

        msgRepo.save(new Message(projectId, username, message));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        for (Map.Entry<Long, Map<WebSocketSession, String>> entry : projectSessions.entrySet()) {
            if (entry.getValue().containsKey(session)) {
                String username = entry.getValue().remove(session);
                broadcast(entry.getKey(), username + " has left the chat.");

                if (entry.getValue().isEmpty()) {
                    projectSessions.remove(entry.getKey());
                }
                break;
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        session.close(CloseStatus.SERVER_ERROR);
    }

    private void sendMessage(WebSocketSession session, String message) throws IOException {
        session.sendMessage(new TextMessage(message));
    }

    private void broadcast(Long projectId, String message) {
        Map<WebSocketSession, String> sessions = projectSessions.get(projectId);
        if (sessions != null) {
            for (WebSocketSession session : sessions.keySet()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    // Log error
                }
            }
        }
    }

    private void sendPrivateMessage(Long projectId, String sender, String recipient, String message) {
        Map<WebSocketSession, String> sessions = projectSessions.get(projectId);
        if (sessions == null) return;

        sessions.forEach((s, uname) -> {
            if (uname.equals(recipient) || uname.equals(sender)) {
                try {
                    s.sendMessage(new TextMessage("[DM] " + sender + ": " + message));
                } catch (IOException e) {
                    // Log error
                }
            }
        });
    }

    private String getChatHistory(Long projectId) {
        List<Message> messages = msgRepo.findByProjectId(projectId);
        StringBuilder sb = new StringBuilder();
        for (Message msg : messages) {
            sb.append(msg.getUserName()).append(": ").append(msg.getContent()).append("\n");
        }
        return sb.toString();
    }

    // Helper to parse projectId and username from URI path
    private Map<String, String> getPathParams(WebSocketSession session) {
        String path = session.getUri().getPath(); // e.g., /project-chat/123/bob
        String[] segments = path.split("/");
        return Map.of(
                "projectId", segments[2],
                "username", segments[3]
        );
    }
}
