package com._project._project.Schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserId(Long userId);
    List<Schedule> findByUserIdAndStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<Schedule> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
