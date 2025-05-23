package com._project._project.User;

import com._project._project.Company.Company;
import com._project._project.Project.Project;
import com._project._project.Project.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;

    public List<User> getCompanyUsers(long user_id) {
        User user = userRepository.findById(user_id);
        if (user == null)
            throw new RuntimeException("User id does not exist.");

        Company company = user.getCompany();
        return company.getUsers();
    }

    public User getUser(long currentUser_id, long userToView_id) {
        User user = userRepository.findById(currentUser_id);
        if (user == null)
            throw new RuntimeException("Current user id does not exist.");

        User userToView = userRepository.findById(userToView_id);
        if (userToView == null)
            throw new RuntimeException("User you are looking for does not exist.");

        if (user.getCompany() != userToView.getCompany())
            throw new RuntimeException("Users do not belong to the same company.");

        return userToView;
    }

    public void createUser(long currentUser_id, User newUser) {
        User currentUser = userRepository.findById(currentUser_id);
        if (currentUser == null)
            throw new RuntimeException("Current user id does not exist.");

        if ((currentUser.getRole() != UserRole.EXECUTIVE) && (currentUser.getRole() != UserRole.OWNER))
            throw new RuntimeException("Current user does not have permission to add users.");

        Company company = currentUser.getCompany();
        company.addUser(newUser);
        userRepository.save(newUser);

        System.out.println("Successfully added user: " + newUser.getFirstName() + " " + newUser.getLastName());
    }

    public void updateUser(long currentUser_id, long userToUpdate_id, User updatedUser) {
        User currentUser = userRepository.findById(currentUser_id);

        if ((currentUser.getRole() != UserRole.OWNER) && (currentUser.getRole() != UserRole.EXECUTIVE))
            throw new RuntimeException("Current user does not have permission to add users.");

        User userToUpdate = userRepository.findById(userToUpdate_id);
        if (userToUpdate == null)
            throw new RuntimeException("User you are attempting to update does not exist");

        userToUpdate.setFirstName(updatedUser.getFirstName());
        userToUpdate.setLastName(updatedUser.getLastName());
        userToUpdate.setPassword(updatedUser.getPassword());
        userToUpdate.setRole(updatedUser.getRole());
        userToUpdate.setEmail(updatedUser.getEmail());
        userRepository.save(userToUpdate);

        System.out.println("Successfully updated user: " + updatedUser.getFirstName() + " " + updatedUser.getLastName());
    }

    public void deleteUser(long currentUser_id, long userToDelete_id) {
        User currentUser = userRepository.findById(currentUser_id);
        if ((currentUser == null) || (currentUser.getRole() != UserRole.OWNER) && (currentUser.getRole() != UserRole.EXECUTIVE))
            throw new RuntimeException("Current user does not have permission to delete company users.");

        User userToDelete = userRepository.findById(userToDelete_id);
        if ((userToDelete == null) || userToDelete.getRole() == UserRole.OWNER)
            throw new RuntimeException("Unable to delete user. Requested user to delete has owner role");

        for(Project p : userToDelete.getProjects()){
            p.getUsers().remove(userToDelete);
            projectRepository.save(p);
        }

        userToDelete.getProjects().clear();
        userRepository.save(userToDelete);

        userRepository.delete(userToDelete);

        System.out.println("Successfully deleted user: " + userToDelete.getFirstName() + " " + userToDelete.getLastName());
    }

    
}
