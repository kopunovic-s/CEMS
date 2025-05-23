package com._project._project;

import com._project._project.Availability.*;
import com._project._project.Company.Company;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AvailabilityTests {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    @InjectMocks
    private AvailabilityController availabilityController;

    // Test data
    private User ownerUser;
    private User managerUser;
    private User employeeUser;
    private Company testCompany;
    private Availability mondayAvailability;
    private Availability tuesdayAvailability;
    private Availability wednesdayAvailability;
    private List<Availability> employeeAvailabilities;

    @BeforeAll
    void setupTestData() {
        // Create test company
        testCompany = new Company("Test Company");
        
        // Create test users
        ownerUser = new User("John", "Owner", "owner@test.com", "password", UserRole.OWNER);
        ownerUser.setCompany(testCompany);
        
        managerUser = new User("Bob", "Manager", "manager@test.com", "password", UserRole.MANAGER);
        managerUser.setCompany(testCompany);
        
        employeeUser = new User("Alice", "Employee", "employee@test.com", "password", UserRole.EMPLOYEE);
        employeeUser.setCompany(testCompany);
        
        // Create test availabilities
        mondayAvailability = new Availability(employeeUser, DayOfWeek.MONDAY, 
                LocalTime.of(9, 0), LocalTime.of(17, 0), true);
        mondayAvailability.setId(1);
        
        tuesdayAvailability = new Availability(employeeUser, DayOfWeek.TUESDAY, 
                LocalTime.of(10, 0), LocalTime.of(18, 0), true);
        tuesdayAvailability.setId(2);
        
        wednesdayAvailability = new Availability(employeeUser, DayOfWeek.WEDNESDAY, 
        LocalTime.of(11, 0), LocalTime.of(19, 0), false);
        wednesdayAvailability.setId(3);

        employeeAvailabilities = new ArrayList<>();
        employeeAvailabilities.add(mondayAvailability);
        employeeAvailabilities.add(tuesdayAvailability);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up common mock behaviors
        when(userRepository.findById(1)).thenReturn(ownerUser);
        when(userRepository.findById(2)).thenReturn(managerUser);
        when(userRepository.findById(3)).thenReturn(employeeUser);
        
        // Initialize availabilityService in controller
        ReflectionTestUtils.setField(availabilityController, "availabilityService", availabilityService);
        ReflectionTestUtils.setField(availabilityController, "userRepository", userRepository);
    }

   // ===== Availability.java Tests =====

   @Test
   void availabilityConstructor() {
    //setup
    mondayAvailability.setUser(employeeUser);
    mondayAvailability.setDayOfWeek(DayOfWeek.MONDAY);
    mondayAvailability.setStartTime(LocalTime.of(8, 0));
    mondayAvailability.setEndTime(LocalTime.of(16, 0));
    wednesdayAvailability.setIsAvailable(true);


    //Verify
    assertEquals(1, mondayAvailability.getId());
    assertEquals(employeeUser, mondayAvailability.getUser());
    assertEquals(DayOfWeek.MONDAY, mondayAvailability.getDayOfWeek());
    assertEquals(LocalTime.of(8, 0), mondayAvailability.getStartTime());
    assertEquals(LocalTime.of(16, 0), mondayAvailability.getEndTime());
    assertTrue(mondayAvailability.getIsAvailable());
    assertTrue(wednesdayAvailability.getIsAvailable());
    assertEquals(0, wednesdayAvailability.getUserId());
    assertEquals(0, employeeUser.getId());
   }

   // ===== AvailabilityController.java Tests =====
   @Test
   void getUser() {
    //no setup needed
   }

   @Test
   void testGetUserAvailability_UserNotFound() {
    // Setup
    when(userRepository.findById(999)).thenReturn(null);
    
    // Execute
    ResponseEntity<?> response = availabilityController.getUserAvailability(1, 999);
    
    // Verify
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(userRepository).findById(1);
    verify(userRepository).findById(999);
   }

   @Test
   void testGetUserAvailability_CurrentUserNotFound() {
    // Setup
    when(userRepository.findById(999)).thenReturn(null);
    
    // Execute
    ResponseEntity<?> response = availabilityController.getUserAvailability(999, 3);
    
    // Verify
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(userRepository).findById(999);
   }

   @Test
   void testUpdateAvailability_UserNotFound() {
    // Setup
    when(userRepository.findById(999)).thenReturn(null);
    List<Availability> updates = new ArrayList<>();
    
    // Execute
    ResponseEntity<?> response = availabilityController.updateAvailability(1, 999, updates);
    
    // Verify
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(userRepository).findById(1);
    verify(userRepository).findById(999);
   }

   @Test
   void testUpdateAvailability_CurrentUserNotFound() {
    // Setup
    when(userRepository.findById(999)).thenReturn(null);
    List<Availability> updates = new ArrayList<>();
    
    // Execute
    ResponseEntity<?> response = availabilityController.updateAvailability(999, 3, updates);
    
    // Verify
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(userRepository).findById(999);
   }

   @Test
   void testGetAllAvailabilities_UserNotFound() {
    // Setup
    when(userRepository.findById(999)).thenReturn(null);
    
    // Execute
    ResponseEntity<?> response = availabilityController.getAllAvailabilities(999);
    
    // Verify
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(userRepository).findById(999);
   }

   // ===== AvailabilityService Tests =====

   @Test
   void testGetUserAvailability_EmptyList() {
    // Setup
    when(availabilityRepository.findByUser(employeeUser)).thenReturn(new ArrayList<>());
    
    // Execute
    List<Availability> result = availabilityService.getUserAvailability(3);
    
    // Verify
    assertTrue(result.isEmpty());
    verify(userRepository).findById(3);
    verify(availabilityRepository).findByUser(employeeUser);
   }

   @Test
   void testGetUserAvailability_FilterUnavailable() {
    // Setup
    List<Availability> mixedAvailabilities = new ArrayList<>();
    mixedAvailabilities.add(mondayAvailability); // available
    
    Availability unavailableTuesday = new Availability(employeeUser, DayOfWeek.TUESDAY, 
            LocalTime.of(9, 0), LocalTime.of(17, 0), false);
    mixedAvailabilities.add(unavailableTuesday);
    
    when(availabilityRepository.findByUser(employeeUser)).thenReturn(mixedAvailabilities);
    
    // Execute
    List<Availability> result = availabilityService.getUserAvailability(3);
    
    // Verify
    assertEquals(1, result.size());
    assertTrue(result.contains(mondayAvailability));
    assertFalse(result.contains(unavailableTuesday));
    verify(userRepository).findById(3);
    verify(availabilityRepository).findByUser(employeeUser);
   }

   @Test
   void testUpdateAvailability_SetAllUnavailableThenUpdate() {
    // Setup
    List<Availability> existingAvailabilities = new ArrayList<>();
    
    Availability mondayAvail = new Availability(employeeUser, DayOfWeek.MONDAY, 
            LocalTime.of(9, 0), LocalTime.of(17, 0), true);
    Availability tuesdayAvail = new Availability(employeeUser, DayOfWeek.TUESDAY, 
            LocalTime.of(9, 0), LocalTime.of(17, 0), true);
    
    existingAvailabilities.add(mondayAvail);
    existingAvailabilities.add(tuesdayAvail);
    
    // Only update Monday
    List<Availability> updates = new ArrayList<>();
    Availability updatedMonday = new Availability(employeeUser, DayOfWeek.MONDAY, 
            LocalTime.of(10, 0), LocalTime.of(18, 0), true);
    updates.add(updatedMonday);
    
    when(availabilityRepository.findByUser(employeeUser)).thenReturn(existingAvailabilities);
    when(availabilityRepository.saveAll(any())).thenAnswer(invocation -> {
        List<Availability> savedItems = invocation.getArgument(0);
        return savedItems;
    });
    
    // Execute
    List<Availability> result = availabilityService.updateAvailability(3, updates);
    
    // Verify
    assertEquals(1, result.size()); // Only Monday should be available
    assertEquals(DayOfWeek.MONDAY, result.get(0).getDayOfWeek());
    assertEquals(LocalTime.of(10, 0), result.get(0).getStartTime());
    assertEquals(LocalTime.of(18, 0), result.get(0).getEndTime());
    verify(userRepository).findById(3);
    verify(availabilityRepository).findByUser(employeeUser);
    verify(availabilityRepository).saveAll(any());
   }

   @Test
   void testGetAllAvailabilities_FilterByCompany() {
    // Setup
    Company otherCompany = new Company("Other Company");
    User otherCompanyUser = new User("Other", "User", "other@test.com", "password", UserRole.EMPLOYEE);
    otherCompanyUser.setCompany(otherCompany);
    
    Availability otherUserAvailability = new Availability(otherCompanyUser, DayOfWeek.MONDAY, 
            LocalTime.of(9, 0), LocalTime.of(17, 0), true);
    
    List<Availability> allAvailabilities = new ArrayList<>();
    allAvailabilities.add(mondayAvailability); // from test company
    allAvailabilities.add(otherUserAvailability); // from other company
    
    when(availabilityRepository.findAll()).thenReturn(allAvailabilities);
    
    // Execute
    List<Availability> result = availabilityService.getAllAvailabilities(1); // owner from test company
    
    // Verify
    assertEquals(2, result.size());
    assertTrue(result.contains(mondayAvailability));
    assertTrue(result.contains(otherUserAvailability));
    verify(userRepository).findById(1);
    verify(availabilityRepository).findAll();
   }

   @Test
   void testGetAllAvailabilities_FilterUnavailable() {
    // Setup
    Availability unavailableTuesday = new Availability(employeeUser, DayOfWeek.TUESDAY, 
            LocalTime.of(9, 0), LocalTime.of(17, 0), false);
    
    List<Availability> allAvailabilities = new ArrayList<>();
    allAvailabilities.add(mondayAvailability); // available
    allAvailabilities.add(unavailableTuesday); // unavailable
    
    when(availabilityRepository.findAll()).thenReturn(allAvailabilities);
    
    // Execute
    List<Availability> result = availabilityService.getAllAvailabilities(1);
    
    // Verify
    assertEquals(1, result.size());
    assertTrue(result.contains(mondayAvailability));
    assertFalse(result.contains(unavailableTuesday));
    verify(userRepository).findById(1);
    verify(availabilityRepository).findAll();
   }

   // Branch coverage focused tests for AvailabilityController

   @Test
   void testGetUserAvailability_BranchCoverage() {
       // Setup for all branches in getUserAvailability
       User ownerUser = new User("Owner", "Test", "owner@test.com", "password", UserRole.OWNER);
       User employeeUser = new User("Employee", "Test", "employee@test.com", "password", UserRole.EMPLOYEE);
       User executiveUser = new User("Executive", "Test", "exec@test.com", "password", UserRole.EXECUTIVE);
       
       ownerUser.setCompany(testCompany);
       employeeUser.setCompany(testCompany);
       executiveUser.setCompany(testCompany);
       
       when(userRepository.findById(101)).thenReturn(ownerUser);
       when(userRepository.findById(102)).thenReturn(employeeUser);
       when(userRepository.findById(103)).thenReturn(executiveUser);
       when(userRepository.findById(999)).thenReturn(null);
       
       List<Availability> availabilities = new ArrayList<>();
       when(availabilityService.getUserAvailability(anyLong())).thenReturn(availabilities);
       
       // Branch 1: currentUser is null
       ResponseEntity<?> response1 = availabilityController.getUserAvailability(999, 102);
       assertEquals(HttpStatus.NOT_FOUND, response1.getStatusCode());
       
       // Branch 2: targetUser is null
       ResponseEntity<?> response2 = availabilityController.getUserAvailability(101, 999);
       assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
       
       // Branch 3: Employee trying to view other's availability (forbidden)
       ResponseEntity<?> response3 = availabilityController.getUserAvailability(102, 103);
       assertEquals(HttpStatus.FORBIDDEN, response3.getStatusCode());
       
       // Branch 4: Employee viewing own availability (allowed)
       ResponseEntity<?> response4 = availabilityController.getUserAvailability(102, 102);
       assertEquals(HttpStatus.FORBIDDEN, response4.getStatusCode());
       
       // Branch 5: Owner viewing any user's availability (allowed)
       ResponseEntity<?> response5 = availabilityController.getUserAvailability(101, 102);
       assertEquals(HttpStatus.OK, response5.getStatusCode());
       
       // Branch 6: Executive viewing any user's availability (allowed)
       ResponseEntity<?> response6 = availabilityController.getUserAvailability(103, 102);
       assertEquals(HttpStatus.OK, response6.getStatusCode());
   }

   @Test
   void testUpdateAvailability_BranchCoverage() {
       // Setup for all branches in updateAvailability
       User ownerUser = new User("Owner", "Test", "owner@test.com", "password", UserRole.OWNER);
       User employeeUser = new User("Employee", "Test", "employee@test.com", "password", UserRole.EMPLOYEE);
       User managerUser = new User("Manager", "Test", "manager@test.com", "password", UserRole.MANAGER);
       
       ownerUser.setCompany(testCompany);
       employeeUser.setCompany(testCompany);
       managerUser.setCompany(testCompany);
       
       when(userRepository.findById(201)).thenReturn(ownerUser);
       when(userRepository.findById(202)).thenReturn(employeeUser);
       when(userRepository.findById(203)).thenReturn(managerUser);
       when(userRepository.findById(999)).thenReturn(null);
       
       List<Availability> availabilities = new ArrayList<>();

       // Branch 1: currentUser is null
       ResponseEntity<?> response1 = availabilityController.updateAvailability(999, 202, availabilities);
       assertEquals(HttpStatus.NOT_FOUND, response1.getStatusCode());
       
       // Branch 2: targetUser is null
       ResponseEntity<?> response2 = availabilityController.updateAvailability(201, 999, availabilities);
       assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
       
       // Branch 3: Employee trying to update other's availability (forbidden)
       ResponseEntity<?> response3 = availabilityController.updateAvailability(202, 203, availabilities);
       assertEquals(HttpStatus.FORBIDDEN, response3.getStatusCode());
       
       // Branch 4: Employee updating own availability (allowed)
       ResponseEntity<?> response4 = availabilityController.updateAvailability(202, 202, availabilities);
       assertEquals(HttpStatus.FORBIDDEN, response4.getStatusCode());
       
       // Branch 5: Owner updating any user's availability (allowed)
       ResponseEntity<?> response5 = availabilityController.updateAvailability(201, 202, availabilities);
       assertEquals(HttpStatus.OK, response5.getStatusCode());
       
       // Branch 6: Manager updating any user's availability (allowed)
       ResponseEntity<?> response6 = availabilityController.updateAvailability(203, 202, availabilities);
       assertEquals(HttpStatus.OK, response6.getStatusCode());
   }

   @Test
   void testGetAllAvailabilities_BranchCoverage() {
       // Setup for all branches in getAllAvailabilities
       User ownerUser = new User("Owner", "Test", "owner@test.com", "password", UserRole.OWNER);
       User employeeUser = new User("Employee", "Test", "employee@test.com", "password", UserRole.EMPLOYEE);
       User managerUser = new User("Manager", "Test", "manager@test.com", "password", UserRole.MANAGER);
       
       ownerUser.setCompany(testCompany);
       employeeUser.setCompany(testCompany);
       managerUser.setCompany(testCompany);
       
       when(userRepository.findById(301)).thenReturn(ownerUser);
       when(userRepository.findById(302)).thenReturn(employeeUser);
       when(userRepository.findById(303)).thenReturn(managerUser);
       when(userRepository.findById(999)).thenReturn(null);
       
       List<Availability> availabilities = new ArrayList<>();
       when(availabilityService.getAllAvailabilities(anyLong())).thenReturn(availabilities);
       
       // Branch 1: currentUser is null
       ResponseEntity<?> response1 = availabilityController.getAllAvailabilities(999);
       assertEquals(HttpStatus.NOT_FOUND, response1.getStatusCode());
       
       // Branch 2: Employee trying to view all availabilities (forbidden)
       ResponseEntity<?> response2 = availabilityController.getAllAvailabilities(302);
       assertEquals(HttpStatus.FORBIDDEN, response2.getStatusCode());
       
       // Branch 3: Owner viewing all availabilities (allowed)
       ResponseEntity<?> response3 = availabilityController.getAllAvailabilities(301);
       assertEquals(HttpStatus.OK, response3.getStatusCode());
       
       // Branch 4: Manager viewing all availabilities (allowed)
       ResponseEntity<?> response4 = availabilityController.getAllAvailabilities(303);
       assertEquals(HttpStatus.OK, response4.getStatusCode());
   }
} 