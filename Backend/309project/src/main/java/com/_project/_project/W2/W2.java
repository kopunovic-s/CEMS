package com._project._project.W2;

import com._project._project.User.User;
import com._project._project.Company.Company;
import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@Table(name = "w2s")
public class W2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonIgnore
    private User employee;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnore
    private Company company;

    private int year;
    
    // Employee Info
    private String employeeSsn;
    private String employeeFirstName;
    private String employeeLastName;
    private String employeeAddress;
    private String employeeCity;
    private String employeeState;
    private String employeeZip;

    // Employer Info
    private String employerEin;
    private String employerName;
    private String employerAddress;
    private String employerCity;
    private String employerState;
    private String employerZip;

    // Wage Info
    private Double wagesTipsOtherComp;
    private Double federalIncomeTax;
    private Double socialSecurityWages;
    private Double socialSecurityTax;
    private Double medicareWages;
    private Double medicareTax;
    private String stateCode;
    private String stateId;
    private Double stateWages;
    private Double stateIncomeTax;
} 