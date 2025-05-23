package com._project._project.Project;

import com._project._project.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    ProjectService projectService;

    @GetMapping(path = "/{currentUser_id}")
    public List<Project> getCompanyProjects(@PathVariable long currentUser_id) {
        return projectService.getCompanyProjects(currentUser_id);
    }

    @GetMapping(path = "/active/{currentUser_id}")
    public List<Project> getActiveCompanyProjects(@PathVariable long currentUser_id) {
        return projectService.getActiveCompanyProjects(currentUser_id);
    }

    @GetMapping(path = "/inactive/{currentUser_id}")
    public List<Project> getInactiveCompanyProjects(@PathVariable long currentUser_id) {
        return projectService.getInactiveCompanyProjects(currentUser_id);
    }

    @GetMapping(path = "/id/{currentUser_id}/{project_id}")
    Project getProjectById(@PathVariable long currentUser_id, @PathVariable long project_id) {
        return projectService.getProjectById(currentUser_id, project_id);
    }

    @PostMapping(path = "/{currentUser_id}")
    public String createProject(@PathVariable long currentUser_id, @RequestBody Project project) {
        return projectService.createProject(currentUser_id, project);
    }

    @PutMapping(path = "/{currentUser_id}/{project_id}")
    public Project updateProject(@PathVariable long currentUser_id, @PathVariable long project_id, @RequestBody Project request) {
        return projectService.updateProject(currentUser_id, project_id, request);
    }

    @PutMapping(path = "/close/{currentUser_id}/{project_id}")
    public String closeProject(@PathVariable long currentUser_id, @PathVariable long project_id) {
        return projectService.closeProject(currentUser_id, project_id);
    }

    @PutMapping(path = "/open/{currentUser_id}/{project_id}")
    public String reopenProject(@PathVariable long currentUser_id, @PathVariable long project_id) {
        return projectService.reopenProject(currentUser_id, project_id);
    }

    @DeleteMapping(path = "/{currentUser_id}/{project_id}")
    public String deleteProject(@PathVariable long currentUser_id, @PathVariable long project_id) {
        return projectService.deleteProject(currentUser_id, project_id);
    }

    @GetMapping(path = "/{currentUser_id}/project-get-users/{project_id}")
    public List<User> getProjectGetUsers(@PathVariable long currentUser_id, @PathVariable long project_id) {
        return projectService.getProjectUsers(currentUser_id, project_id);
    }

    @PostMapping(path = "/{project_id}/project-add-user/{currentUser_id}/{userToAddEmail}")
    public String addUserToProject(@PathVariable long project_id, @PathVariable long currentUser_id, @PathVariable String userToAddEmail) {
        return projectService.addUserToProject(project_id, currentUser_id, userToAddEmail);
    }

    @PutMapping(path = "/{project_id}/project-remove-user/{currentUser_id}/{userToRemoveEmail}")
    public String removeUserFromProject(@PathVariable long project_id, @PathVariable long currentUser_id, @PathVariable String userToRemoveEmail) {
        return projectService.removeUserFromProject(project_id, currentUser_id, userToRemoveEmail);
    }

    /*
    GET request to retrieve filtered list of users to add to project,
    excludes any users that are already added to the project.
    SEND THIS NEW ENDPOINT TO NATE!!!!!
     */
    @GetMapping(path = "/{currentUser_id}/project-get-users-to-add/{project_id}")
    List<User> getUsersToAddToProject(@PathVariable long currentUser_id, @PathVariable long project_id) {
        return projectService.getUsersToAddToProject(currentUser_id, project_id);
    }
}
