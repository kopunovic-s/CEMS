package com._project._project.Project.SalesData;

import com._project._project.Company.Company;
import com._project._project.Project.Project;
import com._project._project.Project.ProjectRepository;
import com._project._project.User.User;
import com._project._project.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SalesDataService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    SalesDataRepository salesDataRepository;

    private final String success = "success";

    public List<SalesData> getCompanySalesData(long currentUser_id){
        User user = userRepository.findById(currentUser_id);
        Company company = user.getCompany();
        if(company == null)
            throw new RuntimeException("Invalid company Id.");

        List<SalesData> companySalesData = new ArrayList<>();
        for(Project project : company.getProjects()){
            companySalesData.addAll(project.getSalesData());
        }

        return companySalesData;
    }

    public List<SalesData> getProjectSalesData(long currentUser_id, long project_id){
        User user = userRepository.findById(currentUser_id);
        Project project = projectRepository.findById(project_id);
        verifyUserAndProject(user, project);

        return project.getSalesData();
    }

    public String createSalesData(long currentUser_id, long project_id, SalesData data){
        User user = userRepository.findById(currentUser_id);
        Project project = projectRepository.findById(project_id);
        verifyUserAndProject(user, project);

        SalesData salesData = new SalesData(project, data.getIncome(), data.getExpenses(), data.getDate());
        project.addSalesData(salesData);
        salesDataRepository.save(salesData);
        projectRepository.save(project);

        System.out.println("Created sales data for project " + project.getName() + ", Id: " + project.getId());
        System.out.println("Created by: " + user.getUserName());
        return success;
    }

    public String removeSalesData(long currentUser_id, long salesData_id){
        User user = userRepository.findById(currentUser_id);
        if(user == null) throw new RuntimeException("Invalid sales data or user Id.");
        salesDataRepository.deleteById(salesData_id);

        System.out.println("Deleted data Id: " + salesData_id);
        System.out.println("Deleted by: " + user.getUserName());
        return success;
    }

    public String updateSalesData(long currentUser_id, long salesData_id, SalesData data){
        User user = userRepository.findById(currentUser_id);
        SalesData salesData = salesDataRepository.findById(salesData_id);
        if(user == null || salesData == null) throw new RuntimeException("Invalid sales data or user Id.");

        salesData.setDate(data.getDate());
        salesData.setIncome(data.getIncome());
        salesData.setExpenses(data.getExpenses());
        salesDataRepository.save(salesData);

        System.out.println("Updated data Id: " + salesData_id);
        System.out.println("New date: " + salesData.getDate() + "\nNew income: " + data.getIncome() + "\nNew expense: " + data.getExpenses());
        System.out.println("Updated by: " + user.getUserName());
        return success;
    }


    //Helper method to avoid redundant code for verifying user and project
    private void verifyUserAndProject(User user, Project project){
        if(user == null
                || project == null
                || !user.getCompany().equals(project.getCompany()))
            throw new RuntimeException("Invalid company or user Id.");
    }
}
