package com._project._project.Payroll.Responses;

import java.util.List;

public class EmployeeWeekResponse {
    private String name;
    private String role;
    private List<DailyTimeCardResponse> dailyDetails;

    public EmployeeWeekResponse(String name, String role, List<DailyTimeCardResponse> dailyDetails) {
        this.name = name;
        this.role = role;
        this.dailyDetails = dailyDetails;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public List<DailyTimeCardResponse> getDailyDetails() {
        return dailyDetails;
    }
} 