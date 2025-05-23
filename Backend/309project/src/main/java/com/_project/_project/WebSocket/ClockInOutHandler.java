package com._project._project.WebSocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClockInOutHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClockInOutHandler.class);
    private static final Map<WebSocketSession, String> sessionCompanyMap = new ConcurrentHashMap<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, MM-dd-yyyy");
    
    private final ClockEventRepository clockEventRepository;

    @Autowired
    public ClockInOutHandler(ClockEventRepository clockEventRepository) {
        this.clockEventRepository = clockEventRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String companyId = extractCompanyId(session);
        String userId = extractUserId(session);
        logger.info("New WebSocket connection: Company ID = " + companyId + ", User ID = " + userId);
        sessionCompanyMap.put(session, companyId);

        // Send historical messages
        List<ClockInOutMessage> history = clockEventRepository.findAll();
        for (ClockInOutMessage event : history) {
            String historicalMessage = formatClockEvent(event);
            session.sendMessage(new TextMessage(historicalMessage));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String[] parts = message.getPayload().split(":");
        String employeeName = parts[0];
        // Convert "CLOCK IN" to "CLOCK_IN" format
        String action = parts[1].trim().toUpperCase().replace(" ", "_");
        
        // Debug log
        logger.info("Processing action: " + action);
        
        // Save to database
        ClockInOutMessage clockEvent = new ClockInOutMessage(employeeName, action);
        clockEventRepository.save(clockEvent);
        
        // Format and broadcast the message
        String response = formatClockEvent(clockEvent);
        String companyId = sessionCompanyMap.get(session);
        broadcast(response, companyId);
    }

    private String formatClockEvent(ClockInOutMessage event) {
        // Debug log
        logger.info("Event type in formatClockEvent: " + event.getEventType());
        
        String action = "CLOCK_IN".equals(event.getEventType()) ? "in" : "out";
        return event.getEmployeeName() + " clocked " + action + " at " + 
               event.getTimestamp().format(formatter);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        sessionCompanyMap.remove(session);
    }

    private void broadcast(String message, String companyId) {
        sessionCompanyMap.forEach((session, cId) -> {
            if (session.isOpen() && cId.equals(companyId)) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    logger.error("Error broadcasting message:", e);
                }
            }
        });
    }

    private String extractCompanyId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        return parts[parts.length - 2];
    }

    private String extractUserId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
} 