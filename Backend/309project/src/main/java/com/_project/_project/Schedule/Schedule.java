package com._project._project.Schedule;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com._project._project.User.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.temporal.WeekFields;

@Entity
@Table(name = "schedule")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;

    @JsonFormat(pattern = "MM-dd-yyyy-HH:mm")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "MM-dd-yyyy-HH:mm")
    private LocalDateTime endTime;

    @JsonIgnore
    public String getDayOfWeek() {
        return startTime.getDayOfWeek().toString();
    }

    @JsonIgnore
    public int getWeekOfYear() {
        return startTime.get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    // Default constructor
    public Schedule() {}

    // Constructor with fields
    public Schedule(User user, LocalDateTime startTime, LocalDateTime endTime) {
        this.user = user;
        this.userId = user.getId();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    // Add getter/setter for userId
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
