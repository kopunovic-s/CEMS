package com._project._project.ReportGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportGeneratorController {

    @Autowired
    private ReportGeneratorService reportGeneratorService;

    /*
    Get initial data for reports to display on frontend as clickable objects, use second GET request
    to display a specific Project Report PDF based on the Report id.
     */
    @GetMapping("/get-reports-company/{currentUser_id}")
    public ResponseEntity<List<ReportGenerator>> getCompanyReportsList(@PathVariable long currentUser_id) {
        List<ReportGenerator> companyReports = reportGeneratorService.getCompanyReports(currentUser_id);
        return ResponseEntity.ok(companyReports);
    }

    /*
    Use this GET request to request the actual PDF based on the passed report_id,
    which you should have from the list of company reports you get from the
    above GET request ^^^^^.
     */
    @GetMapping("/get-report/{report_id}")
    public ResponseEntity<byte[]> getReport(@PathVariable long report_id) {
        byte[] report = reportGeneratorService.getReportById(report_id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=report.pdf")
                .body(report);
    }


    /*
    Endpoint for creating a report, sends bytecode to the frontend to immediately display
    the report after generation.
     */
    @PostMapping("/create-report/{currentUser_id}/{project_id}")
    public ResponseEntity<byte[]> createProjectReport(@PathVariable long currentUser_id, @PathVariable long project_id) {
        byte[] report = reportGeneratorService.createReport(currentUser_id, project_id);
        return ResponseEntity.ok()
                .header("Content-type", "application/pdf")
                .header("Content-Disposition", "inline; filename=report.pdf")
                .body(report);
    }

    @DeleteMapping("/delete-report/{report_id}")
    public ResponseEntity<String> deleteReport(@PathVariable long report_id) {
        String response = reportGeneratorService.deleteReport(report_id);
        return ResponseEntity.ok(response);
    }
}
