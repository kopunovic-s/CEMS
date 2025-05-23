package com._project._project.W2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface W2Repository extends JpaRepository<W2, Long> {
    List<W2> findByEmployeeId(Long employeeId);
    List<W2> findByCompanyId(Long companyId);
    List<W2> findByEmployeeIdAndYear(Long employeeId, int year);
} 