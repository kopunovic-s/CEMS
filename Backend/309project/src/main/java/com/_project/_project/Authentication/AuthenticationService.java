package com._project._project.Authentication;

import com._project._project.Company.Company;
import com._project._project.Company.CompanyRepository;
import com._project._project.User.User;
import com._project._project.User.UserRepository;
import com._project._project.User.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AuthenticationService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    public User registerUser(RegisterRequest request) {

        Company company = new Company(request.getCompanyName());
        companyRepository.save(company);

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(UserRole.OWNER);
        user.setCompany(company);

        userRepository.save(user);
        company.addUser(user);
        companyRepository.save(company);

        System.out.println("Created company: " + company.getName() + " with owner: " + user.getFirstName() + " " + user.getLastName());
        return user;
    }

    public User loginUser(LoginRequest request) {
        Company company = companyRepository.findById(request.getCompanyId());
        if(company == null)
            throw new RuntimeException("Company not found");

        if(!company.getName().equals(request.getCompanyName()))
            throw new RuntimeException("Company name does not match company ID.");

        User userToLogin = new User();
        for(User user : company.getUsers()){
           if(user.getEmail().equals(request.getEmail())){
               userToLogin = user;
               break;
            }
        }
        if(userToLogin.getEmail() == null)
            throw new RuntimeException("User with this email does not exist in company: " + company.getName());

        if(!userToLogin.getPassword().equals(request.getPassword()))
            throw new RuntimeException("Incorrect password.");

        System.out.println("Logged in user: " + userToLogin.getFirstName() + " " + userToLogin.getLastName());
        return userToLogin;
    }
}
