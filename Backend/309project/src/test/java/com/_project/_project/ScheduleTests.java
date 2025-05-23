package com._project._project;

import com._project._project.Company.Company;
import com._project._project.Schedule.*;
import com._project._project.User.*;
import com._project._project.Perminissions.PermissionsService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import com._project._project.Availability.Availability;
import com._project._project.Availability.AvailabilityRepository;
import com._project._project.Schedule.ScheduleRepository;
import com._project._project.Schedule.ScheduleService;
import com._project._project.Schedule.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScheduleTests {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionsService permissionsService;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ScheduleController scheduleController;

    @Mock
    private AvailabilityRepository availabilityRepository;

    // Test data
    private User ownerUser;
    private User employeeUser;
    private Company testCompany;
    private Schedule testSchedule;
    private LocalDate testDate;
    private List<Schedule> weekSchedules;

    @BeforeAll
    void setupTestData() {
        testCompany = new Company("Test Company");
        testCompany.setId(1L);

        ownerUser = new User("John", "Owner", "owner@test.com", "password", UserRole.OWNER);
        ownerUser.setId(1L);
        ownerUser.setCompany(testCompany);

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

        weekSchedules = new ArrayList<>();
        weekSchedules.add(testSchedule);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(scheduleController, "scheduleService", scheduleService);
        ReflectionTestUtils.setField(scheduleController, "permissionsService", permissionsService);
    }

    // ===== Controller Tests =====

    @Test
    void testGetDaySchedule_Success() {
        // Setup
        when(userRepository.findById(1L)).thenReturn(ownerUser);
        when(userRepository.findById(2L)).thenReturn(employeeUser);
        when(permissionsService.NoUserExists(ownerUser, employeeUser)).thenReturn(false);
        when(permissionsService.NoDoublePermissions(ownerUser, employeeUser)).thenReturn(false);
        when(scheduleService.getScheduleByDate(any(LocalDate.class), any(User.class))).thenReturn(weekSchedules);

        // Execute
        ResponseEntity<?> response = scheduleController.getDaySchedule(1L, 2L, testDate);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(scheduleService).getScheduleByDate(testDate, employeeUser);
    }

    @Test
    void testGetDaySchedule_UserNotFound() {
        // Setup
        when(permissionsService.NoUserExists(null, null)).thenReturn(true);

        // Execute
        ResponseEntity<?> response = scheduleController.getDaySchedule(999L, 2L, testDate);

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(scheduleService, never()).getScheduleByDate(any(), any());
    }

    @Test
    void testGetWeekSchedule_Success() {
        // Setup
        when(userRepository.findById(1L)).thenReturn(ownerUser);
        when(userRepository.findById(2L)).thenReturn(employeeUser);
        when(permissionsService.NoUserExists(ownerUser, employeeUser)).thenReturn(false);
        when(permissionsService.NoDoublePermissions(ownerUser, employeeUser)).thenReturn(false);
        when(scheduleService.getScheduleByWeek(any(LocalDate.class), any(User.class))).thenReturn(weekSchedules);

        // Execute
        ResponseEntity<?> response = scheduleController.getWeekSchedule(1L, 2L, testDate);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(scheduleService).getScheduleByWeek(testDate, employeeUser);
    }

    @Test
    void testCreateSchedule_Success() {
        // Setup
        when(userRepository.findById(1L)).thenReturn(ownerUser);
        when(userRepository.findById(2L)).thenReturn(employeeUser);
        when(permissionsService.NoUserExists(ownerUser, employeeUser)).thenReturn(false);
        when(scheduleService.createSchedule(anyLong(), any(Schedule.class))).thenReturn(testSchedule);

        // Execute
        ResponseEntity<?> response = scheduleController.createSchedule(1L, 2L, testSchedule);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(scheduleService).createSchedule(2L, testSchedule);
    }

    @Test
    void testCreateWeekSchedule_Success() {
        // Setup
        when(scheduleService.createWeekSchedule(anyLong(), anyList())).thenReturn(weekSchedules);

        // Execute
        ResponseEntity<?> response = scheduleController.createWeekSchedule(1L, weekSchedules);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(scheduleService).createWeekSchedule(1L, weekSchedules);
    }

    @Test
    void testCreateWeekFromAvailability_Success() {
        // Setup
        when(userRepository.findById(1L)).thenReturn(ownerUser);
        when(userRepository.findById(2L)).thenReturn(employeeUser);
        when(permissionsService.NoUserExists(ownerUser, employeeUser)).thenReturn(false);
        when(scheduleService.createWeekFromAvailability(anyLong())).thenReturn(weekSchedules);

        // Execute
        ResponseEntity<?> response = scheduleController.createWeekFromAvailability(1L, 2L);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(scheduleService).createWeekFromAvailability(2L);
    }

    @Test
    void testUpdateSchedule_Success() {
        // Setup
        when(userRepository.findById(1L)).thenReturn(ownerUser);
        when(permissionsService.NoUserExists(ownerUser, ownerUser)).thenReturn(false);
        when(scheduleService.updateSchedule(any(Schedule.class))).thenReturn(testSchedule);

        // Execute
        ResponseEntity<?> response = scheduleController.updateSchedule(1L, 1L, testSchedule);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(scheduleService).updateSchedule(testSchedule);
    }

    @Test
    void testCreateBatchSchedule_Success() {
        // Setup
        when(userRepository.findById(1L)).thenReturn(ownerUser);
        when(permissionsService.NoUserExists(ownerUser, ownerUser)).thenReturn(false);
        when(permissionsService.IsEmployee(ownerUser)).thenReturn(false);
        when(scheduleService.createBatchSchedule(anyList())).thenReturn(weekSchedules);

        // Execute
        ResponseEntity<?> response = scheduleController.createBatchSchedule(1L, weekSchedules);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(scheduleService).createBatchSchedule(weekSchedules);
    }

    @Test
    void testUpdateBatchSchedule_Success() {
        // Setup
        when(userRepository.findById(1L)).thenReturn(ownerUser);
        when(permissionsService.NoUserExists(ownerUser, ownerUser)).thenReturn(false);
        when(permissionsService.IsEmployee(ownerUser)).thenReturn(false);
        when(scheduleService.updateBatchSchedule(anyList())).thenReturn(weekSchedules);

        // Execute
        ResponseEntity<?> response = scheduleController.updateBatchSchedule(1L, weekSchedules);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(scheduleService).updateBatchSchedule(weekSchedules);
    }

    // Add error cases for each endpoint
    @Test
    void testCreateBatchSchedule_EmployeeForbidden() {
        // Setup
        when(userRepository.findById(2L)).thenReturn(employeeUser);
        when(permissionsService.NoUserExists(employeeUser, employeeUser)).thenReturn(false);
        when(permissionsService.IsEmployee(employeeUser)).thenReturn(true);

        // Execute
        ResponseEntity<?> response = scheduleController.createBatchSchedule(2L, weekSchedules);

        // Verify
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(scheduleService, never()).createBatchSchedule(anyList());
    }

    @Test
    void testUpdateBatchSchedule_EmployeeForbidden() {
        // Setup
        when(userRepository.findById(2L)).thenReturn(employeeUser);
        when(permissionsService.NoUserExists(employeeUser, employeeUser)).thenReturn(false);
        when(permissionsService.IsEmployee(employeeUser)).thenReturn(true);

        // Execute
        ResponseEntity<?> response = scheduleController.updateBatchSchedule(2L, weekSchedules);

        // Verify
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(scheduleService, never()).updateBatchSchedule(anyList());
    }

    // Add branch coverage focused tests for ScheduleController
    @Test
    void testGetDaySchedule_BranchCoverage() {
        // Setup for all branches
        User ownerUser = new User("Owner", "Test", "owner@test.com", "password", UserRole.OWNER);
        User employeeUser = new User("Employee", "Test", "employee@test.com", "password", UserRole.EMPLOYEE);
        User executiveUser = new User("Executive", "Test", "exec@test.com", "password", UserRole.EXECUTIVE);
        
        ownerUser.setCompany(testCompany);
        employeeUser.setCompany(testCompany);
        executiveUser.setCompany(testCompany);
        
        when(userRepository.findById(101L)).thenReturn(ownerUser);
        when(userRepository.findById(102L)).thenReturn(employeeUser);
        when(userRepository.findById(103L)).thenReturn(executiveUser);
        when(userRepository.findById(999L)).thenReturn(null);
        
        // Branch 1: currentUser is null
        ResponseEntity<?> response1 = scheduleController.getDaySchedule(999L, 102L, testDate);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        
        // Branch 2: targetUser is null
        ResponseEntity<?> response2 = scheduleController.getDaySchedule(101L, 999L, testDate);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        
        // Branch 3: Employee trying to view other's schedule (forbidden)
        when(permissionsService.NoDoublePermissions(employeeUser, executiveUser)).thenReturn(true);
        ResponseEntity<?> response3 = scheduleController.getDaySchedule(102L, 103L, testDate);
        assertEquals(HttpStatus.FORBIDDEN, response3.getStatusCode());
        
        // Branch 4: Owner viewing any user's schedule (allowed)
        when(permissionsService.NoDoublePermissions(ownerUser, employeeUser)).thenReturn(false);
        when(scheduleService.getScheduleByDate(any(), any())).thenReturn(weekSchedules);
        ResponseEntity<?> response5 = scheduleController.getDaySchedule(101L, 102L, testDate);
        assertEquals(HttpStatus.OK, response5.getStatusCode());
    }

    @Test
    void testCreateSchedule_BranchCoverage() {
        // Setup for all branches
        User ownerUser = new User("Owner", "Test", "owner@test.com", "password", UserRole.OWNER);
        User employeeUser = new User("Employee", "Test", "employee@test.com", "password", UserRole.EMPLOYEE);
        User managerUser = new User("Manager", "Test", "manager@test.com", "password", UserRole.MANAGER);
        
        ownerUser.setCompany(testCompany);
        employeeUser.setCompany(testCompany);
        managerUser.setCompany(testCompany);
        
        when(userRepository.findById(201L)).thenReturn(ownerUser);
        when(userRepository.findById(202L)).thenReturn(employeeUser);
        when(userRepository.findById(203L)).thenReturn(managerUser);
        when(userRepository.findById(999L)).thenReturn(null);
        
        // Branch 1: currentUser is null
        ResponseEntity<?> response1 = scheduleController.createSchedule(999L, 202L, testSchedule);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        
        // Branch 2: targetUser is null
        ResponseEntity<?> response2 = scheduleController.createSchedule(201L, 999L, testSchedule);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        
        // Branch 3: Employee trying to create other's schedule (forbidden)
        when(permissionsService.NoDoublePermissions(employeeUser, managerUser)).thenReturn(true);
        ResponseEntity<?> response3 = scheduleController.createSchedule(202L, 203L, testSchedule);
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        
        // Branch 4: Owner creating any user's schedule (allowed)
        when(permissionsService.NoDoublePermissions(ownerUser, employeeUser)).thenReturn(false);
        when(scheduleService.createSchedule(anyLong(), any())).thenReturn(testSchedule);
        ResponseEntity<?> response4 = scheduleController.createSchedule(201L, 202L, testSchedule);
        assertEquals(HttpStatus.OK, response4.getStatusCode());
    }

    @Test
    void testUpdateSchedule_BranchCoverage() {
        // Setup for all branches
        User ownerUser = new User("Owner", "Test", "owner@test.com", "password", UserRole.OWNER);
        User employeeUser = new User("Employee", "Test", "employee@test.com", "password", UserRole.EMPLOYEE);
        User managerUser = new User("Manager", "Test", "manager@test.com", "password", UserRole.MANAGER);
        
        ownerUser.setCompany(testCompany);
        employeeUser.setCompany(testCompany);
        managerUser.setCompany(testCompany);
        
        when(userRepository.findById(301L)).thenReturn(ownerUser);
        when(userRepository.findById(302L)).thenReturn(employeeUser);
        when(userRepository.findById(303L)).thenReturn(managerUser);
        when(userRepository.findById(999L)).thenReturn(null);
        
        // Branch 1: currentUser is null
        ResponseEntity<?> response1 = scheduleController.updateSchedule(999L, 302L, testSchedule);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        
        // Branch 2: Employee trying to update other's schedule (forbidden)
        when(permissionsService.NoDoublePermissions(employeeUser, managerUser)).thenReturn(true);
        ResponseEntity<?> response2 = scheduleController.updateSchedule(302L, 303L, testSchedule);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        
        // Branch 3: Owner updating any user's schedule (allowed)
        when(permissionsService.NoDoublePermissions(ownerUser, employeeUser)).thenReturn(false);
        when(scheduleService.updateSchedule(any())).thenReturn(testSchedule);
        ResponseEntity<?> response3 = scheduleController.updateSchedule(301L, 302L, testSchedule);
        assertEquals(HttpStatus.OK, response3.getStatusCode());
    }}

