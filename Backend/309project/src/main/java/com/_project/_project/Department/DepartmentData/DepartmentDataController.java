package com._project._project.Department.DepartmentData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departmentData")
public class DepartmentDataController {

    @Autowired
    private DepartmentDataService departmentDataService;

    /*
    Returns a list of all data related to a specific department, based on department_id
     */
    @GetMapping(path = "/get-departmentData-list/{department_id}")
    public List<DepartmentData> getDepartmentDataList(@PathVariable long department_id){
        return departmentDataService.getDepartmentDataList(department_id);
    }

    /*
    Returns a list of a specific data point of DepartmentData
     */
    @GetMapping(path =  "/get-departmentData/{departmentData_id}")
    public DepartmentData getDepartmentData(@PathVariable long departmentData_id){
        return departmentDataService.getDepartmentData(departmentData_id);
    }

    /*
    Basic deletion endpoint, just pass the departmentData_id of the one you want deleted
     */
    @DeleteMapping(path = "/delete-departmentData/{departmentData_id}")
    public String deleteDepartmentData(@PathVariable long departmentData_id){
        return departmentDataService.deleteDepartmentData(departmentData_id);
    }
}
