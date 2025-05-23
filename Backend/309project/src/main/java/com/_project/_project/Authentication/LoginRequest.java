package com._project._project.Authentication;

public class LoginRequest {
    private String email;
    private String password;
    private String companyName;
    private long companyId;

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public String getCompanyName() {return companyName;}
    public void setCompanyName(String companyName) {this.companyName = companyName;}

    public long getCompanyId() {return companyId;}
    public void setCompanyId(long companyId) {this.companyId = companyId;}
}
