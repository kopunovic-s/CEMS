package com._project._project;

import com._project._project.Company.Company;
import com._project._project.Company.CompanyRepository;
import com._project._project.User.User;
import com._project._project.User.UserRepository;
import com._project._project.User.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com._project._project.Authentication.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthenticationControllerTests {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    // Test data
    private Company testCompany;
    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeAll
    void setupTestData() {
        testCompany = new Company("Test Company");
        testCompany.setId(1L);

        testUser = new User("John", "Doe", "john@test.com", "password", UserRole.OWNER);
        testUser.setCompany(testCompany);

        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setCompanyName("Test Company");
        registerRequest.setEmail("john@test.com");
        registerRequest.setPassword("password");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@test.com");
        loginRequest.setPassword("password");
        loginRequest.setCompanyName("Test Company");
        loginRequest.setCompanyId(1L);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        when(authenticationService.registerUser(any(RegisterRequest.class))).thenReturn(testUser);
        User response = authenticationController.register(registerRequest);
        
        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(authenticationService).registerUser(any(RegisterRequest.class));
    }

    @Test
    void testRegisterUser_NullRequest() {
        when(authenticationService.registerUser(null)).thenThrow(new RuntimeException("Invalid request"));
        
        assertThrows(RuntimeException.class, () -> {
            authenticationController.register(null);
        });
    }

    @Test
    void testRegisterUser_EmptyFields() {
        RegisterRequest emptyRequest = new RegisterRequest();
        when(authenticationService.registerUser(emptyRequest)).thenThrow(new RuntimeException("Empty fields"));
        
        assertThrows(RuntimeException.class, () -> {
            authenticationController.register(emptyRequest);
        });
    }

    @Test
    void testLoginUser_Success() {
        when(authenticationService.loginUser(any(LoginRequest.class))).thenReturn(testUser);
        User response = authenticationController.logIn(loginRequest);
        
        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(authenticationService).loginUser(any(LoginRequest.class));
    }

    @Test
    void testLoginUser_NullRequest() {
        when(authenticationService.loginUser(null)).thenThrow(new RuntimeException("Invalid login request"));
        
        assertThrows(RuntimeException.class, () -> {
            authenticationController.logIn(null);
        });
    }

    @Test
    void testLoginUser_WrongCompany() {
        LoginRequest wrongCompanyRequest = new LoginRequest();
        wrongCompanyRequest.setCompanyId(999L);
        when(authenticationService.loginUser(wrongCompanyRequest))
            .thenThrow(new RuntimeException("Company not found"));
        
        assertThrows(RuntimeException.class, () -> {
            authenticationController.logIn(wrongCompanyRequest);
        });
    }

    @Test
    void testLoginUser_WrongCredentials() {
        LoginRequest wrongCredentialsRequest = new LoginRequest();
        wrongCredentialsRequest.setEmail("wrong@email.com");
        wrongCredentialsRequest.setPassword("wrongpass");
        when(authenticationService.loginUser(wrongCredentialsRequest))
            .thenThrow(new RuntimeException("Invalid credentials"));
        
        assertThrows(RuntimeException.class, () -> {
            authenticationController.logIn(wrongCredentialsRequest);
        });
    }

    @Test
    void testLoginUser_CompanyNameMismatch() {
        LoginRequest mismatchRequest = new LoginRequest();
        mismatchRequest.setCompanyId(1L);
        mismatchRequest.setCompanyName("Wrong Company");
        when(authenticationService.loginUser(mismatchRequest))
            .thenThrow(new RuntimeException("Company name mismatch"));
        
        assertThrows(RuntimeException.class, () -> {
            authenticationController.logIn(mismatchRequest);
        });
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setEmail("john@test.com");
        when(authenticationService.registerUser(duplicateRequest))
            .thenThrow(new RuntimeException("Email already exists"));
        
        assertThrows(RuntimeException.class, () -> {
            authenticationController.register(duplicateRequest);
        });
    }

    @Test
    void testLoginUser_EmptyFields() {
        LoginRequest emptyRequest = new LoginRequest();
        when(authenticationService.loginUser(emptyRequest))
            .thenThrow(new RuntimeException("Empty fields"));
        
        assertThrows(RuntimeException.class, () -> {
            authenticationController.logIn(emptyRequest);
        });
    }
}