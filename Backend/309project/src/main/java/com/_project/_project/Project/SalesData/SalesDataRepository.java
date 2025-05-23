package com._project._project.Project.SalesData;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SalesDataRepository extends JpaRepository<SalesData, Long> {
    SalesData findById(long id);

    void deleteById(long id);

    List<SalesData> findByDate(LocalDate date);
}
