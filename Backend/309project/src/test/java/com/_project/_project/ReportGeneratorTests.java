package com._project._project;

import com._project._project.ReportGenerator.*;
import com._project._project.Company.Company;
import com._project._project.Project.Project;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReportGeneratorTests {

    @Mock
    private ReportGeneratorService reportGeneratorService;

    @InjectMocks
    private ReportGeneratorController reportGeneratorController;

    private ReportGenerator report;
    private Company company;
    private User user;

    @BeforeAll
    void setupTestData() {
        company = new Company("Test Company");
        user = new User("John", "Doe", "john@test.com", "password", UserRole.EXECUTIVE);
        report = new ReportGenerator(company, 1L, "Test Report", LocalDate.now(), "John Doe", 1L, "Project A", 1L, new ArrayList<>());
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(reportGeneratorService.getCompanyReports(1L)).thenReturn(List.of(report));
        when(reportGeneratorService.getReportById(1L)).thenReturn(new byte[0]);
        when(reportGeneratorService.createReport(1L, 1L)).thenReturn(new byte[0]);
        when(reportGeneratorService.deleteReport(1L)).thenReturn("success");

        // Edge cases: Null returns and exceptions
        when(reportGeneratorService.getCompanyReports(0L)).thenThrow(new RuntimeException("User not found"));
        when(reportGeneratorService.getReportById(0L)).thenThrow(new RuntimeException("Report not found"));
        when(reportGeneratorService.createReport(0L, 0L)).thenThrow(new RuntimeException("Invalid report creation"));
        when(reportGeneratorService.deleteReport(0L)).thenThrow(new RuntimeException("Report not found"));
    }

    @Test
    void testGetCompanyReportsList_Success() {
        var response = reportGeneratorController.getCompanyReportsList(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(null, response.getBody().get(0).getFileName());
    }

    @Test
    void testGetCompanyReportsList_Failure() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reportGeneratorController.getCompanyReportsList(0L);
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetReport_Success() {
        var response = reportGeneratorController.getReport(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetReport_Failure() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reportGeneratorController.getReport(0L);
        });
        assertEquals("Report not found", exception.getMessage());
    }

    @Test
    void testCreateProjectReport_Success() {
        var response = reportGeneratorController.createProjectReport(1L, 1L);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateProjectReport_Failure() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reportGeneratorController.createProjectReport(0L, 0L);
        });
        assertEquals("Invalid report creation", exception.getMessage());
    }

    @Test
    void testDeleteReport_Success() {
        var response = reportGeneratorController.deleteReport(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("success", response.getBody());
    }

    @Test
    void testDeleteReport_Failure() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reportGeneratorController.deleteReport(0L);
        });
        assertEquals("Report not found", exception.getMessage());
    }

    @Test
    void testReportEntityMethods() {
        report.setFileName("Updated Report");
        assertEquals("Updated Report", report.getFileName());

        LocalDate newDate = LocalDate.now().minusDays(1);
        report.setReportDate(newDate);
        assertEquals(newDate, report.getReportDate());

        report.setEmployeeName("Jane Doe");
        assertEquals("Jane Doe", report.getEmployeeName());

        report.setProjectName("Project B");
        assertEquals("Project B", report.getProjectName());

        report.setProjectId(2L);
        assertEquals(2L, report.getProjectId());

        report.setEmployeeId(2L);
        assertEquals(2L, report.getEmployeeId());

        // Test graph data
        List<ReportGraphData> graphDataList = new ArrayList<>();
        graphDataList.add(new ReportGraphData(LocalDate.now(), 1000, 500, 500));
        report.setGraphDataList(graphDataList);
        assertEquals(1, report.getGraphDataList().size());
    }

    @Test
    void testNullReportData() {
        report.setFileName(null);
        assertNull(report.getFileName());

        report.setEmployeeName(null);
        assertNull(report.getEmployeeName());

        report.setProjectName(null);
        assertNull(report.getProjectName());

        report.setGraphDataList(null);
        assertNull(report.getGraphDataList());
    }

    @Test
    void testEdgeCases() {
        report.setEmployeeId(-1L);
        assertEquals(-1L, report.getEmployeeId());

        report.setProjectId(-1L);
        assertEquals(-1L, report.getProjectId());
    }

    @Test
    void testGraphDataObject() {
        ReportGraphData data = new ReportGraphData(LocalDate.now(), 1000, 500, 500);
        assertEquals(1000, data.income());
        assertEquals(500, data.expenses());
        assertEquals(500, data.revenue());
    }
}
