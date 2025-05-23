package com._project._project.Payroll.Responses;

import java.time.LocalDateTime;
import java.util.List;

import com._project._project.Payroll.Payroll;
import com._project._project.User.User;

public class PayrollWeekResponse {
    private int weekNumber;
    private int year;
    private LocalDateTime processedDate;
    private boolean isPaid;
    private List<EmployeeSummary> employees;

    public static class EmployeeSummary {
        private UserInfo user;
        private Double totalHours;
        private Double hourlyRate;
        private Double totalPay;

        public static class UserInfo {
            private String firstName;
            private String lastName;
            private Long id;
            private String role;

            public UserInfo(User user) {
                this.firstName = user.getFirstName();
                this.lastName = user.getLastName();
                this.id = user.getId();
                this.role = user.getRole().toString();
            }

            public String getFirstName() {
                return firstName;
            }

            public String getLastName() {
                return lastName;
            }

            public Long getId() {
                return id;
            }

            public String getRole() {
                return role;
            }
        }

        public EmployeeSummary(Payroll payroll) {
            this.user = new UserInfo(payroll.getUser());
            this.totalHours = payroll.getTotalHours();
            this.hourlyRate = payroll.getHourlyRate();
            this.totalPay = payroll.getTotalPay();
        }

        public UserInfo getUser() {
            return user;
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
    }

    public PayrollWeekResponse(int weekNumber, int year, LocalDateTime processedDate, 
            boolean isPaid, List<Payroll> payrolls) {
        this.weekNumber = weekNumber;
        this.year = year;
        this.processedDate = processedDate;
        this.isPaid = isPaid;
        this.employees = payrolls.stream()
            .map(EmployeeSummary::new)
            .toList();
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public int getYear() {
        return year;
    }

    public LocalDateTime getProcessedDate() {
        return processedDate;
    }

    public boolean getIsPaid() {
        return isPaid;
    }

    public List<EmployeeSummary> getEmployees() {
        return employees;
    }
} 