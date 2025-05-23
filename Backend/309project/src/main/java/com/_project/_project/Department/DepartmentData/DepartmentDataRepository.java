package com._project._project.Department.DepartmentData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DepartmentDataRepository extends JpaRepository<DepartmentData, Long> {
    DepartmentData findById(long id);
    <Optional> DepartmentData findByDate(LocalDate date);
    void deleteById(long id);
    void deleteByDate(LocalDate date);
}
