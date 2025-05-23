package com._project._project;

import com._project._project.Company.Company;
import com._project._project.Company.CompanyRepository;
import com._project._project.Project.Project;
import com._project._project.Project.ProjectRepository;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class userTests {
    
    // Mocks
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CompanyRepository companyRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    // Service to test
    @InjectMocks
    private UserService userService;
    
    // Controllers to test
    @InjectMocks
    private CompanyUserController companyUserController;
    
    @InjectMocks
    private UserController userController;
    
    // Test data
    private User ownerUser;
    private User executiveUser;
    private User managerUser;
    private User employeeUser;
    private Company testCompany;
    private List<User> companyUsers;
    private Project testProject;
    
    @BeforeAll
    void setupTestData() {
        // Create test company
        testCompany = new Company("Test Company");
        
        // Create test users
        ownerUser = new User("John", "Owner", "owner@test.com", "password", UserRole.OWNER);
        ownerUser.setCompany(testCompany);
        
        executiveUser = new User("Jane", "Executive", "executive@test.com", "password", UserRole.EXECUTIVE);
        executiveUser.setCompany(testCompany);
        
        managerUser = new User("Bob", "Manager", "manager@test.com", "password", UserRole.MANAGER);
        managerUser.setCompany(testCompany);
        managerUser.setHourlyRate(25.0);
        
        employeeUser = new User("Alice", "Employee", "employee@test.com", "password", UserRole.EMPLOYEE);
        employeeUser.setCompany(testCompany);
        employeeUser.setHourlyRate(15.0);
        
        // Add users to company
        companyUsers = new ArrayList<>();
        companyUsers.add(ownerUser);
        companyUsers.add(executiveUser);
        companyUsers.add(managerUser);
        companyUsers.add(employeeUser);
        
        testCompany.setUsers(companyUsers);
    }
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up common mock behaviors
        when(userRepository.findById(1)).thenReturn(ownerUser);
        when(userRepository.findById(2)).thenReturn(executiveUser);
        when(userRepository.findById(3)).thenReturn(managerUser);
        when(userRepository.findById(4)).thenReturn(employeeUser);
        
        // Initialize userService in controllers
        ReflectionTestUtils.setField(companyUserController, "userService", userService);
    }
    
    // ===== UserService Tests =====
    
    @Test
    void testGetCompanyUsers_Service() {
        // Execute
        List<User> result = userService.getCompanyUsers(1);
        
        // Verify
        assertEquals(5, result.size());
        assertTrue(result.contains(ownerUser));
        assertTrue(result.contains(executiveUser));
        assertTrue(result.contains(managerUser));
        assertTrue(result.contains(employeeUser));
    }
    
    @Test
    void testGetUser_Service() {
        // Execute
        User result = userService.getUser(1, 3);
        
        // Verify
        assertEquals(managerUser, result);
        verify(userRepository).findById(3);
    }
    
    @Test
    void testGetUser_UserNotInCompany() {
        // Setup
        User otherCompanyUser = new User("Other", "User", "other@test.com", "password", UserRole.EMPLOYEE);
        Company otherCompany = new Company("Other Company");
        otherCompanyUser.setCompany(otherCompany);
        
        when(userRepository.findById(5)).thenReturn(otherCompanyUser);
        
        // Execute & Verify
        assertThrows(RuntimeException.class, () -> userService.getUser(1, 5));
        verify(userRepository).findById(5);
    }
    
    @Test
    void testCreateUser_Service() {
        // Setup
        User newUser = new User("New", "User", "new@test.com", "password", UserRole.EMPLOYEE);
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        
        // Execute
        userService.createUser(1, newUser);
        
        // Verify
        assertEquals(testCompany, newUser.getCompany());
        verify(userRepository).save(newUser);
    }
    
    @Test
    void testUpdateUser_Service() {
        // Setup
        User updatedUser = new User("Updated", "Manager", "manager@test.com", "newpassword", UserRole.MANAGER);
        when(userRepository.save(any(User.class))).thenReturn(managerUser);
        
        // Execute
        userService.updateUser(1, 3, updatedUser);
        
        // Verify
        assertEquals("Updated", managerUser.getFirstName());
        assertEquals("Manager", managerUser.getLastName());
        assertEquals("newpassword", managerUser.getPassword());
        verify(userRepository).save(managerUser);
    }
    
    @Test
    void testDeleteUser_Service() {
        // Setup
        List<Project> projects = new ArrayList<>();
        projects.add(testProject);
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        
        // Execute
        userService.deleteUser(1, 4);
        
        // Verify
        verify(userRepository).delete(employeeUser);
        verify(userRepository).save(employeeUser);
    }
    
    // ===== CompanyUserController Tests =====
    
    @Test
    void testGetCompanyUsers_Controller() {
        // Execute
        List<User> result = companyUserController.getCompanyUsers(1);
        
        // Verify
        assertEquals(5, result.size());
    }
    
    @Test
    void testGetUser_Controller() {
        // Execute
        User result = companyUserController.getUser(1, 3);
        
        // Verify
        assertEquals(managerUser, result);
        assertEquals("Updated", result.getFirstName());
        assertEquals(UserRole.MANAGER, result.getRole());
    }
    
    @Test
    void testUpdateUserWage_AsOwner() {
        // Execute
        ResponseEntity<?> response = companyUserController.updateUserWage(1, 4, 20.0);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(employeeUser);
        assertEquals(20.0, employeeUser.getHourlyRate());
    }
    
    @Test
    void testUpdateUserWage_AsEmployee_Forbidden() {
        // Execute
        ResponseEntity<?> response = companyUserController.updateUserWage(4, 3, 30.0);
        
        // Verify
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testUpdateUserInfo_Self() {
        // Setup
        Map<String, String> info = new HashMap<>();
        info.put("firstName", "Alice Updated");
        info.put("email", "alice.updated@test.com");
        
        // Execute
        ResponseEntity<?> response = companyUserController.updateUserInfo(4, 4, info);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Alice Updated", employeeUser.getFirstName());
        assertEquals("alice.updated@test.com", employeeUser.getEmail());
        verify(userRepository).save(employeeUser);
    }
    
    @Test
    void testGetUserInfo() {
        // Setup
        employeeUser.setStreetAddress("123 Main St");
        employeeUser.setCity("Anytown");
        employeeUser.setState("CA");
        employeeUser.setZipCode("12345");
        employeeUser.setCountry("USA");
        employeeUser.setSsn("123-45-6789");
        employeeUser.setHourlyRate(15.0);
        String testLogo = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...";
        testCompany.setCompanyLogo(testLogo);

        //Verify

        // Execute
        ResponseEntity<?> response = companyUserController.getUserInfo(1, 4);
        
        // Verify
        assertEquals(testLogo, testCompany.getCompanyLogo());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Object responseBody = response.getBody();
        assertTrue(responseBody instanceof Map<?, ?>);
        Map<?, ?> userInfo = (Map<?, ?>) responseBody;
        assertEquals("123 Main St", userInfo.get("streetAddress"));
        assertEquals("Anytown", userInfo.get("city"));
        assertEquals("CA", userInfo.get("state"));
        assertEquals("15.0", userInfo.get("hourlyRate"));
        assertEquals("12345", userInfo.get("zipCode"));
        assertEquals("USA", userInfo.get("country"));
        assertEquals("123-45-6789", userInfo.get("ssn"));
        assertEquals("Alice", employeeUser.getFirstName());
        assertEquals("Employee", employeeUser.getLastName());
        assertEquals("employee@test.com", employeeUser.getEmail());
        assertEquals("15.0", userInfo.get("hourlyRate"));
        assertEquals("Test Company", employeeUser.getCompanyName());
    }
    
    // ===== UserController Tests =====
    
    @Test
    void testGetAllUsers() {
        // Setup
        when(userRepository.findAll()).thenReturn(companyUsers);
        
        // Execute
        List<User> result = userController.getAllUsers();
        
        // Verify
        assertEquals(4, result.size());
        verify(userRepository).findAll();
    }
    
    @Test
    void testCreateUserBackend() {
        // Setup
        User newUser = new User("Backend", "Test", "backend@test.com", "password", UserRole.EMPLOYEE);
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        
        // Execute
        ResponseEntity<?> response = userController.createUser(newUser);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newUser, response.getBody());
        verify(userRepository).save(newUser);
    }

    // Branch coverage focused tests for CompanyUserController

    @Test
    void testUpdateUserWage_BranchCoverage() {
        // Setup for all branches in updateUserWage
        User ownerUser = new User("Owner", "Test", "owner@test.com", "password", UserRole.OWNER);
        User executiveUser = new User("Executive", "Test", "exec@test.com", "password", UserRole.EXECUTIVE);
        User managerUser = new User("Manager", "Test", "manager@test.com", "password", UserRole.MANAGER);
        User employeeUser = new User("Employee", "Test", "employee@test.com", "password", UserRole.EMPLOYEE);
        User ownerTargetUser = new User("Target", "Owner", "target@test.com", "password", UserRole.OWNER);
        
        ownerUser.setCompany(testCompany);
        executiveUser.setCompany(testCompany);
        managerUser.setCompany(testCompany);
        employeeUser.setCompany(testCompany);
        ownerTargetUser.setCompany(testCompany);
        
        when(userRepository.findById(401)).thenReturn(ownerUser);
        when(userRepository.findById(402)).thenReturn(executiveUser);
        when(userRepository.findById(403)).thenReturn(managerUser);
        when(userRepository.findById(404)).thenReturn(employeeUser);
        when(userRepository.findById(405)).thenReturn(ownerTargetUser);
        when(userRepository.findById(999)).thenReturn(null);
        
        // Branch 1: currentUser is null
        ResponseEntity<?> response1 = companyUserController.updateUserWage(999, 404, 20.0);
        assertEquals(HttpStatus.NOT_FOUND, response1.getStatusCode());
        
        // Branch 2: targetUser is null
        ResponseEntity<?> response2 = companyUserController.updateUserWage(401, 999, 20.0);
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
        
        // Branch 3: Employee trying to update wage (forbidden)
        ResponseEntity<?> response3 = companyUserController.updateUserWage(404, 403, 20.0);
        assertEquals(HttpStatus.FORBIDDEN, response3.getStatusCode());
        
        // Branch 4: Manager trying to update wage (forbidden)
        ResponseEntity<?> response4 = companyUserController.updateUserWage(403, 404, 20.0);
        assertEquals(HttpStatus.FORBIDDEN, response4.getStatusCode());
        
        // Branch 5: Owner updating employee wage (allowed)
        ResponseEntity<?> response5 = companyUserController.updateUserWage(401, 404, 20.0);
        assertEquals(HttpStatus.OK, response5.getStatusCode());
        verify(userRepository).save(employeeUser);
        
        // Branch 6: Executive updating manager wage (allowed)
        ResponseEntity<?> response6 = companyUserController.updateUserWage(402, 403, 25.0);
        assertEquals(HttpStatus.OK, response6.getStatusCode());
        verify(userRepository).save(managerUser);
        
        // Branch 7: Owner trying to update owner wage (not allowed)
        ResponseEntity<?> response7 = companyUserController.updateUserWage(401, 405, 30.0);
        assertEquals(HttpStatus.BAD_REQUEST, response7.getStatusCode());
        verify(userRepository, never()).save(ownerTargetUser);
    }

    @Test
    void testUpdateUserInfo_BranchCoverage() {
        // Setup for all branches in updateUserInfo
        User ownerUser = new User("Owner", "Test", "owner@test.com", "password", UserRole.OWNER);
        User executiveUser = new User("Executive", "Test", "exec@test.com", "password", UserRole.EXECUTIVE);
        User managerUser = new User("Manager", "Test", "manager@test.com", "password", UserRole.MANAGER);
        User employeeUser = new User("Employee", "Test", "employee@test.com", "password", UserRole.EMPLOYEE);
        
        ownerUser.setCompany(testCompany);
        executiveUser.setCompany(testCompany);
        managerUser.setCompany(testCompany);
        employeeUser.setCompany(testCompany);
        
        when(userRepository.findById(501)).thenReturn(ownerUser);
        when(userRepository.findById(502)).thenReturn(executiveUser);
        when(userRepository.findById(503)).thenReturn(managerUser);
        when(userRepository.findById(504)).thenReturn(employeeUser);
        when(userRepository.findById(999)).thenReturn(null);
        
        Map<String, String> basicInfo = Map.of(
            "firstName", "Updated",
            "lastName", "User",
            "email", "updated@test.com"
        );
        
        Map<String, String> infoWithHourlyRate = new HashMap<>(basicInfo);
        infoWithHourlyRate.put("hourlyRate", "25.0");
        
        Map<String, String> infoWithInvalidRate = new HashMap<>(basicInfo);
        infoWithInvalidRate.put("hourlyRate", "invalid");
        
        Map<String, String> completeInfo = new HashMap<>(basicInfo);
        completeInfo.put("password", "newpassword");
        completeInfo.put("role", "MANAGER");
        completeInfo.put("streetAddress", "123 Main St");
        completeInfo.put("city", "Anytown");
        completeInfo.put("state", "CA");
        completeInfo.put("zipCode", "12345");
        completeInfo.put("country", "USA");
        completeInfo.put("ssn", "123-45-6789");
        
        // Branch 1: currentUser is null
        ResponseEntity<?> response1 = companyUserController.updateUserInfo(999, 504, basicInfo);
        assertEquals(HttpStatus.NOT_FOUND, response1.getStatusCode());
        
        // Branch 2: targetUser is null
        ResponseEntity<?> response2 = companyUserController.updateUserInfo(501, 999, basicInfo);
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
        
        // Branch 3: Employee trying to update another user (not allowed)
        ResponseEntity<?> response3 = companyUserController.updateUserInfo(504, 503, basicInfo);
        assertEquals(HttpStatus.BAD_REQUEST, response3.getStatusCode());
        
        // Branch 4: Employee updating self (allowed)
        ResponseEntity<?> response4 = companyUserController.updateUserInfo(504, 504, basicInfo);
        assertEquals(HttpStatus.OK, response4.getStatusCode());
        verify(userRepository).save(employeeUser);
        
        // Branch 5: Owner updating any user (allowed)
        ResponseEntity<?> response5 = companyUserController.updateUserInfo(501, 504, completeInfo);
        assertEquals(HttpStatus.OK, response5.getStatusCode());
        verify(userRepository, times(2)).save(employeeUser);
        
        // Branch 6: Manager updating any user (allowed)
        ResponseEntity<?> response6 = companyUserController.updateUserInfo(503, 504, basicInfo);
        assertEquals(HttpStatus.OK, response6.getStatusCode());
        verify(userRepository, times(3)).save(employeeUser);
        
        // Branch 7: Owner updating hourly rate (allowed)
        ResponseEntity<?> response7 = companyUserController.updateUserInfo(501, 504, infoWithHourlyRate);
        assertEquals(HttpStatus.OK, response7.getStatusCode());
        verify(userRepository, times(4)).save(employeeUser);
        
        // Branch 8: Employee trying to update hourly rate (ignored)
        ResponseEntity<?> response8 = companyUserController.updateUserInfo(504, 504, infoWithHourlyRate);
        assertEquals(HttpStatus.OK, response8.getStatusCode());
        verify(userRepository, times(5)).save(employeeUser);
        
        // Branch 9: Invalid hourly rate format
        ResponseEntity<?> response9 = companyUserController.updateUserInfo(501, 504, infoWithInvalidRate);
        assertEquals(HttpStatus.BAD_REQUEST, response9.getStatusCode());
    }

    @Test
    void testGetUserInfo_BranchCoverage() {
        // Setup for all branches in getUserInfo
        User ownerUser = new User("Owner", "Test", "owner@test.com", "password", UserRole.OWNER);
        User employeeUser = new User("Employee", "Test", "employee@test.com", "password", UserRole.EMPLOYEE);
        User otherCompanyUser = new User("Other", "User", "other@test.com", "password", UserRole.EMPLOYEE);
        
        Company otherCompany = new Company("Other Company");
        
        ownerUser.setCompany(testCompany);
        employeeUser.setCompany(testCompany);
        otherCompanyUser.setCompany(otherCompany);
        
        employeeUser.setStreetAddress("123 Main St");
        employeeUser.setCity("Anytown");
        employeeUser.setState("CA");
        employeeUser.setZipCode("12345");
        employeeUser.setCountry("USA");
        employeeUser.setSsn("123-45-6789");
        employeeUser.setHourlyRate(15.0);
        
        when(userRepository.findById(601)).thenReturn(ownerUser);
        when(userRepository.findById(604)).thenReturn(employeeUser);
        when(userRepository.findById(605)).thenReturn(otherCompanyUser);
        when(userRepository.findById(999)).thenReturn(null);
        
        // Branch 1: currentUser is null
        ResponseEntity<?> response1 = companyUserController.getUserInfo(999, 604);
        assertEquals(HttpStatus.NOT_FOUND, response1.getStatusCode());
        
        // Branch 2: targetUser is null
        ResponseEntity<?> response2 = companyUserController.getUserInfo(601, 999);
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
        
        // Branch 3: Users from different companies
        ResponseEntity<?> response3 = companyUserController.getUserInfo(601, 605);
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        
        // Branch 4: Valid request - same company
        ResponseEntity<?> response4 = companyUserController.getUserInfo(601, 604);
        assertEquals(HttpStatus.OK, response4.getStatusCode());
        
        // Verify response contains all user info
        Map<?, ?> userInfo = (Map<?, ?>) response4.getBody();
        assertEquals("123 Main St", userInfo.get("streetAddress"));
        assertEquals("Anytown", userInfo.get("city"));
        assertEquals("CA", userInfo.get("state"));
        assertEquals("12345", userInfo.get("zipCode"));
        assertEquals("USA", userInfo.get("country"));
        assertEquals("123-45-6789", userInfo.get("ssn"));
        assertEquals("15.0", userInfo.get("hourlyRate"));
    }
}