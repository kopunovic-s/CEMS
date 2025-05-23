package com._project._project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com._project._project.WebSocket.ClockEventRepository;
import com._project._project.WebSocket.ClockInOutHandler;
import com._project._project.WebSocket.ClockInOutMessage;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class WebSocketTests {

    @Mock
    private ClockEventRepository clockEventRepository;

    @Mock
    private WebSocketSession session;

    private ClockInOutHandler handler;
    private ClockInOutMessage testMessage;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        handler = new ClockInOutHandler(clockEventRepository);
        
        // Setup test message
        testMessage = new ClockInOutMessage("John Doe", "CLOCK_IN");
        testMessage.setId(1L);
        testMessage.setTimestamp(LocalDateTime.now());

        // Mock session URI
        when(session.getUri()).thenReturn(new URI("/clockEvents/123/456"));
        when(session.isOpen()).thenReturn(true);
    }
    
    @Test
    void testAfterConnectionEstablished() throws Exception {
        // Setup
        List<ClockInOutMessage> history = Arrays.asList(testMessage);
        when(clockEventRepository.findAll()).thenReturn(history);

        // Execute
        handler.afterConnectionEstablished(session);

        // Verify
        verify(clockEventRepository).findAll();
        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void testClockEventRepository() {
        // Setup
        LocalDateTime testTime = LocalDateTime.now().minusHours(1);
        when(clockEventRepository.findByTimestampAfter(testTime))
            .thenReturn(Arrays.asList(testMessage));
        when(clockEventRepository.findByEmployeeName("John Doe"))
            .thenReturn(Arrays.asList(testMessage));
        when(clockEventRepository.findAllByOrderByTimestampDesc())
            .thenReturn(Arrays.asList(testMessage));

        // Execute & Verify
        List<ClockInOutMessage> timeResults = clockEventRepository.findByTimestampAfter(testTime);
        List<ClockInOutMessage> nameResults = clockEventRepository.findByEmployeeName("John Doe");
        List<ClockInOutMessage> orderedResults = clockEventRepository.findAllByOrderByTimestampDesc();

        assertEquals(1, timeResults.size());
        assertEquals(1, nameResults.size());
        assertEquals(1, orderedResults.size());
    }

    @Test
    void testClockInOutMessage() {
        // Test constructor and getters/setters
        ClockInOutMessage message = new ClockInOutMessage("Jane Doe", "CLOCK_OUT");
        
        assertEquals("Jane Doe", message.getEmployeeName());
        assertEquals("CLOCK_OUT", message.getEventType());
        assertNotNull(message.getTimestamp());

        // Test setters
        message.setId(2L);
        message.setEmployeeName("John Smith");
        message.setEventType("CLOCK_IN");
        LocalDateTime newTime = LocalDateTime.now();
        message.setTimestamp(newTime);

        assertEquals(2L, message.getId());
        assertEquals("John Smith", message.getEmployeeName());
        assertEquals("CLOCK_IN", message.getEventType());
        assertEquals(newTime, message.getTimestamp());
    }

    @Test
    void testAfterConnectionClosed() throws Exception {
        // Execute
        handler.afterConnectionClosed(session, null);

        // Verify no more interactions with repository
        verifyNoMoreInteractions(clockEventRepository);
    }
} 