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
public class ProjectServiceTests {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private Company testCompany;
    private Project project;
    private User user;
    private final LocalDateTime startDateTime = LocalDateTime.now();
    private final LocalDateTime endDateTime = LocalDateTime.now().plusDays(7);

    @BeforeAll
    void setupTestData() {
        testCompany = new Company("Test Company");
        project = new Project("Test Project", "Test Description", startDateTime, endDateTime);
        testCompany.addProject(project);
        user = new User("John", "Doe", "john@example.com", "password", UserRole.MANAGER);
        user.setCompany(testCompany);
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(userRepository.findById(1L)).thenReturn(user);
        when(projectRepository.findById(1L)).thenReturn(project);
    }

    @Test
    void testCreateProject_Success() {
        String result = projectService.createProject(1L, project);
        assertEquals("Success", result);
    }

    @Test
    void testCreateProject_InvalidUser() {
        when(userRepository.findById(2L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> projectService.createProject(2L, project));
    }

    @Test
    void testGetCompanyProjects_Success() {
        when(userRepository.findById(1L)).thenReturn(user);
        when(projectRepository.findByCompanyAndIsActiveTrue(testCompany)).thenReturn(List.of(project));
        List<Project> result = projectService.getCompanyProjects(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testCloseProject_AlreadyClosed() {
        project.setIsActive(false); // Simulate a closed project
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.closeProject(1L, 1L);
        });
        assertEquals("Project is already closed.", exception.getMessage());
    }

    @Test
    void testReopenProject_Success() {
        project.setIsActive(false);
        String result = projectService.reopenProject(1L, 1L);
        assertEquals("Success", result);
    }

    @Test
    void testDeleteProject_Success() {
        String result = projectService.deleteProject(1L, 1L);
        assertEquals("Success", result);
    }

    @Test
    void testAddUserToProject_Success() {
        // Create and set up the necessary objects
        Company testCompany = new Company("Test Company");
        User alice = new User("Alice", "Employee", "alice@example.com", "password", UserRole.EMPLOYEE);
        alice.setCompany(testCompany);
        Project project = new Project("Test Project", "Test Description", LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        project.setCompany(testCompany);

        // Mocking the repository calls
        when(userRepository.findById(1L)).thenReturn(alice);
        when(projectRepository.findById(1L)).thenReturn(project);

        // Call the service method
        String result = projectService.addUserToProject(1L, 1L, "alice@example.com");
        assertEquals("Success", result);
    }

    @Test
    void testRemoveUserFromProject_Success() {
        String result = projectService.removeUserFromProject(1L, 1L, "alice@example.com");
        assertEquals("Success", result);
    }

    @Test
    void testGetActiveCompanyProjects() {
        when(projectRepository.findByCompanyAndIsActiveTrue(testCompany)).thenReturn(List.of(project));
        List<Project> result = projectService.getActiveCompanyProjects(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testGetInactiveCompanyProjects() {
        project.setIsActive(false);
        when(projectRepository.findByCompanyAndIsActiveFalse(testCompany)).thenReturn(List.of(project));
        List<Project> result = projectService.getInactiveCompanyProjects(1L);
        assertEquals(1, result.size());
    }

}
