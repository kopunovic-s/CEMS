package com._project._project;

import com._project._project.Project.SalesData.SalesData;
import com._project._project.Project.SalesData.SalesDataController;
import com._project._project.Project.SalesData.SalesDataService;
import com._project._project.Project.Project;
import com._project._project.User.User;
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
public class SalesDataTests {

    @Mock
    private SalesDataService salesDataService;

    @InjectMocks
    private SalesDataController salesDataController;

    private User testUser;
    private Project testProject;
    private SalesData salesData;

    @BeforeAll
    void setupTestData() {
        testUser = new User("Alice", "Employee", "alice@test.com", "password", UserRole.EMPLOYEE);
        testProject = new Project("Test Project", "Test Description", LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        salesData = new SalesData(testProject, 1000L, 500L, LocalDate.now());
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(salesDataService.createSalesData(1L, 1L, salesData)).thenReturn("Success");
        when(salesDataService.removeSalesData(1L, 1L)).thenReturn("Success");
        when(salesDataService.updateSalesData(1L, 1L, salesData)).thenReturn("Success");
        when(salesDataService.getCompanySalesData(1L)).thenReturn(List.of(salesData));
        when(salesDataService.getProjectSalesData(1L, 1L)).thenReturn(List.of(salesData));
    }

    @Test
    void testCreateSalesData() {
        String result = salesDataController.createSalesData(1L, 1L, salesData);
        assertEquals("Success", result);
    }

    @Test
    void testRemoveSalesData() {
        String result = salesDataController.removeSalesData(1L, 1L);
        assertEquals("Success", result);
    }

    @Test
    void testUpdateSalesData() {
        String result = salesDataController.updateSalesData(1L, 1L, salesData);
        assertEquals("Success", result);
    }

    @Test
    void testGetCompanySalesData() {
        List<SalesData> result = salesDataController.getCompanyData(1L);
        assertEquals(1, result.size());
        assertEquals(1500L, result.get(0).getIncome());
    }

    @Test
    void testGetProjectSalesData() {
        List<SalesData> result = salesDataController.getProjectData(1L, 1L);
        assertEquals(1, result.size());
        assertEquals(700L, result.get(0).getExpenses());
    }

    @Test
    void testSalesDataEntityMethods() {
        salesData.setIncome(1500L);
        assertEquals(1500L, salesData.getIncome());

        salesData.setExpenses(700L);
        assertEquals(700L, salesData.getExpenses());

        long calculatedRevenue = 1500L - 700L;
        assertEquals(calculatedRevenue, salesData.getRevenue());

        LocalDate newDate = LocalDate.now().plusDays(1);
        salesData.setDate(newDate);
        assertEquals(newDate, salesData.getDate());
    }
}
