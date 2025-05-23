package com._project._project.Project.SalesData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
public class SalesDataController {

    @Autowired
    private SalesDataService salesDataService;

    /*
    Returns all SalesData for all projects combined within a company
     */
    @GetMapping(path = "/get-data-company/{currentUser_id}")
    public List<SalesData> getCompanyData(@PathVariable long currentUser_id) {
        return salesDataService.getCompanySalesData(currentUser_id);
    }

    /*
    Returns SalesData for a specific project, using user_id just to verify they belong to same company
     */
    @GetMapping(path = "/get-data-project/{currentUser_id}/{project_id}")
    public List<SalesData> getProjectData(@PathVariable long currentUser_id, @PathVariable long project_id) {
        return salesDataService.getProjectSalesData(currentUser_id, project_id);
    }

    /*
    POST method for creating sales data for a specific project. Using currentUser_id
    to verify correct company with Project. project_id used for identifying proper project, and
    a SalesData object to create a new object on the backend to be added to Project data array
     */
    @PostMapping(path = "/post-data/{currentUser_id}/{project_id}")
    public String createSalesData(@PathVariable long currentUser_id, @PathVariable long project_id, @RequestBody SalesData data) {
        return salesDataService.createSalesData(currentUser_id, project_id, data);
    }

    /*
    DELETE method using currentUser_id to verify proper user, and salesData_id
    to identify which data to remove.
     */
    @DeleteMapping(path = "/delete-data/{currentUser_id}/{salesData_id}")
    public String removeSalesData(@PathVariable long currentUser_id, @PathVariable long salesData_id) {
        return salesDataService.removeSalesData(currentUser_id, salesData_id);
    }

    /*
    Updating existing sales data. Use currentUser_id for verifying user, use
    salesData_id to find proper data in repo to update. Use data object to update parameters.
     */
    @PutMapping(path = "/put-data/{currentUser_id}/{salesData_id}")
    public String updateSalesData(@PathVariable long currentUser_id, @PathVariable long salesData_id, @RequestBody SalesData data) {
        return salesDataService.updateSalesData(currentUser_id, salesData_id, data);
    }
}
