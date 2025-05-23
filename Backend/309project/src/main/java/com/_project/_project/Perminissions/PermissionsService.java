package com._project._project.Perminissions;

import com._project._project.Company.Company;
import com._project._project.User.User;
import com._project._project.User.UserRole;
import org.springframework.stereotype.Service;


//NOTE: IF CHECK = TRUE, THEN THE USER DOES NOT HAVE PERMISSION

@Service
public class PermissionsService {

    public boolean NoDoublePermissions(User currentUser, User targetUser) {
        return currentUser.getId() != targetUser.getId() && 
        currentUser.getRole() != UserRole.OWNER && 
        currentUser.getRole() != UserRole.EXECUTIVE;
    }

    public boolean NoManagerPermissions(User currentUser, User targetUser) {
        return currentUser.getId() != targetUser.getId() && 
        currentUser.getRole() != UserRole.OWNER && 
        currentUser.getRole() != UserRole.EXECUTIVE &&
        currentUser.getRole() != UserRole.MANAGER;
    }

    public boolean NoSinglePermissions(User currentUser) {
        return currentUser.getRole() != UserRole.OWNER && 
        currentUser.getRole() != UserRole.EXECUTIVE;
    }
    
    public boolean IsEmployee(User currentUser) {
        return currentUser.getRole() == UserRole.EMPLOYEE;
    }

    public boolean NoCompanyEditPermissions(User currentUser, Company targetCompany) {
        return currentUser.getCompany().getId() != targetCompany.getId() && 
        currentUser.getRole() != UserRole.OWNER && 
        currentUser.getRole() != UserRole.EXECUTIVE;
    }
    
    public boolean NoUserExists(User currentUser, User targetUser) {
        return currentUser == null || targetUser == null;
    }

    public boolean NoCompanyExists(User currentUser, Company targetCompany) {
        return currentUser == null || targetCompany == null;
    }
}
