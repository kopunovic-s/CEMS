package com._project._project;

import com._project._project.W2.*;
import com._project._project.User.*;
import com._project._project.Company.*;
import com._project._project.TimeCard.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.io.IOException;

public class W2Tests {

    @Mock
    private W2Repository w2Repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TimeCardRepository timeCardRepository;

    @Mock
    private W2Service w2Service;

    @InjectMocks
    private W2Controller w2Controller;

    private User testEmployee;
    private User testOwner;
    private Company testCompany;
    private W2 testW2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test company
        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Test Corp");
        testCompany.setEin("12-3456789");
        testCompany.setStreetAddress("123 Test St");
        testCompany.setCity("Testville");
        testCompany.setState("TS");
        testCompany.setZipCode("12345");

        // Setup test employee
        testEmployee = new User();
        testEmployee.setId(1L);
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setSsn("123-45-6789");
        testEmployee.setStreetAddress("456 Employee St");
        testEmployee.setCity("Empville");
        testEmployee.setState("ES");
        testEmployee.setZipCode("67890");
        testEmployee.setRole(UserRole.EMPLOYEE);
        testEmployee.setCompany(testCompany);

        // Setup test owner
        testOwner = new User();
        testOwner.setId(2L);
        testOwner.setRole(UserRole.OWNER);
        testOwner.setCompany(testCompany);

        // Setup test W2
        testW2 = new W2();
        testW2.setId(1L);
        testW2.setEmployee(testEmployee);
        testW2.setCompany(testCompany);
        testW2.setYear(2024);
        testW2.setWagesTipsOtherComp(50000.0);
        testW2.setFederalIncomeTax(7500.0);
    }

    @Test
    void getW2_Unauthorized() {
        User unauthorizedUser = new User();
        unauthorizedUser.setId(3L);
        unauthorizedUser.setRole(UserRole.EMPLOYEE);

        when(userRepository.findById(unauthorizedUser.getId())).thenReturn(unauthorizedUser);
        when(userRepository.findById(testEmployee.getId())).thenReturn(testEmployee);

        ResponseEntity<?> response = w2Controller.getW2(unauthorizedUser.getId(), testEmployee.getId(), 2024);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void generateAndSaveW2_Success() throws IOException {
        when(userRepository.findById(testOwner.getId())).thenReturn(testOwner);
        when(userRepository.findById(testEmployee.getId())).thenReturn(testEmployee);
        when(w2Service.generateAndSaveW2(any(), any(), anyInt(), anyBoolean())).thenReturn(testW2);

        ResponseEntity<?> response = w2Controller.generateAndSaveW2(testOwner.getId(), testEmployee.getId(), 2024);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testW2, response.getBody());
    }

    @Test
    void deleteW2_Success() {
        when(userRepository.findById(testOwner.getId())).thenReturn(testOwner);
        doNothing().when(w2Service).deleteW2(testW2.getId());

        ResponseEntity<?> response = w2Controller.deleteW2(testOwner.getId(), testW2.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(w2Service).deleteW2(testW2.getId());
    }

    @Test
    void deleteW2_Unauthorized() {
        when(userRepository.findById(testEmployee.getId())).thenReturn(testEmployee);

        ResponseEntity<?> response = w2Controller.deleteW2(testEmployee.getId(), testW2.getId());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(w2Service, never()).deleteW2(any());
    }

} 