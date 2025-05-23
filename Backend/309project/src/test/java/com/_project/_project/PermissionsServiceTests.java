package com._project._project;

import com._project._project.Company.Company;
import com._project._project.User.User;
import com._project._project.User.UserRole;
import com._project._project.Perminissions.PermissionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionsServiceTests {

    private PermissionsService permissionsService;
    private User owner;
    private User executive;
    private User manager;
    private User employee;
    private Company company1;
    private Company company2;

    @BeforeEach
    void setUp() {
        permissionsService = new PermissionsService();

        company1 = new Company();
        company1.setId(1L);

        company2 = new Company();
        company2.setId(2L);

        owner = new User();
        owner.setId(1L);
        owner.setRole(UserRole.OWNER);
        owner.setCompany(company1);

        executive = new User();
        executive.setId(2L);
        executive.setRole(UserRole.EXECUTIVE);
        executive.setCompany(company1);

        manager = new User();
        manager.setId(3L);
        manager.setRole(UserRole.MANAGER);
        manager.setCompany(company1);

        employee = new User();
        employee.setId(4L);
        employee.setRole(UserRole.EMPLOYEE);
        employee.setCompany(company1);
    }

    @Test
    void testPermissions() {
        // Test NoDoublePermissions
        assertFalse(permissionsService.NoDoublePermissions(owner, employee));
        assertFalse(permissionsService.NoDoublePermissions(executive, employee));
        assertTrue(permissionsService.NoDoublePermissions(manager, employee));
        assertTrue(permissionsService.NoDoublePermissions(employee, manager));

        // Test NoManagerPermissions
        assertFalse(permissionsService.NoManagerPermissions(owner, employee));
        assertFalse(permissionsService.NoManagerPermissions(manager, employee));
        assertTrue(permissionsService.NoManagerPermissions(employee, manager));

        // Test NoSinglePermissions
        assertFalse(permissionsService.NoSinglePermissions(owner));
        assertFalse(permissionsService.NoSinglePermissions(executive));
        assertTrue(permissionsService.NoSinglePermissions(manager));
        assertTrue(permissionsService.NoSinglePermissions(employee));

        // Test IsEmployee
        assertFalse(permissionsService.IsEmployee(owner));
        assertTrue(permissionsService.IsEmployee(employee));

        // Test NoCompanyEditPermissions
        assertFalse(permissionsService.NoCompanyEditPermissions(owner, company1));
        assertTrue(permissionsService.NoCompanyEditPermissions(employee, company2));

        // Test NoUserExists
        assertTrue(permissionsService.NoUserExists(null, employee));
        assertTrue(permissionsService.NoUserExists(employee, null));
        assertFalse(permissionsService.NoUserExists(employee, employee));

        // Test NoCompanyExists
        assertTrue(permissionsService.NoCompanyExists(null, company1));
        assertTrue(permissionsService.NoCompanyExists(employee, null));
        assertFalse(permissionsService.NoCompanyExists(employee, company1));
    }
} 