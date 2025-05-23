package com._project._project.Project;

import com._project._project.Company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Project findById(long id);
    void deleteById(long id);
    List<Project> findByCompany(Company company);
    List<Project> findByCompanyAndIsActiveTrue(Company company);
    Project findByName(String name);
    List<Project> findByCompanyAndIsActiveFalse(Company company);
}
