package com._project._project.WebSocket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClockEventRepository extends JpaRepository<ClockInOutMessage, Long> {
    // Add methods to query historical data
    List<ClockInOutMessage> findByTimestampAfter(LocalDateTime timestamp);
    List<ClockInOutMessage> findByEmployeeName(String employeeName);
    List<ClockInOutMessage> findAllByOrderByTimestampDesc();
} 