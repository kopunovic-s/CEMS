package com._project._project.User;

import com._project._project.Company.Company;
import com._project._project.Project.Project;
import com._project._project.TimeCard.TimeCard;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id //Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) //generate a value if none present
    private long Id;
    private String firstName;
    private String lastName;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    private String email;

    private Double hourlyRate;

    @ManyToOne
    @JsonIgnore
    private Company company;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<TimeCard> timeCards = new ArrayList<>();

    @ManyToMany
    @JsonIgnore
    private List<Project> projects = new ArrayList<>();

    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    @Column(unique = true)
    private String ssn;  // Format: XXX-XX-XXXX

    @Column(columnDefinition = "LONGTEXT")
    private String profileImage; 

    public User(String firstName, String lastName, String email, String password, UserRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User() {
    }

    //getters and setters
    public long getId() {return Id;}

    public String getFirstName() {return firstName;}
    public void setFirstName(String firstName) {this.firstName = firstName;}

    public String getLastName() {return lastName;}
    public void setLastName(String lastName) {this.lastName = lastName;}

    @JsonIgnore
    public String getUserName() {return firstName + " " + lastName;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public UserRole getRole() {return role;}
    public void setRole(UserRole role) {this.role = role;}

    public Company getCompany() {return company;}
    public void setCompany(Company company) {this.company = company;}

    public String getCompanyName() {return company.getName();}

    public long getCompanyId() {return company.getId();}

    public void addTimeCard(TimeCard timeCard) {
        timeCard.setUser(this);
        timeCards.add(timeCard);
    }

    public Double getHourlyRate() {
        if (role == UserRole.EMPLOYEE || role == UserRole.MANAGER) {
            return hourlyRate;
        }
        return null;
    }
    
    public void setHourlyRate(Double hourlyRate) {
        // Only set hourly rate for EMPLOYEE and MANAGER
        if (role == UserRole.EMPLOYEE || role == UserRole.MANAGER) {
            this.hourlyRate = hourlyRate;
        }
    }

    public List<Project> getProjects() {return projects;}
    public void addProject(Project project){projects.add(project);}
    public void removeProject(Project project){projects.remove(project);}

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public void setId(long id) {
        Id = id;
    }

}
