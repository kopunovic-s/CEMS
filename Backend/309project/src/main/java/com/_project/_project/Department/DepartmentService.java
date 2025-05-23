package com._project._project.Department;

import com._project._project.User.User;
import com._project._project.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    private String success = "success";

    public List<Department> getAllDepartments(long currentUser_id) {
        User currentUser = getCurrentUser(currentUser_id);
        return currentUser.getCompany().getDepartments();
    }

    public Department getDepartment(long department_id) {
        return departmentRepository.findById(department_id);
    }

    public String createDepartment(long currentUser_id, String departmentName) {
        User currentUser = getCurrentUser(currentUser_id);
        Department newDepartment = new Department(departmentName);
        currentUser.getCompany().addDepartment(newDepartment);
        departmentRepository.save(newDepartment);

        System.out.println("Created new Department: " + newDepartment.getDepartmentName());
        return success;
    }

    public String updateDepartment(long department_id, String updatedDepartmentName) {
        Department departmentToUpdate = departmentRepository.findById(department_id);
        departmentToUpdate.setDepartmentName(updatedDepartmentName);
        departmentRepository.save(departmentToUpdate);

        System.out.println("Updated department name: " + departmentToUpdate.getDepartmentName());
        return success;
    }

    public String removeDepartment(long department_id) {
        Department departmentToRemove = departmentRepository.findById(department_id);
        departmentToRemove.getInventory().clear();
        departmentToRemove.getCompany().removeDepartment(departmentToRemove);
        departmentRepository.save(departmentToRemove);
        departmentRepository.deleteById(department_id);

        System.out.println("Removed department name: " + departmentToRemove.getDepartmentName());
        return success;
    }

    private User getCurrentUser(long user_id) {
        User user = userRepository.findById(user_id);
        if (user == null) throw new RuntimeException("Invalid user Id.");
        return user;
    }
}
