package com._project._project;

import com._project._project.Company.Company;
import com._project._project.Schedule.*;
import com._project._project.User.*;
import com._project._project.Availability.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScheduleServiceTests {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    // Test data
    private User employeeUser;
    private Company testCompany;
    private LocalDate testDate;
    private Schedule testSchedule;

    @BeforeAll
    void setupTestData() {
        testCompany = new Company("Test Company");
        testCompany.setId(1L);

        employeeUser = new User("Alice", "Employee", "employee@test.com", "password", UserRole.EMPLOYEE);
        employeeUser.setId(2L);
        employeeUser.setCompany(testCompany);

        testDate = LocalDate.now();
        
        testSchedule = new Schedule();
        testSchedule.setId(1L);
        testSchedule.setUser(employeeUser);
        testSchedule.setUserId(employeeUser.getId());
        testSchedule.setStartTime(LocalDateTime.of(testDate, LocalTime.of(9, 0)));
        testSchedule.setEndTime(LocalDateTime.of(testDate, LocalTime.of(17, 0)));
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserSchedule() {
        // Setup
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findByUserId(employeeUser.getId())).thenReturn(schedules);

        // Execute
        List<Schedule> result = scheduleService.getUserSchedule(employeeUser.getId());

        // Verify
        assertEquals(1, result.size());
        assertEquals(testSchedule, result.get(0));
        verify(scheduleRepository).findByUserId(employeeUser.getId());
    }

    @Test
    void testGetScheduleByDateRange() {
        // Setup
        LocalDateTime start = testDate.atStartOfDay();
        LocalDateTime end = testDate.plusDays(1).atStartOfDay();
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findByStartTimeBetween(start, end)).thenReturn(schedules);

        // Execute
        List<Schedule> result = scheduleService.getScheduleByDateRange(start, end);

        // Verify
        assertEquals(1, result.size());
        assertEquals(testSchedule, result.get(0));
        verify(scheduleRepository).findByStartTimeBetween(start, end);
    }

    @Test
    void testCreateSchedule_Success() {
        // Setupa
        when(userRepository.findById(employeeUser.getId())).thenReturn(employeeUser);
        when(availabilityRepository.findByUserAndDayOfWeek(
            eq(employeeUser), 
            any(DayOfWeek.class)
        )).thenReturn(Arrays.asList(new Availability(employeeUser, 
            DayOfWeek.MONDAY, 
            LocalTime.of(9, 0), 
            LocalTime.of(17, 0), 
            true)));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(testSchedule);

        // Execute
        Schedule result = scheduleService.createSchedule(employeeUser.getId(), testSchedule);

        // Verify
        assertNotNull(result);
        assertEquals(testSchedule, result);
        verify(userRepository).findById(employeeUser.getId());
        verify(scheduleRepository).save(testSchedule);
    }

    @Test
    void testCreateSchedule_AvailabilityConflict() {
        // Setup
        when(userRepository.findById(employeeUser.getId())).thenReturn(employeeUser);
        when(availabilityRepository.findByUserAndDayOfWeek(
            eq(employeeUser), 
            any(DayOfWeek.class)
        )).thenReturn(Arrays.asList(new Availability(employeeUser, 
            DayOfWeek.MONDAY, 
            LocalTime.of(9, 0), 
            LocalTime.of(17, 0), 
            false)));  // User is not available

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> 
            scheduleService.createSchedule(employeeUser.getId(), testSchedule));
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void testCreateWeekFromAvailability_Success() {
        // Setup
        when(userRepository.findById(employeeUser.getId())).thenReturn(employeeUser);
        Availability availability = new Availability(employeeUser, 
            DayOfWeek.MONDAY, 
            LocalTime.of(9, 0), 
            LocalTime.of(17, 0), 
            true);
        when(availabilityRepository.findByUserAndDayOfWeek(
            eq(employeeUser), 
            any(DayOfWeek.class)
        )).thenReturn(Arrays.asList(availability));
        when(scheduleRepository.saveAll(anyList())).thenReturn(Arrays.asList(testSchedule));

        // Execute
        List<Schedule> result = scheduleService.createWeekFromAvailability(employeeUser.getId());

        // Verify
        assertFalse(result.isEmpty());
        verify(availabilityRepository, atLeastOnce()).findByUserAndDayOfWeek(any(), any());
        verify(scheduleRepository).saveAll(anyList());
    }

    @Test
    void testUpdateSchedule_Success() {
        // Setup
        Schedule updatedSchedule = new Schedule();
        updatedSchedule.setId(1L);
        updatedSchedule.setStartTime(LocalDateTime.of(testDate, LocalTime.of(10, 0)));
        updatedSchedule.setEndTime(LocalDateTime.of(testDate, LocalTime.of(18, 0)));
        
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(updatedSchedule);

        // Execute
        Schedule result = scheduleService.updateSchedule(updatedSchedule);

        // Verify
        assertNotNull(result);
        assertEquals(updatedSchedule.getStartTime(), result.getStartTime());
        assertEquals(updatedSchedule.getEndTime(), result.getEndTime());
        verify(scheduleRepository).findById(1L);
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void testUpdateSchedule_NotFound() {
        // Setup
        Schedule schedule = new Schedule();
        schedule.setId(999L);
        when(scheduleRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> scheduleService.updateSchedule(schedule));
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void testUpdateBatchSchedule_Success() {
        // Setup
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        List<Schedule> schedules = Arrays.asList(schedule);
        
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(testSchedule);

        // Execute
        List<Schedule> result = scheduleService.updateBatchSchedule(schedules);

        // Verify
        assertEquals(1, result.size());
        verify(scheduleRepository).findById(1L);
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void testCreateWeekSchedule_Success() {
        // Setup
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(userRepository.findById(employeeUser.getId())).thenReturn(employeeUser);
        when(availabilityRepository.findByUserAndDayOfWeek(
            eq(employeeUser), 
            any(DayOfWeek.class)
        )).thenReturn(Arrays.asList(new Availability(employeeUser, 
            DayOfWeek.MONDAY, 
            LocalTime.of(9, 0), 
            LocalTime.of(17, 0), 
            true)));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(testSchedule);

        // Execute
        List<Schedule> result = scheduleService.createWeekSchedule(employeeUser.getId(), schedules);

        // Verify
        assertEquals(1, result.size());
        assertEquals(testSchedule, result.get(0));
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void testCreateWeekFromAvailability_NoAvailableDays() {
        // Setup
        when(userRepository.findById(employeeUser.getId())).thenReturn(employeeUser);
        when(availabilityRepository.findByUserAndDayOfWeek(
            eq(employeeUser), 
            any(DayOfWeek.class)
        )).thenReturn(Arrays.asList(new Availability(employeeUser, 
            DayOfWeek.MONDAY, 
            LocalTime.of(9, 0), 
            LocalTime.of(17, 0), 
            false)));

        // Execute & Verify
        assertThrows(RuntimeException.class, 
            () -> scheduleService.createWeekFromAvailability(employeeUser.getId()));
        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    void testUpdateSchedules_UserNotFound() {
        // Setup
        List<Schedule> schedules = new ArrayList<>();
        Schedule schedule = new Schedule();
        schedule.setUserId(999L);
        schedules.add(schedule);

        when(userRepository.findById(999L)).thenThrow(new RuntimeException("User not found"));

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> scheduleService.updateSchedules(schedules));
        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    void testGetAllDaySchedule() {
        // Setup
        LocalDate date = testDate;
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findByStartTimeBetween(
            any(LocalDateTime.class),
            any(LocalDateTime.class)
        )).thenReturn(schedules);

        // Execute
        List<Schedule> result = scheduleService.getAllDaySchedule(testCompany.getId(), date);

        // Verify
        assertEquals(1, result.size());
        assertEquals(testSchedule, result.get(0));
        verify(scheduleRepository).findByStartTimeBetween(
            any(LocalDateTime.class),
            any(LocalDateTime.class)
        );
    }

    @Test
    void testGetAllWeekSchedule() {
        // Setup
        LocalDate date = testDate;
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findByStartTimeBetween(
            any(LocalDateTime.class),
            any(LocalDateTime.class)
        )).thenReturn(schedules);

        // Execute
        List<Schedule> result = scheduleService.getAllWeekSchedule(testCompany.getId(), date);

        // Verify
        assertEquals(1, result.size());
        assertEquals(testSchedule, result.get(0));
        verify(scheduleRepository).findByStartTimeBetween(
            any(LocalDateTime.class),
            any(LocalDateTime.class)
        );
    }

    @Test
    void testCreateWeekFromDate_Success() {
        // Setup
        when(userRepository.findById(employeeUser.getId())).thenReturn(employeeUser);
        Availability availability = new Availability(employeeUser, 
            DayOfWeek.MONDAY, 
            LocalTime.of(9, 0), 
            LocalTime.of(17, 0), 
            true);
        when(availabilityRepository.findByUserAndDayOfWeek(
            eq(employeeUser), 
            any(DayOfWeek.class)
        )).thenReturn(Arrays.asList(availability));
        when(scheduleRepository.saveAll(anyList())).thenReturn(Arrays.asList(testSchedule));

        // Execute
        List<Schedule> result = scheduleService.createWeekFromDate(employeeUser.getId(), testDate);

        // Verify
        assertFalse(result.isEmpty());
        verify(availabilityRepository, atLeastOnce()).findByUserAndDayOfWeek(any(), any());
        verify(scheduleRepository).saveAll(anyList());
    }

    @Test
    void testCreateWeekFromDate_NoAvailableDays() {
        // Setup
        when(userRepository.findById(employeeUser.getId())).thenReturn(employeeUser);
        when(availabilityRepository.findByUserAndDayOfWeek(
            eq(employeeUser), 
            any(DayOfWeek.class)
        )).thenReturn(Arrays.asList(new Availability(employeeUser, 
            DayOfWeek.MONDAY, 
            LocalTime.of(9, 0), 
            LocalTime.of(17, 0), 
            false)));

        // Execute & Verify
        assertThrows(RuntimeException.class, 
            () -> scheduleService.createWeekFromDate(employeeUser.getId(), testDate));
        verify(scheduleRepository, never()).saveAll(anyList());
    }

}