package com._project._project.Project;

import com._project._project.Company.Company;
import com._project._project.User.User;
import com._project._project.User.UserRepository;
import com._project._project.User.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private final String success = "Success";
    private final String failure = "Failure";

    public List<Project> getCompanyProjects(long currentUser_id) {
        User user = userRepository.findById(currentUser_id);
        if (user == null)
            throw new RuntimeException("User with this ID not found");

        Company company = user.getCompany();
        if (company.getProjects() == null)
            throw new RuntimeException("Company does not have any projects");

        return company.getProjects();
    }

    public List<Project> getActiveCompanyProjects(long currentUser_id) {
        User user = userRepository.findById(currentUser_id);
        if (user == null)
            throw new RuntimeException("User with this ID not found");

        List<Project> projects = projectRepository.findByCompanyAndIsActiveTrue(user.getCompany());
        if (projects.isEmpty())
            throw new RuntimeException("No active projects found for this company");

        return projects;
    }

    public List<Project> getInactiveCompanyProjects(long currentUser_id) {
        User user = userRepository.findById(currentUser_id);
        if (user == null)
            throw new RuntimeException("User with this ID not found");

        List<Project> projects = projectRepository.findByCompanyAndIsActiveFalse(user.getCompany());
        if (projects.isEmpty())
            throw new RuntimeException("No active projects found for this company.");

        return projects;
    }

    public Project getProjectById(long currentUser_id, long project_id) {
        User user = userRepository.findById(currentUser_id);
        if (user == null)
            throw new RuntimeException("User with this ID not found");

        Project project = projectRepository.findById(project_id);
        if (project == null)
            throw new RuntimeException("Project with this ID not found");

        if (!user.getCompany().equals(project.getCompany()))
            throw new RuntimeException("User and project belong to different companies.");

        return project;
    }

    public String createProject(long currentUser_id, Project project) {
        if (project == null)
            return failure;

        User user = userRepository.findById(currentUser_id);
        if (user == null)
            throw new RuntimeException("User with this ID not found");

        if (user.getRole() == UserRole.EMPLOYEE)
            throw new RuntimeException("Employees do not have permission to create projects.");

        Project newProject = new Project(project.getName(), project.getDescription(), LocalDateTime.now(), project.getDeadline());

        Company company = user.getCompany();
        company.addProject(newProject);

        projectRepository.save(newProject);

        return success;
    }

    public Project updateProject(long currentUser_id, long project_id, Project request) {
        User user = userRepository.findById(currentUser_id);
        Project projectToUpdate = projectRepository.findById(project_id);
        if (user == null
                || projectToUpdate == null
                || !user.getCompany().equals(projectToUpdate.getCompany()))
            throw new RuntimeException("Invalid user or project ID.");

        projectToUpdate.setName(request.getName());
        projectToUpdate.setDescription(request.getDescription());
        projectToUpdate.setStartDate(request.getStartDate());
        projectToUpdate.setIsActive(request.getIsActive());
        projectToUpdate.setStatus(request.getStatus());

        if ((!request.getIsActive()) && (request.getClosedDate() != null)) {
            projectToUpdate.setClosedDate(request.getClosedDate());
            projectToUpdate.setIsActive(false);
        }

        return projectRepository.save(projectToUpdate);
    }

    public String closeProject(long currentUser_id, long project_id) {
        User user = userRepository.findById(currentUser_id);
        Project project = projectRepository.findById(project_id);
        if (user == null || project == null)
            throw new RuntimeException("Invalid user or project ID.");

        if (!user.getCompany().equals(project.getCompany()) || user.getRole() == UserRole.EMPLOYEE)
            throw new RuntimeException("Permission denied.");

        if (!project.getIsActive())
            throw new RuntimeException("Project is already closed.");

        project.setClosedDate(LocalDateTime.now());
        project.setIsActive(false);
        project.setStatus(Status.COMPLETE);
        projectRepository.save(project);

        return success;
    }

    public String reopenProject(long currentUser_id, long project_id) {
        User user = userRepository.findById(currentUser_id);
        Project project = projectRepository.findById(project_id);
        if (user == null || project == null)
            throw new RuntimeException("Invalid user or project ID.");

        if (!user.getCompany().equals(project.getCompany()) || user.getRole() == UserRole.EMPLOYEE)
            throw new RuntimeException("Permission denied.");

        project.setClosedDate(null);
        project.setIsActive(true);
        project.setStatus(Status.IN_PROGRESS);
        projectRepository.save(project);

        return success;
    }

    public String deleteProject(long currentUser_id, long project_id) {
        User user = userRepository.findById(currentUser_id);
        Project project = projectRepository.findById(project_id);
        if (user == null || project == null)
            throw new RuntimeException("Invalid user or project ID.");

        if ((user.getRole() == UserRole.EMPLOYEE) || (!user.getCompany().equals(projectRepository.findById(project_id).getCompany())))
            throw new RuntimeException("Employees do not have permission to delete projects.");

        for (User u : project.getUsers()) {
            u.removeProject(project);
            userRepository.save(u);
        }

        project.getUsers().clear();
        projectRepository.save(project);

        projectRepository.delete(project);

        return success;
    }

    public List<User> getProjectUsers(long currentUser_id, long project_id) {
        User currentUser = userRepository.findById(currentUser_id);
        Project project = projectRepository.findById(project_id);
        if (currentUser == null || project == null || !currentUser.getCompany().equals(project.getCompany()))
            throw new RuntimeException("Invalid user or project ID.");

        return project.getUsers();
    }

    public String addUserToProject(long project_id, long currentUser_id, String userToAddEmail) {
        User currentUser = userRepository.findById(currentUser_id);
        Project project = projectRepository.findById(project_id);
        if (project == null || currentUser == null)
            throw new RuntimeException("Invalid user or project ID.");

        int index = 0;
        User userToAdd = new User();
        for (User user : currentUser.getCompany().getUsers()) {
            if (user.getEmail().equals(userToAddEmail)) {
                userToAdd = user;
                break;
            }
            if (index == currentUser.getCompany().getUsers().size() - 1)
                throw new RuntimeException("User with this email does not exist.");

            index++;
        }

//        if ((!userToAdd.getCompany().equals(project.getCompany())) || (!currentUser.getCompany().equals(project.getCompany())))
//            throw new RuntimeException("Does not belong to the same company.");

        if (project.getUsers().contains(userToAdd))
            throw new RuntimeException("User already a part of this project.");

        project.addUser(userToAdd);

        projectRepository.save(project);
        System.out.print("Added user " + userToAdd.getFirstName() + " " + userToAdd.getLastName() + " to project " + project.getName());
        return success;
    }

    public String removeUserFromProject(long project_id, long currentUser_id, String userToRemoveEmail) {
        User currentUser = userRepository.findById(currentUser_id);
        Project project = projectRepository.findById(project_id);
        if (project == null || currentUser == null)
            throw new RuntimeException("Invalid user or project ID.");

        int index = 0;
        User userToRemove = new User();
        for (User user : currentUser.getCompany().getUsers()) {
            if (user.getEmail().equals(userToRemoveEmail)) {
                userToRemove = user;
                break;
            }
            if (index == currentUser.getCompany().getUsers().size() - 1)
                throw new RuntimeException("User with this email does not exist.");
        }

        if (!currentUser.getCompany().equals(project.getCompany()))
            throw new RuntimeException("Current user does not belong to the same company.");

        project.removeUser(userToRemove);

        projectRepository.save(project);
        System.out.print("Removed user " + userToRemove.getFirstName() + " " + userToRemove.getLastName() + " from project " + project.getName());
        return success;
    }

    List<User> getUsersToAddToProject(long project_id, long currentUser_id) {
        User currentUser = userRepository.findById(currentUser_id);
        Project project = projectRepository.findById(project_id);
        if (project == null || currentUser == null)
            throw new RuntimeException("Invalid user or project ID.");

        List<User> filteredUsers = new ArrayList<>();
        for (User user : project.getCompany().getUsers()) {
            if (project.getUsers().contains(user)) continue;
            filteredUsers.add(user);
        }
        return filteredUsers;
    }
}
