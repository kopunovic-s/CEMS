package com._project._project.Company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Company findById(long id);

    void deleteById(long id);

    Company findByName(String name);

    void deleteByName(String name);
}
