package com._project._project;

import com._project._project.Company.Company;
import com._project._project.Project.*;
import com._project._project.User.User;
import com._project._project.User.UserRepository;
import com._project._project.User.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectTests {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private Company testCompany;
    private Project project;
    private final LocalDateTime startDateTime = LocalDateTime.now();
    private final LocalDateTime endDateTime = LocalDateTime.now().plusDays(7);

    @BeforeAll
    void setupTestData() {
        testCompany = new Company("Test Company");
        project = new Project("Test Project", "Test Description", startDateTime, endDateTime);
        testCompany.addProject(project);
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(projectRepository.findById(1L)).thenReturn(project);
        when(projectService.getProjectById(1L, 1L)).thenReturn(project);
        when(projectService.createProject(1L, project)).thenReturn("Success");
        when(projectService.closeProject(1L, 1L)).thenReturn("Success");
        when(projectService.reopenProject(1L, 1L)).thenReturn("Success");
        when(projectService.addUserToProject(1L, 1L, "alice@test.com")).thenReturn("Success");
        when(projectService.removeUserFromProject(1L, 1L, "alice@test.com")).thenReturn("Success");
    }

    @Test
    void testCreateProject_Success() {
        String result = projectController.createProject(1L, project);
        assertEquals("Success", result);
    }

    @Test
    void testCloseProject() {
        String result = projectController.closeProject(1L, 1L);
        assertEquals("Success", result);
    }

    @Test
    void testReopenProject() {
        project.setIsActive(false);
        String result = projectController.reopenProject(1L, 1L);
        assertEquals("Success", result);
    }

    @Test
    void testAddUserToProject() {
        String result = projectController.addUserToProject(1L, 1L, "alice@test.com");
        assertEquals("Success", result);
    }

    @Test
    void testRemoveUserFromProject() {
        String result = projectController.removeUserFromProject(1L, 1L, "alice@test.com");
        assertEquals("Success", result);
    }

    @Test
    void testGetProjectUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("Alice", "Employee", "alice@test.com", "password", UserRole.EMPLOYEE));
        when(projectService.getProjectUsers(1L, 1L)).thenReturn(users);
        List<User> result = projectController.getProjectGetUsers(1L, 1L);
        assertEquals(1, result.size());
    }

    @Test
    void testDeleteProject() {
        String result = projectController.deleteProject(1L, 1L);
        assertEquals(null, result);
    }

    @Test
    void testUpdateProject() {
        Project updatedProject = new Project("Updated Project", "Updated Description", startDateTime, endDateTime);
        when(projectService.updateProject(1L, 1L, updatedProject)).thenReturn(updatedProject);
        Project result = projectController.updateProject(1L, 1L, updatedProject);
        assertEquals("Updated Project", result.getName());
        assertEquals("Updated Description", result.getDescription());
    }

    @Test
    void testGetCompanyProjects() {
        List<Project> projects = new ArrayList<>();
        projects.add(project);
        when(projectService.getCompanyProjects(1L)).thenReturn(projects);
        List<Project> result = projectController.getCompanyProjects(1L);
        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
    }

    @Test
    void testProjectEntityMethods() {
        project.setName("Updated Project");
        assertEquals("Updated Project", project.getName());

        project.setDescription("Updated Description");
        assertEquals("Updated Description", project.getDescription());

        LocalDateTime newStartDate = startDateTime.plusDays(1);
        project.setStartDate(newStartDate);
        assertEquals(newStartDate, project.getStartDate());

        project.setIsActive(false);
        assertFalse(project.getIsActive());

        LocalDateTime newDeadline = endDateTime.plusDays(3);
        project.setDeadline(newDeadline);
        assertEquals(newDeadline, project.getDeadline());
    }

    @Test
    void testGetActiveCompanyProjects() {
        List<Project> projects = new ArrayList<>();
        projects.add(project);
        when(projectService.getActiveCompanyProjects(1L)).thenReturn(projects);
        List<Project> result = projectController.getActiveCompanyProjects(1L);
        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
    }

    @Test
    void testGetInactiveCompanyProjects() {
        project.setIsActive(false);
        List<Project> projects = new ArrayList<>();
        projects.add(project);
        when(projectService.getInactiveCompanyProjects(1L)).thenReturn(projects);
        List<Project> result = projectController.getInactiveCompanyProjects(1L);
        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
    }


}
