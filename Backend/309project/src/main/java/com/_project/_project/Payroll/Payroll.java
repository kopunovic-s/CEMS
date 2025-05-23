package com._project._project.Payroll;

import com._project._project.User.User;
import com._project._project.TimeCard.TimeCard;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "payroll")
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "payroll")
    @JsonIgnore
    private List<TimeCard> timeCards;

    private int weekNumber;

    @Column(name = "year_recorded")
    private int year;
    private Double totalHours;
    private Double hourlyRate;
    private Double totalPay;
    private LocalDateTime processedDate;
    private boolean isPaid;

    // Constructors
    public Payroll() {}

    public Payroll(User user, int weekNumber, int year) {
        this.user = user;
        this.weekNumber = weekNumber;
        this.year = year;
        this.hourlyRate = user.getHourlyRate();
        this.processedDate = LocalDateTime.now();
        this.isPaid = false;
        this.timeCards = new ArrayList<>();
    }

    // Getters
    public Long getId() {
        return Id;
    }

    public User getUser() {
        return user;
    }

    public List<TimeCard> getTimeCards() {
        return timeCards;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public int getYear() {
        return year;
    }

    public Double getTotalHours() {
        return totalHours;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public Double getTotalPay() {
        return totalPay;
    }

    public LocalDateTime getProcessedDate() {
        return processedDate;
    }

    public boolean getIsPaid() {
        return isPaid;
    }

    // Setters
    public void setId(Long id) {
        this.Id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setTimeCards(List<TimeCard> timeCards) {
        this.timeCards = timeCards;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setTotalHours(Double totalHours) {
        this.totalHours = totalHours;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public void setTotalPay(Double totalPay) {
        this.totalPay = totalPay;
    }

    public void setProcessedDate(LocalDateTime processedDate) {
        this.processedDate = processedDate;
    }

    public void setIsPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }
}
