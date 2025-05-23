package com._project._project;

import com._project._project.Company.*;
import com._project._project.User.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;
import com._project._project.Project.Project;
///asdadasdadsasdas
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CompanyTests {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompanyController companyController;

    // Test data
    private User ownerUser;
    private User executiveUser;
    private User managerUser;
    private User employeeUser;
    private Company testCompany;
    private List<User> companyUsers;

    @BeforeAll
    void setupTestData() {
        // Create test company
        testCompany = new Company("Test Company");
        testCompany.setId(1L);
        testCompany.setEin("12-3456789");
        testCompany.setStreetAddress("123 Test St");
        testCompany.setCity("Test City");
        testCompany.setState("TS");
        testCompany.setZipCode("12345");
        testCompany.setCountry("Test Country");
        
        // Create test users
        ownerUser = new User("John", "Owner", "owner@test.com", "password", UserRole.OWNER);
        ownerUser.setCompany(testCompany);
        
        executiveUser = new User("Jane", "Executive", "executive@test.com", "password", UserRole.EXECUTIVE);
        executiveUser.setCompany(testCompany);
        
        managerUser = new User("Bob", "Manager", "manager@test.com", "password", UserRole.MANAGER);
        managerUser.setCompany(testCompany);
        
        employeeUser = new User("Alice", "Employee", "employee@test.com", "password", UserRole.EMPLOYEE);
        employeeUser.setCompany(testCompany);
        
        // Set up company users list
        companyUsers = new ArrayList<>();
        companyUsers.add(ownerUser);
        companyUsers.add(executiveUser);
        companyUsers.add(managerUser);
        companyUsers.add(employeeUser);
        
        // Add users to company
        testCompany.setUsers(companyUsers);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up common mock behaviors
        when(userRepository.findById(1L)).thenReturn(ownerUser);
        when(userRepository.findById(2L)).thenReturn(executiveUser);
        when(userRepository.findById(3L)).thenReturn(managerUser);
        when(userRepository.findById(4L)).thenReturn(employeeUser);
        
        when(companyRepository.findById(1L)).thenReturn(testCompany);
        
        // Initialize repositories in controller
        ReflectionTestUtils.setField(companyController, "companyRepository", companyRepository);
        ReflectionTestUtils.setField(companyController, "userRepository", userRepository);
    }

    // ===== Company.java Tests =====

    @Test
    void testCompanyConstructor() {
        //setup
        User newUser = new User("New", "User", "new@test.com", "password", UserRole.EMPLOYEE);
        testCompany.addUser(newUser);

        String testLogo = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...";
        testCompany.setCompanyLogo(testLogo);

        //Verify
        assertEquals(testLogo, testCompany.getCompanyLogo());

        assertEquals(1L, testCompany.getId());
        assertEquals("12-3456789", testCompany.getEin());
        assertEquals("123 Test St", testCompany.getStreetAddress());
        assertEquals("Test City", testCompany.getCity());
        assertEquals("TS", testCompany.getState());
        assertEquals("12345", testCompany.getZipCode());
        assertEquals("Test Country", testCompany.getCountry());

        assertEquals(ownerUser, testCompany.getOwner());

        assertEquals(5, testCompany.getUsers().size());
        assertTrue(testCompany.getUsers().contains(newUser));
        assertEquals(testCompany, newUser.getCompany());

        assertTrue(testCompany.getUsers().contains(ownerUser));
        assertTrue(testCompany.getUsers().contains(executiveUser));
        assertTrue(testCompany.getUsers().contains(managerUser));
        assertTrue(testCompany.getUsers().contains(employeeUser));
        assertFalse(employeeUser.getCompany().getOwner().equals(employeeUser));
    }

    
    @Test
    void testEmptyCompanyConstructor() {
        // Setup
        Company emptyCompany = new Company();
        
        // Verify
        assertNull(emptyCompany.getName());
        assertNull(emptyCompany.getId());
        assertNotNull(emptyCompany.getUsers());
        assertNotNull(emptyCompany.getProjects());
        assertTrue(emptyCompany.getUsers().isEmpty());
        assertTrue(emptyCompany.getProjects().isEmpty());
        
    }
    
    @Test
    void testGetOwnerWithNoOwner() {
        //setup
        Company noOwnerCompany = new Company("No Owner Company");
        User employee1 = new User("Emp1", "Test", "emp1@test.com", "password", UserRole.EMPLOYEE);
        User employee2 = new User("Emp2", "Test", "emp2@test.com", "password", UserRole.EMPLOYEE);
        
        noOwnerCompany.addUser(employee1);
        noOwnerCompany.addUser(employee2);
        
        // Verify
        assertNull(noOwnerCompany.getOwner());
        assertEquals(2, noOwnerCompany.getUsers().size());
    }
    
    // ===== CompanyController.java Tests =====
    
    @Test
    void testGetAllCompanies() {
        // Setup
        List<Company> companies = new ArrayList<>();
        companies.add(testCompany);
        when(companyRepository.findAll()).thenReturn(companies);
        
        // Execute
        List<Company> result = companyController.getAllCompanies();
        
        // Verify
        assertEquals(1, result.size());
        assertEquals(testCompany, result.get(0));
        verify(companyRepository).findAll();
    }
    
    @Test
    void testGetCompanyById() {
        // Setup
        when(companyRepository.findById(1)).thenReturn(testCompany);
        
        // Execute
        Company result = companyController.getCompanyById(1);
        
        // Verify
        assertEquals(testCompany, result);
        verify(companyRepository).findById(1);
    }
    
    @Test
    void testGetCompanyUsers() {
        // Execute
        List<User> result = companyController.getCompanyUsers(1L);
        
        // Verify
        assertEquals(4, result.size());
        assertTrue(result.contains(ownerUser));
        assertTrue(result.contains(executiveUser));
        assertTrue(result.contains(managerUser));
        assertTrue(result.contains(employeeUser));
        verify(companyRepository).findById(1L);
    }
    
    @Test
    void testGetCompanyOwner() {
        // Execute
        User result = companyController.getCompanyOwner(1L);
        
        // Verify
        assertEquals(ownerUser, result);
        verify(companyRepository).findById(1L);
    }
    
    @Test
    void testCreateCompany_Success() {
        // Setup
        Company newCompany = new Company("New Company");
        when(companyRepository.save(any(Company.class))).thenReturn(newCompany);
        
        // Execute
        String result = companyController.createCompany(newCompany);
        
        // Verify
        assertEquals("Success", result);
        verify(companyRepository).save(newCompany);
    }
    
    @Test
    void testCreateCompany_Failure() {
        // Execute
        String result = companyController.createCompany(null);
        
        // Verify
        assertEquals("Failure", result);
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void testUpdateCompany_CompanyNotFound() {
        // Setup
        Company updatedCompany = new Company("Updated Company");
        updatedCompany.setId(999L);
        when(companyRepository.findById(999)).thenReturn(null);
        
        // Execute & Verify
        Exception exception = assertThrows(RuntimeException.class, () -> {
            companyController.updateCompany(999, updatedCompany);
        });
        
        assertEquals("Company ID does not exist", exception.getMessage());
        verify(companyRepository).findById(999);
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void testDeleteCompany_Success() {
        // Setup
        when(companyRepository.findById(1)).thenReturn(testCompany);
        
        // Execute
        String result = companyController.deleteCompany(1, ownerUser);
        
        // Verify
        assertEquals("Success", result);
        verify(companyRepository).deleteById(1);
    }
    
    @Test
    void testDeleteCompany_CompanyNotFound() {
        // Setup
        when(companyRepository.findById(999)).thenReturn(null);
        
        // Execute
        String result = companyController.deleteCompany(999, ownerUser);
        
        // Verify
        assertEquals("Failure", result);
        verify(companyRepository).findById(999);
        verify(companyRepository, never()).deleteById(anyInt());
    }
    
    @Test
    void testDeleteCompany_NotOwner() {
        // Setup
        when(companyRepository.findById(1)).thenReturn(testCompany);
        
        // Execute & Verify
        Exception exception = assertThrows(RuntimeException.class, () -> {
            companyController.deleteCompany(1, employeeUser);
        });
        
        assertEquals("You do not have permission to delete this company", exception.getMessage());
    }
    
    @Test
    void testUpdateCompanyInfo_Success() {
        // Setup
        Map<String, String> info = new HashMap<>();
        info.put("streetAddress", "456 New St");
        info.put("city", "New City");
        info.put("state", "NS");
        info.put("zipCode", "54321");
        info.put("country", "New Country");
        info.put("ein", "98-7654321");
        
        // Execute
        ResponseEntity<?> response = companyController.updateCompanyInfo(1L, info);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("456 New St", testCompany.getStreetAddress());
        assertEquals("New City", testCompany.getCity());
        assertEquals("NS", testCompany.getState());
        assertEquals("54321", testCompany.getZipCode());
        assertEquals("New Country", testCompany.getCountry());
        assertEquals("98-7654321", testCompany.getEin());
        verify(userRepository).findById(1L);
        verify(companyRepository).save(testCompany);
    }
    
    @Test
    void testUpdateCompanyInfo_UserNotFound() {
        // Setup
        when(userRepository.findById(999L)).thenReturn(null);
        Map<String, String> info = new HashMap<>();
        
        // Execute
        ResponseEntity<?> response = companyController.updateCompanyInfo(999L, info);
        
        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository).findById(999L);
        verify(companyRepository, never()).save(any(Company.class));
    }
    
    @Test
    void testUpdateCompanyInfo_CompanyNotFound() {
        // Setup
        User userWithoutCompany = new User("No", "Company", "no@company.com", "password", UserRole.OWNER);
        when(userRepository.findById(5L)).thenReturn(userWithoutCompany);
        Map<String, String> info = new HashMap<>();
        
        // Execute
        ResponseEntity<?> response = companyController.updateCompanyInfo(5L, info);
        
        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository).findById(5L);
        verify(companyRepository, never()).save(any(Company.class));
    }
    
    @Test
    void testUpdateCompanyInfo_NotAuthorized() {
        // Setup
        Map<String, String> info = new HashMap<>();
        
        // Execute
        ResponseEntity<?> response = companyController.updateCompanyInfo(4L, info);
        
        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Only OWNER or EXECUTIVE can update company information", response.getBody());
        verify(userRepository).findById(4L);
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void testGetCompanyInfo_Success() {
        // Important: Reset the company state before this test
        testCompany.setStreetAddress("123 Test St");
        testCompany.setCity("Test City");
        testCompany.setState("TS");
        testCompany.setZipCode("12345");
        testCompany.setCountry("Test Country");
        testCompany.setEin("12-3456789");
        
        // Execute
        ResponseEntity<?> response = companyController.getCompanyInfo(1L);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) response.getBody();
        assertEquals("123 Test St", result.get("streetAddress"));
        assertEquals("Test City", result.get("city"));
        assertEquals("TS", result.get("state"));
        assertEquals("12345", result.get("zipCode"));
        assertEquals("Test Country", result.get("country"));
        assertEquals("12-3456789", result.get("ein"));
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetCompanyInfo_UserNotFound() {
        // Setup
        when(userRepository.findById(999L)).thenReturn(null);
        
        // Execute
        ResponseEntity<?> response = companyController.getCompanyInfo(999L);
        
        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository).findById(999L);
    }
    
    @Test
    void testGetCompanyInfo_CompanyNotFound() {
        // Setup
        User userWithoutCompany = new User("No", "Company", "no@company.com", "password", UserRole.OWNER);
        when(userRepository.findById(5L)).thenReturn(userWithoutCompany);
        
        // Execute
        ResponseEntity<?> response = companyController.getCompanyInfo(5L);
        
        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository).findById(5L);
    }
}