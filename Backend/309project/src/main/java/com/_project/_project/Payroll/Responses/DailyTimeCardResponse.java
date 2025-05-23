package com._project._project.Payroll.Responses;

public class DailyTimeCardResponse {
    private String weekDay;
    private Double hoursWorked;
    private String times;

    public DailyTimeCardResponse(String weekDay, Double hoursWorked, String times) {
        this.weekDay = weekDay;
        this.hoursWorked = hoursWorked;
        this.times = times;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public Double getHoursWorked() {
        return hoursWorked;
    }

    public String getTimes() {
        return times;
    }
} 