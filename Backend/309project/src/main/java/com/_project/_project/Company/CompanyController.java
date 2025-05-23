package com._project._project.Company;

import com._project._project.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com._project._project.User.UserRepository;
import com._project._project.User.UserRole;

import java.util.List;
import java.util.Map;

//This might not be necessary as we do not want to give users the ability to delete companies apart from their own, however it will probably have a use from an admin perspective

@RestController
public class CompanyController {

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    UserRepository userRepository;

    private final String success = "Success";
    private final String failure = "Failure";

    @GetMapping(path = "/Companies")
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @GetMapping(path = "/Companies/{id}")
    public Company getCompanyById(@PathVariable int id) {
        return companyRepository.findById(id);
    }

    @GetMapping(path = "/Companies/{id}/Users")
    public List<User> getCompanyUsers(@PathVariable long id) {
        Company company = companyRepository.findById(id);

        return company.getUsers();
    }

    //    Can use this in the future if needed, the JSON return a separate OWNER object under company
//    so we would need to alter a bit
    @GetMapping(path = "/Companies/{id}/Owner")
    public User getCompanyOwner(@PathVariable long id) {
        Company company = companyRepository.findById(id);

        return company.getOwner();
    }

    @PostMapping(path = "/Companies")
    public String createCompany(@RequestBody Company company) {
        if (company == null) return failure;

        companyRepository.save(company);
        return success;
    }

    @PutMapping(path = "/Companies/{Id}")
    public Company updateCompany(@PathVariable int Id, @RequestBody Company request) {
        Company company = companyRepository.findById(Id);

        if (company == null) {
            throw new RuntimeException("Company ID does not exist");
        } else if (company.getId() != Id) {
            throw new RuntimeException("Path variable ID does not match company request Id");
        }

        companyRepository.save(request);
        return companyRepository.findById(Id);
    }

    @DeleteMapping(path = "/Companies/{Id}")
    public String deleteCompany(@PathVariable int Id, @RequestBody User user) {
        if (companyRepository.findById(Id) == null) return failure;

        Company company = companyRepository.findById(Id);

        if (!company.getOwner().equals(user)) {
            throw new RuntimeException("You do not have permission to delete this company");
        }

        companyRepository.deleteById(Id);
        return success;
    }

    @PutMapping("/{currentUser_id}/company/update-info")
    public ResponseEntity<?> updateCompanyInfo(
            @PathVariable long currentUser_id,
            @RequestBody Map<String, String> info) {
        
        User currentUser = userRepository.findById(currentUser_id);
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }

        Company company = currentUser.getCompany();
        if (company == null) {
            return ResponseEntity.notFound().build();
        }

        // Only OWNER or EXECUTIVE can update company info
        if (currentUser.getRole() != UserRole.OWNER && 
            currentUser.getRole() != UserRole.EXECUTIVE) {
            return ResponseEntity.badRequest()
                .body("Only OWNER or EXECUTIVE can update company information");
        }

        company.setStreetAddress(info.get("streetAddress"));
        company.setCity(info.get("city"));
        company.setState(info.get("state"));
        company.setZipCode(info.get("zipCode"));
        company.setCountry(info.get("country"));
        
        if (info.containsKey("ein")) {
            company.setEin(info.get("ein"));
        }

        companyRepository.save(company);
        return ResponseEntity.ok(company);
    }

    @GetMapping("/{currentUser_id}/company/info")
    public ResponseEntity<?> getCompanyInfo(@PathVariable long currentUser_id) {
        User currentUser = userRepository.findById(currentUser_id);
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }

        Company company = currentUser.getCompany();
        if (company == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> companyInfo = Map.of(
            "streetAddress", company.getStreetAddress() != null ? company.getStreetAddress() : "",
            "city", company.getCity() != null ? company.getCity() : "",
            "state", company.getState() != null ? company.getState() : "",
            "zipCode", company.getZipCode() != null ? company.getZipCode() : "",
            "country", company.getCountry() != null ? company.getCountry() : "",
            "ein", company.getEin() != null ? company.getEin() : ""
        );

        return ResponseEntity.ok(companyInfo);
    }

}
