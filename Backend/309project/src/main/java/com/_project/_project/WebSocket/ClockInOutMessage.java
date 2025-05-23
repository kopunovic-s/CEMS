package com._project._project.WebSocket;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clock_events")
public class ClockInOutMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String employeeName;

    @Column
    private String eventType; // "CLOCK_IN" or "CLOCK_OUT"

    @Column
    private LocalDateTime timestamp;

    public ClockInOutMessage() {}

    public ClockInOutMessage(String employeeName, String eventType) {
        this.employeeName = employeeName;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
} 