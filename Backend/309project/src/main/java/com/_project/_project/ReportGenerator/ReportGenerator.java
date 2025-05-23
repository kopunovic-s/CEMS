package com._project._project.ReportGenerator;

import com._project._project.Company.Company;
import com._project._project.Project.SalesData.SalesData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReportGenerator {

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnore
    private Company company;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String fileName;
    private LocalDate reportDate;

    private String employeeName;
    private long employeeId;

    private String projectName;
    private long projectId;

    @ElementCollection
    @CollectionTable(name = "report_sales_data", joinColumns = @JoinColumn(name = "report_id"))
    @JsonIgnore
    List<ReportGraphData> graphDataList;
}
