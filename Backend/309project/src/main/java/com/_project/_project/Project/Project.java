package com._project._project.Project;

import com._project._project.Project.SalesData.SalesData;
import com._project._project.User.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com._project._project.Company.Company;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnore
    private Company company;

    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime closedDate;
    private boolean isActive;
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany
    private List<User> users = new ArrayList<>();


    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<SalesData> salesDataList = new ArrayList<>();

    public Project() {}

    public Project(String name, String description, LocalDateTime startDate, LocalDateTime deadline) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.isActive = true;
        this.deadline = deadline;
        this.status = Status.IN_PROGRESS;
    }

    // Getters and Setters
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Company getCompany() {return company;}
    public void setCompany(Company company) {this.company = company;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public LocalDateTime getStartDate() {return startDate;}
    public void setStartDate(LocalDateTime startDate) {this.startDate = startDate;}

    public LocalDateTime getClosedDate() {return closedDate;}
    public void setClosedDate(LocalDateTime closedDate) {this.closedDate = closedDate;}

    public boolean getIsActive() {return isActive;}
    public void setIsActive(boolean active) {isActive = active;}

    public Status getStatus() {return status;}
    public void setStatus(Status status) {this.status = status;}

    public List<User> getUsers() {return users;}
    public void addUser(User user){
        user.addProject(this);
        users.add(user);
    }
    public void removeUser(User user){
        user.removeProject(this);
        users.remove(user);
    }

    public List<SalesData> getSalesData() {return salesDataList;}
    public void addSalesData(SalesData salesData){
        salesData.setProject(this);
        salesDataList.add(salesData);
    }

    public LocalDateTime getDeadline() {return deadline;}
    public void setDeadline(LocalDateTime deadline) {this.deadline = deadline;}

}
