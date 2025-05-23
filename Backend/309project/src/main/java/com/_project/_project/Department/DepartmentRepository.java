package com._project._project.Department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findById(long id);
    Department findByDepartmentName(String name);
    void deleteById(long id);
}
