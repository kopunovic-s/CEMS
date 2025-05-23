package com._project._project;

import com._project._project.Project.SalesData.SalesData;
import com._project._project.Project.SalesData.SalesDataService;
import com._project._project.Project.Project;
import com._project._project.User.User;
import com._project._project.User.UserRepository;
import com._project._project.Project.ProjectRepository;
import com._project._project.Project.SalesData.SalesDataRepository;
import com._project._project.Company.Company;
import com._project._project.User.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SalesDataServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SalesDataRepository salesDataRepository;

    @InjectMocks
    private SalesDataService salesDataService;

    private User testUser;
    private Project testProject;
    private SalesData salesData;
    private Company testCompany;

    @BeforeAll
    void setupTestData() {
        testCompany = new Company("Test Company");
        testUser = new User("Alice", "Employee", "alice@test.com", "password", UserRole.EMPLOYEE);
        testUser.setCompany(testCompany);

        testProject = new Project("Test Project", "Test Description", LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        testProject.setCompany(testCompany);
        salesData = new SalesData(testProject, 1000L, 500L, LocalDate.now());
        testProject.addSalesData(salesData);
        testCompany.addProject(testProject);
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(userRepository.findById(1L)).thenReturn(testUser);
        when(projectRepository.findById(1L)).thenReturn(testProject);
        when(salesDataRepository.findById(1L)).thenReturn(salesData);
        when(salesDataRepository.save(any(SalesData.class))).thenReturn(salesData);
    }

    @Test
    void testGetCompanySalesData_Success() {
        List<SalesData> result = salesDataService.getCompanySalesData(1L);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1000L, result.get(0).getIncome());
    }


    @Test
    void testGetProjectSalesData_Success() {
        List<SalesData> result = salesDataService.getProjectSalesData(1L, 1L);
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals(2000L, result.get(0).getIncome());
    }

    @Test
    void testGetProjectSalesData_InvalidUser() {
        when(userRepository.findById(1L)).thenReturn(null);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            salesDataService.getProjectSalesData(1L, 1L);
        });
        assertEquals("Invalid company or user Id.", exception.getMessage());
    }

    @Test
    void testCreateSalesData_Success() {
        String result = salesDataService.createSalesData(1L, 1L, salesData);
        assertEquals("success", result);
        verify(salesDataRepository, times(1)).save(any(SalesData.class));
    }

    @Test
    void testCreateSalesData_InvalidUser() {
        when(userRepository.findById(1L)).thenReturn(null);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            salesDataService.createSalesData(1L, 1L, salesData);
        });
        assertEquals("Invalid company or user Id.", exception.getMessage());
    }

    @Test
    void testRemoveSalesData_Success() {
        String result = salesDataService.removeSalesData(1L, 1L);
        assertEquals("success", result);
        verify(salesDataRepository, times(1)).deleteById(1L);
    }

    @Test
    void testRemoveSalesData_InvalidUser() {
        when(userRepository.findById(1L)).thenReturn(null);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            salesDataService.removeSalesData(1L, 1L);
        });
        assertEquals("Invalid sales data or user Id.", exception.getMessage());
    }

    @Test
    void testUpdateSalesData_Success() {
        salesData.setIncome(2000L);
        salesData.setExpenses(1000L);
        String result = salesDataService.updateSalesData(1L, 1L, salesData);
        assertEquals("success", result);
        assertEquals(2000L, salesData.getIncome());
        assertEquals(1000L, salesData.getExpenses());
        verify(salesDataRepository, times(1)).save(salesData);
    }

    @Test
    void testUpdateSalesData_InvalidUser() {
        when(userRepository.findById(1L)).thenReturn(null);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            salesDataService.updateSalesData(1L, 1L, salesData);
        });
        assertEquals("Invalid sales data or user Id.", exception.getMessage());
    }

    @Test
    void testVerifyUserAndProject_Valid() {
        assertDoesNotThrow(() -> {
            salesDataService.getProjectSalesData(1L, 1L);
        });
    }

    @Test
    void testVerifyUserAndProject_InvalidUser() {
        when(userRepository.findById(1L)).thenReturn(null);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            salesDataService.getProjectSalesData(1L, 1L);
        });
        assertEquals("Invalid company or user Id.", exception.getMessage());
    }

    @Test
    void testVerifyUserAndProject_InvalidProject() {
        when(projectRepository.findById(1L)).thenReturn(null);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            salesDataService.getProjectSalesData(1L, 1L);
        });
        assertEquals("Invalid company or user Id.", exception.getMessage());
    }
}
