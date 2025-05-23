package com._project._project.Payroll;

import com._project._project.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Payroll findByUserAndWeekNumberAndYear(User user, int weekNumber, int year);
    List<Payroll> findByUser(User user);
}
