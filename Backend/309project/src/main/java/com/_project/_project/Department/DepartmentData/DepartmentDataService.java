package com._project._project.Department.DepartmentData;

import com._project._project.Department.Department;
import com._project._project.Department.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentDataService {

    @Autowired
    private DepartmentDataRepository departmentDataRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<DepartmentData> getDepartmentDataList(long department_id) {
        Department department = departmentRepository.findById(department_id);
        if(department == null) throw new RuntimeException("Department not found");

        return department.getDepartmentData();
    }

    public DepartmentData getDepartmentData(long departmentData_id) {
        DepartmentData departmentData = departmentDataRepository.findById(departmentData_id);
        if(departmentData == null) throw new RuntimeException("Invalid department data id.");

        return departmentData;
    }

    public String deleteDepartmentData(long departmentData_id) {
        DepartmentData departmentData = departmentDataRepository.findById(departmentData_id);
        Department department = departmentData.getDepartment();
        department.removeDepartmentData(departmentData);

        departmentDataRepository.save(departmentData);
        departmentDataRepository.deleteById(departmentData_id);

        System.out.println("Deleted data with date: "  + departmentData.getDate());
        System.out.println("Department: " + department.getDepartmentName());
        return "success";
    }


}
