package com._project._project;

import com._project._project.Department.Department;
import com._project._project.Department.DepartmentData.DepartmentData;
import com._project._project.Department.DepartmentData.DepartmentDataController;
import com._project._project.Department.DepartmentData.DepartmentDataService;
import com._project._project.Company.Company;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DepartmentDataTests {

    @Mock
    private DepartmentDataService departmentDataService;

    @InjectMocks
    private DepartmentDataController departmentDataController;

    private DepartmentData departmentData;
    private Department department;

    @BeforeAll
    void setupTestData() {
        department = new Department("Finance");
        departmentData = new DepartmentData(LocalDate.now(), BigDecimal.valueOf(1000), BigDecimal.valueOf(200));
        departmentData.setRevenue(BigDecimal.valueOf(800));
        department.addDepartmentData(departmentData);
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(departmentDataService.getDepartmentDataList(1L)).thenReturn(List.of(departmentData));
        when(departmentDataService.getDepartmentData(1L)).thenReturn(departmentData);
        when(departmentDataService.deleteDepartmentData(1L)).thenReturn("success");

        // Edge cases: Non-existing data
        when(departmentDataService.getDepartmentDataList(0L)).thenThrow(new RuntimeException("Department not found"));
        when(departmentDataService.getDepartmentData(0L)).thenThrow(new RuntimeException("Invalid department data id."));
        when(departmentDataService.deleteDepartmentData(0L)).thenThrow(new RuntimeException("Invalid department data id."));
    }

    @Test
    void testGetDepartmentDataList_Success() {
        var response = departmentDataController.getDepartmentDataList(1L);
        assertEquals(1, response.size());
        assertEquals(BigDecimal.valueOf(2000), response.get(0).getIncome());
        assertEquals(BigDecimal.valueOf(500), response.get(0).getExpense());
        assertEquals(BigDecimal.valueOf(1500), response.get(0).getRevenue());
    }

    @Test
    void testGetDepartmentDataList_Failure() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            departmentDataController.getDepartmentDataList(0L);
        });
        assertEquals("Department not found", exception.getMessage());
    }

    @Test
    void testGetDepartmentData_Success() {
        var response = departmentDataController.getDepartmentData(1L);
        assertEquals(BigDecimal.valueOf(1000), response.getIncome());
        assertEquals(BigDecimal.valueOf(200), response.getExpense());
        assertEquals(BigDecimal.valueOf(800), response.getRevenue());
    }

    @Test
    void testGetDepartmentData_Failure() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            departmentDataController.getDepartmentData(0L);
        });
        assertEquals("Invalid department data id.", exception.getMessage());
    }

    @Test
    void testDeleteDepartmentData_Success() {
        var response = departmentDataController.deleteDepartmentData(1L);
        assertEquals("success", response);
    }

    @Test
    void testDeleteDepartmentData_Failure() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            departmentDataController.deleteDepartmentData(0L);
        });
        assertEquals("Invalid department data id.", exception.getMessage());
    }

    @Test
    void testDepartmentDataEntityMethods() {
        departmentData.setIncome(BigDecimal.valueOf(1500));
        assertEquals(BigDecimal.valueOf(1500), departmentData.getIncome());

        departmentData.setExpense(BigDecimal.valueOf(500));
        assertEquals(BigDecimal.valueOf(500), departmentData.getExpense());

        departmentData.setRevenue(BigDecimal.valueOf(1000));
        assertEquals(BigDecimal.valueOf(1000), departmentData.getRevenue());

        departmentData.setDate(LocalDate.now().minusDays(1));
        assertEquals(LocalDate.now().minusDays(1), departmentData.getDate());

        departmentData.setId(2L);
        assertEquals(2L, departmentData.getId());

        departmentData.setDepartment(department);
        assertEquals(department, departmentData.getDepartment());
    }

    @Test
    void testDepartmentDataEdgeCases() {
        departmentData.setIncome(BigDecimal.ZERO);
        departmentData.setExpense(BigDecimal.ZERO);
        departmentData.setRevenue(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, departmentData.getIncome());
        assertEquals(BigDecimal.ZERO, departmentData.getExpense());
        assertEquals(BigDecimal.ZERO, departmentData.getRevenue());

        departmentData.setDate(null);
        assertNull(departmentData.getDate());

        departmentData.setDepartment(null);
        assertNull(departmentData.getDepartment());
    }

    @Test
    void testRevenueCalculation() {
        departmentData.setIncome(BigDecimal.valueOf(2000));
        departmentData.setExpense(BigDecimal.valueOf(500));
        departmentData.setRevenue(departmentData.getIncome().subtract(departmentData.getExpense()));
        assertEquals(BigDecimal.valueOf(1500), departmentData.getRevenue());
    }

    @Test
    void testDepartmentDataNullHandling() {
        departmentData.setIncome(null);
        departmentData.setExpense(null);
        departmentData.setRevenue(null);
        assertNull(departmentData.getIncome());
        assertNull(departmentData.getExpense());
        assertNull(departmentData.getRevenue());
    }
}
