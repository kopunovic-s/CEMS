package com._project._project;

import com._project._project.TimeCard.*;
import com._project._project.User.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TimeCardTests {

    @Mock
    private TimeCardRepository timeCardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TimeCardController timeCardController;

    private User testUser;
    private TimeCard testTimeCard;
    private LocalDateTime testDateTime;

    @BeforeAll
    void setupTestData() {
        testUser = new User("John", "Doe", "john@test.com", "password", UserRole.EMPLOYEE);
        testUser.setId(1L);

        testDateTime = LocalDateTime.now();
        testTimeCard = new TimeCard(testUser, testDateTime);
        testTimeCard.setId(1L);
        testTimeCard.setUserNumber(testUser.getId());
        testTimeCard.setWeekNumber(testDateTime.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()));
        testTimeCard.setYear(testDateTime.getYear());
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllTimeCards() {
        // Setup
        List<TimeCard> timeCards = Arrays.asList(testTimeCard);
        when(timeCardRepository.findAll()).thenReturn(timeCards);

        // Execute
        List<TimeCard> result = timeCardController.getAllTimeCards();

        // Verify
        assertEquals(1, result.size());
        assertEquals(testTimeCard, result.get(0));
        verify(timeCardRepository).findAll();
    }

    @Test
    void testGetTimeCardById() {
        // Setup
        List<TimeCard> timeCards = Arrays.asList(testTimeCard);
        when(timeCardRepository.findAllByUserNumber(testUser.getId())).thenReturn(timeCards);

        // Execute
        List<TimeCard> result = timeCardController.getTimeCardById(testUser.getId());

        // Verify
        assertEquals(1, result.size());
        assertEquals(testTimeCard, result.get(0));
        verify(timeCardRepository).findAllByUserNumber(testUser.getId());
    }

    @Test
    void testClockIn_Success() {
        // Setup
        when(userRepository.findById(testUser.getId())).thenReturn(testUser);
        when(timeCardRepository.findByUserAndClockOutIsNull(testUser)).thenReturn(null);
        when(timeCardRepository.save(any(TimeCard.class))).thenReturn(testTimeCard);

        // Execute
        String result = timeCardController.clockIn(testUser.getId());

        // Verify
        assertEquals("success", result);
        verify(timeCardRepository).save(any(TimeCard.class));
    }

    @Test
    void testClockIn_UserNotFound() {
        // Setup
        when(userRepository.findById(999L)).thenReturn(null);

        // Execute
        String result = timeCardController.clockIn(999L);

        // Verify
        assertEquals("failure", result);
        verify(timeCardRepository, never()).save(any(TimeCard.class));
    }

    @Test
    void testClockIn_AlreadyClockedIn() {
        // Setup
        when(userRepository.findById(testUser.getId())).thenReturn(testUser);
        when(timeCardRepository.findByUserAndClockOutIsNull(testUser)).thenReturn(testTimeCard);

        // Execute
        String result = timeCardController.clockIn(testUser.getId());

        // Verify
        assertEquals("failure", result);
        verify(timeCardRepository, never()).save(any(TimeCard.class));
    }

    @Test
    void testClockOut_Success() {
        // Setup
        when(timeCardRepository.findByUserNumberAndClockOutIsNull(testUser.getId())).thenReturn(testTimeCard);
        when(timeCardRepository.save(any(TimeCard.class))).thenReturn(testTimeCard);

        // Execute
        String result = timeCardController.clockOut(testUser.getId());

        // Verify
        assertEquals("success", result);
        verify(timeCardRepository).save(any(TimeCard.class));
    }

    @Test
    void testClockOut_NoActiveTimeCard() {
        // Setup
        when(timeCardRepository.findByUserNumberAndClockOutIsNull(testUser.getId())).thenReturn(null);

        // Execute
        String result = timeCardController.clockOut(testUser.getId());

        // Verify
        assertEquals("failure", result);
        verify(timeCardRepository, never()).save(any(TimeCard.class));
    }

    @Test
    void testGetLatestTimeCard_LatestClosedCard() {
        // Setup
        when(timeCardRepository.findByUserNumberAndClockOutIsNull(testUser.getId())).thenReturn(null);
        when(timeCardRepository.findFirstByUserNumberOrderByClockInDesc(testUser.getId())).thenReturn(testTimeCard);

        // Execute
        ResponseEntity<?> response = timeCardController.getLatestTimeCard(testUser.getId());

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTimeCard, response.getBody());
    }

    @Test
    void testGetLatestTimeCard_NotFound() {
        // Setup
        when(timeCardRepository.findByUserNumberAndClockOutIsNull(testUser.getId())).thenReturn(null);
        when(timeCardRepository.findFirstByUserNumberOrderByClockInDesc(testUser.getId())).thenReturn(null);

        // Execute
        ResponseEntity<?> response = timeCardController.getLatestTimeCard(testUser.getId());

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testSetDayTimeCard_Success() {
        // Setup
        DayTimeCardRequest request = new DayTimeCardRequest();
        request.setClockIn(testDateTime);
        request.setClockOut(testDateTime.plusHours(8));

        when(userRepository.findById(testUser.getId())).thenReturn(testUser);
        when(timeCardRepository.save(any(TimeCard.class))).thenReturn(testTimeCard);

        // Execute
        ResponseEntity<?> response = timeCardController.setDayTimeCard(testUser.getId(), request);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(timeCardRepository).save(any(TimeCard.class));
    }

    @Test
    void testSetDayTimeCard_UserNotFound() {
        // Setup
        DayTimeCardRequest request = new DayTimeCardRequest();
        when(userRepository.findById(999L)).thenReturn(null);

        // Execute
        ResponseEntity<?> response = timeCardController.setDayTimeCard(999L, request);

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(timeCardRepository, never()).save(any(TimeCard.class));
    }

    @Test
    void testTimeCardEntity() {
        // Test TimeCard entity getters and setters
        TimeCard timeCard = new TimeCard();
        
        timeCard.setId(1L);
        timeCard.setUser(testUser);
        timeCard.setClockIn(testDateTime);
        timeCard.setClockOut(testDateTime.plusHours(8));
        timeCard.setHoursWorked(8.0);
        timeCard.setUserNumber(1L);
        timeCard.setWeekNumber(1);
        timeCard.setYear(2024);

        assertEquals(1L, timeCard.getId());
        assertEquals(testUser, timeCard.getUser());
        assertEquals(testDateTime, timeCard.getClockIn());
        assertEquals(testDateTime.plusHours(8), timeCard.getClockOut());
        assertEquals(8.0, timeCard.getHoursWorked());
        assertEquals(1L, timeCard.getUserNumber());
        assertEquals(1, timeCard.getWeekNumber());
        assertEquals(2024, timeCard.getYear());
    }
} 