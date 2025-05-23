package com._project._project.Department;


import com._project._project.Company.Company;
import com._project._project.Department.DepartmentData.DepartmentData;
import com._project._project.Department.Item.Item;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Department {

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnore
    private Company company;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DepartmentData> departmentData = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String departmentName;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Item> inventory = new ArrayList<>();

    public Department(String departmentName) {
        this.departmentName = departmentName;
    }

    public void addItem(Item item) {
        item.setDepartment(this);
        inventory.add(item);
    }

    public void removeItem(Item item) {
        item.setDepartment(null);
        inventory.remove(item);
    }

    public void addDepartmentData(DepartmentData data) {
        data.setDepartment(this);
        departmentData.add(data);
    }

    public void removeDepartmentData(DepartmentData data) {
        data.setDepartment(null);
        departmentData.remove(data);
    }
}
