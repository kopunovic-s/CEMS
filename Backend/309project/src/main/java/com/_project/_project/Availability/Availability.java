package com._project._project.Availability;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com._project._project.User.User;

import java.time.LocalTime;

@Entity
@Table(name = "availability")
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isAvailable;

    public Availability() {}

    public Availability(User user, DayOfWeek dayOfWeek, LocalTime startTime, 
                       LocalTime endTime, boolean isAvailable) {
        this.user = user;
        this.userId = user.getId();
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(boolean isAvailable) { this.isAvailable = isAvailable; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
}
