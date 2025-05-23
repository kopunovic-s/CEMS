package com._project._project.Project.SalesData;

import com._project._project.Project.Project;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class SalesData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDate date;
    private long income;
    private long expenses;
    private long revenue;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    public SalesData() {}

    public SalesData(Project project, long income, long expenses, LocalDate date) {
        this.date = date;
        this.income = income;
        this.expenses = expenses;
        this.revenue = this.income - this.expenses;

        if(project != null) project.addSalesData(this);
    }

    public long getId() {return id;}

    public LocalDate getDate() {return date;}
    public void setDate(LocalDate date) {this.date = date;}

    public long getIncome() {return income;}
    public void setIncome(long income) {
        this.income = income;
        revenue = this.income - this.expenses;
    }

    public long getExpenses() {return expenses;}
    public void setExpenses(long expenses) {
        this.expenses = expenses;
        revenue = this.income - this.expenses;
    }

    @JsonIgnore
    public Project getProject() {return project;}
    public void setProject(Project project) {this.project = project;}

    public long getRevenue() {return revenue;}
}
