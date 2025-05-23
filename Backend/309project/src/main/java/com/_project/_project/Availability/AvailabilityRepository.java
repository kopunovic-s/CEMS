package com._project._project.Availability;

import com._project._project.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByUser(User user);
    List<Availability> findByUserAndDayOfWeek(User user, DayOfWeek dayOfWeek);
    void deleteByUser(User user);
    List<Availability> findByUserId(long userId);
}
