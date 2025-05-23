package com._project._project;

import com._project._project.Authentication.*;
import com._project._project.Company.Company;
import com._project._project.Company.CompanyRepository;
import com._project._project.User.User;
import com._project._project.User.UserRepository;
import com._project._project.User.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthenticationTests {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ===== Tests for AuthenticationController.java =====


    // ===== Tests for AuthenticationService.java =====
    @Test
    void testRegisterUser() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setCompanyName("Test Company");
        request.setEmail("jane.doe@test.com");
        request.setPassword("password");

        Company company = new Company("Test Company");
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane.doe@test.com");

        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User registeredUser = authenticationService.registerUser(request);

        assertNotNull(registeredUser);
        assertEquals("Jane", registeredUser.getFirstName());
        assertEquals("Doe", registeredUser.getLastName());
        assertEquals("jane.doe@test.com", registeredUser.getEmail());
    }

    @Test
    void testLoginUser_Success() {
        // Setup
        LoginRequest request = new LoginRequest();
        request.setEmail("jane.doe@test.com");
        request.setPassword("password");
        request.setCompanyName("Test Company");
        request.setCompanyId(1L);

        Company company = new Company("Test Company");
        User user = new User();
        user.setEmail("jane.doe@test.com");
        user.setPassword("password");
        company.addUser(user);

        when(companyRepository.findById(1L)).thenReturn(company);

        // Execute
        User loggedInUser = authenticationService.loginUser(request);

        // Verify
        assertNotNull(loggedInUser);
        assertEquals("jane.doe@test.com", loggedInUser.getEmail());
        verify(companyRepository).findById(1L);
    }

    @Test
    void testLoginUser_CompanyNotFound() {
        // Setup
        LoginRequest request = new LoginRequest();
        request.setCompanyId(999L);
        
        when(companyRepository.findById(999L)).thenReturn(null);

        // Execute & Verify
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.loginUser(request);
        });
        assertEquals("Company not found", exception.getMessage());
        verify(companyRepository).findById(999L);
    }

    @Test
    void testLoginUser_CompanyNameMismatch() {
        // Setup
        LoginRequest request = new LoginRequest();
        request.setCompanyId(1L);
        request.setCompanyName("Wrong Company");

        Company company = new Company("Test Company");
        when(companyRepository.findById(1L)).thenReturn(company);

        // Execute & Verify
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.loginUser(request);
        });
        assertEquals("Company name does not match company ID.", exception.getMessage());
        verify(companyRepository).findById(1L);
    }

    @Test
    void testLoginUser_UserNotFound() {
        // Setup
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@test.com");
        request.setCompanyName("Test Company");
        request.setCompanyId(1L);

        Company company = new Company("Test Company");
        when(companyRepository.findById(1L)).thenReturn(company);

        // Execute & Verify
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.loginUser(request);
        });
        assertEquals("User with this email does not exist in company: Test Company", exception.getMessage());
        verify(companyRepository).findById(1L);
    }

    @Test
    void testLoginUser_IncorrectPassword() {
        // Setup
        LoginRequest request = new LoginRequest();
        request.setEmail("jane.doe@test.com");
        request.setPassword("wrongpassword");
        request.setCompanyName("Test Company");
        request.setCompanyId(1L);

        Company company = new Company("Test Company");
        User user = new User();
        user.setEmail("jane.doe@test.com");
        user.setPassword("correctpassword");
        company.addUser(user);

        when(companyRepository.findById(1L)).thenReturn(company);

        // Execute & Verify
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.loginUser(request);
        });
        assertEquals("Incorrect password.", exception.getMessage());
        verify(companyRepository).findById(1L);
    }

    // ===== Tests for Getter and Setter Validation =====
    @Test
    void testLoginRequestGettersAndSetters() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");
        request.setCompanyId(1L);
        request.setCompanyName("Test Company");

        assertEquals("test@test.com", request.getEmail());
        assertEquals("password", request.getPassword());
        assertEquals(1L, request.getCompanyId());
        assertEquals("Test Company", request.getCompanyName());
    }

    @Test
    void testRegisterRequestGettersAndSetters() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setCompanyName("Test Company");
        request.setEmail("jane.doe@test.com");
        request.setPassword("password");

        assertEquals("Jane", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertEquals("Test Company", request.getCompanyName());
        assertEquals("jane.doe@test.com", request.getEmail());
        assertEquals("password", request.getPassword());
    }
}
