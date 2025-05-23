package com._project._project.ReportGenerator;

import com._project._project.Company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportGeneratorRepository extends JpaRepository<ReportGenerator, Long> {
    ReportGenerator findById(long id);
    void deleteById(long id);
    List<ReportGenerator> findByReportDateAndCompany(LocalDate date, Company company);
}
