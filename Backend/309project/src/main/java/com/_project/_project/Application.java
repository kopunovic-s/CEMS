package com._project._project;

import com._project._project.Company.Company;
import com._project._project.Company.CompanyRepository;
import com._project._project.Department.Department;
import com._project._project.Department.DepartmentRepository;
import com._project._project.Department.Item.Item;
import com._project._project.Project.Project;
import com._project._project.Project.ProjectRepository;
import com._project._project.Project.SalesData.SalesData;
import com._project._project.ReportGenerator.ReportGeneratorService;
import com._project._project.ReportGenerator.ReportUtils;
import com._project._project.User.User;
import com._project._project.User.UserRole;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ui.ApplicationFrame;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

        //CI/CD
    //Test entities and endpoints for Compaasdasdny,User
    //Will be testing with Database later on
//    Use for testing other parts of backend as needed
//    @Bean
//    CommandLineRunner initCompany(CompanyRepository CompanyRepository, DepartmentRepository departmentRepository, ProjectRepository projectRepository, ReportGeneratorService reportGeneratorService) {
//        return args -> {
//            Company c1 = new Company("ISU");
//            Company c2 = new Company("Target");
//            Company c3 = new Company("Microsoft");
//            Company c4 = new Company("Apple");
//
//            User u1 = new User("John", "Williams", "jwill@gmail.com", "123", UserRole.OWNER);
//            User u2 = new User("Emma", "Thomas", "fakemail@gmail.com", "456", UserRole.EXECUTIVE);
//            User u3 = new User("Devin", "Smith", "dsmit@hotmail.com", "123", UserRole.MANAGER);
//
//            User u4 = new User("Srdjan", "Kopunovic","srdjan@gmail.com", "111", UserRole.OWNER);
//            User u5 = new User("Andrew", "Vanderbilt","avanderbilt@gmail.com", "222", UserRole.EMPLOYEE);
//
//            Project p1 = new Project("FilteredList", "testing new GET request for a filtered user list.", LocalDateTime.now(), null);
//
//            Department d1 = new Department("TestDepartment");
//
//            c1.addDepartment(d1);
//
//            Item i1 = new Item("Shoes", BigDecimal.valueOf(140), BigDecimal.valueOf(40.50),  10, "White shoes");
//            Item i2 = new Item("Shirt", BigDecimal.valueOf(40), BigDecimal.valueOf(10.00), 20, "Black shirts");
//            Item i3 = new Item("Hat", BigDecimal.valueOf(25.50), BigDecimal.valueOf(8.00), 15, "Baseball cap");
//            Item i4 = new Item("Sweatshirt", BigDecimal.valueOf(65.99), BigDecimal.valueOf(25), 0, "Something something something");
//
//            d1.addItem(i1);
//            d1.addItem(i2);
//            d1.addItem(i3);
//            d1.addItem(i4);
//
//            p1.addUser(u1);
//
//            for(int i = 0; i < 6; i++){
//                SalesData sd = new SalesData(p1, 15 * (i + 1), 5 * (i + 1), LocalDate.now().minusDays(i));
//            }
//
//
//            c1.addUser(u1);
//            c1.addUser(u2);
//            c1.addUser(u3);
//            c1.addProject(p1);
//
//            c2.addUser(u4);
//            c2.addUser(u5);
//
//            CompanyRepository.save(c1);
//            CompanyRepository.save(c2);
//            CompanyRepository.save(c3);
//            CompanyRepository.save(c4);
//
//            for(int i = 0; i < p1.getSalesData().size(); i++){
//                System.out.println("Date: " + p1.getSalesData().get(i).getDate());
//                System.out.println("Income: " + p1.getSalesData().get(i).getIncome());
//                System.out.println("Expenses: " + p1.getSalesData().get(i).getExpenses());
//                System.out.println("Revenue: " + p1.getSalesData().get(i).getRevenue());
//            }
//
//            System.out.println("Project : " + p1.getName() + "\nId: " + p1.getId() + "\nCompany: " + p1.getCompany().getName());

            // Tests for graphing data and saving png within root directory /backend/309/
//            List<SalesData> salesDataList = new ArrayList<>();
//            for (int i = 0; i < 7; i++) {
//                LocalDate date = LocalDate.now().minusDays(6 - i);
//                long income = 1000 + (long) (Math.random() * 500);
//                long expenses = 400 + (long) (Math.random() * 300);
//                salesDataList.add(new SalesData(null, income, expenses, date));
//            }
//
//            // Generate chart
//            JFreeChart chart = ReportUtils.createSalesChart(salesDataList);
//
//            // Option A: Save chart to PNG
//            File outputFile = new File("test_sales_chart.png");
//            ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
//            System.out.println("Chart saved to: " + outputFile.getAbsolutePath());
//            System.out.println("Chart saved to: " + outputFile.getAbsolutePath());
//
//        };
//    }
}
