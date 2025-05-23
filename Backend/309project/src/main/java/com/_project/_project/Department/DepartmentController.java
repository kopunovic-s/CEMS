package com._project._project.Department;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    DepartmentService departmentService;

    /*
    Get a list of all departments for a company based on
    the currentUser_id
     */
    @GetMapping(path = "/get-departments/{currentUser_id}")
    public List<Department> getDepartments(@PathVariable int currentUser_id) {
        return departmentService.getAllDepartments(currentUser_id);
    }

    /*
    Returns a specific department based on the department_id
     */
    @GetMapping(path = "/get-department/{department_id}")
    public Department getDepartment(@PathVariable long department_id) {
        return departmentService.getDepartment(department_id);
    }

    /*
    Create a new department, just pass currentUser_id and the departmentName
    of the new department you want to creat
     */
    @PostMapping(path = "/create-department/{currentUser_id}/{departmentName}")
    public String createDepartment(@PathVariable long currentUser_id, @PathVariable String departmentName) {
        return departmentService.createDepartment(currentUser_id, departmentName);
    }


    /*
    Editing department endpoint, really the only thing we have to edit at
    the moment is the department name, so just pass it in the path
     */
    @PutMapping(path = "/update-department/{department_id}/{department_name}")
    public String updateDepartment(@PathVariable long department_id, @PathVariable String department_name) {
        return departmentService.updateDepartment(department_id, department_name);
    }

    /*
    Delete endpoint, based on department_id, so far has worked when tests
     */
    @DeleteMapping(path = "/remove-department/{department_id}")
    public String removeDepartment(@PathVariable long department_id) {
        return departmentService.removeDepartment(department_id);
    }
}
