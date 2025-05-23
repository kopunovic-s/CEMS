package com._project._project;

import com._project._project.Company.Company;
import com._project._project.Payroll.*;
import com._project._project.Payroll.Responses.EmployeeWeekResponse;
import com._project._project.Payroll.Responses.PayrollWeekResponse;
import com._project._project.Perminissions.PermissionsService;
import com._project._project.TimeCard.TimeCard;
import com._project._project.TimeCard.TimeCardRepository;
import com._project._project.User.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PayrollTests {

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private TimeCardRepository timeCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private PermissionsService permissionsService;

    @InjectMocks
    private PayrollService payrollService;

    @InjectMocks
    private PayrollController payrollController;

    // Test data
    private User ownerUser;
    private User executiveUser;
    private User managerUser;
    private User employeeUser;
    private Company testCompany;
    private List<User> companyUsers;
    private TimeCard timeCard1;
    private TimeCard timeCard2;
    private Payroll payroll;
    private String testDate;
    private int weekNumber;
    private int year;

    @BeforeAll
    void setupTestData() {
        // Create test company
        testCompany = new Company("Test Company");
        testCompany.setId(1L);
        
        // Create test users
        ownerUser = new User("John", "Owner", "owner@test.com", "password", UserRole.OWNER);
        ownerUser.setCompany(testCompany);
        ownerUser.setHourlyRate(50.0);
        
        executiveUser = new User("Jane", "Executive", "executive@test.com", "password", UserRole.EXECUTIVE);
        executiveUser.setCompany(testCompany);
        executiveUser.setHourlyRate(40.0);
        
        managerUser = new User("Bob", "Manager", "manager@test.com", "password", UserRole.MANAGER);
        managerUser.setCompany(testCompany);
        managerUser.setHourlyRate(30.0);
        
        employeeUser = new User("Alice", "Employee", "employee@test.com", "password", UserRole.EMPLOYEE);
        employeeUser.setCompany(testCompany);
        employeeUser.setHourlyRate(20.0);
        
        // Set up company users list
        companyUsers = new ArrayList<>();
        companyUsers.add(ownerUser);
        companyUsers.add(executiveUser);
        companyUsers.add(managerUser);
        companyUsers.add(employeeUser);
        
        // Add users to company
        testCompany.setUsers(companyUsers);
        
        // Create test date
        testDate = "2023-11-01";
        LocalDate localDate = LocalDate.parse(testDate);
        weekNumber = localDate.get(WeekFields.SUNDAY_START.weekOfWeekBasedYear());
        year = localDate.getYear();
        
        // Create test time cards
        LocalDateTime clockIn1 = LocalDateTime.of(2023, 11, 1, 9, 0);
        LocalDateTime clockOut1 = LocalDateTime.of(2023, 11, 1, 17, 0);
        timeCard1 = new TimeCard();
        timeCard1.setId(1L);
        timeCard1.setUser(employeeUser);
        timeCard1.setClockIn(clockIn1);
        timeCard1.setClockOut(clockOut1);
        timeCard1.setWeekNumber(weekNumber);
        timeCard1.setYear(year);
        timeCard1.setHoursWorked(8.0);
        
        LocalDateTime clockIn2 = LocalDateTime.of(2023, 11, 2, 9, 0);
        LocalDateTime clockOut2 = LocalDateTime.of(2023, 11, 2, 17, 0);
        timeCard2 = new TimeCard();
        timeCard2.setId(2L);
        timeCard2.setUser(employeeUser);
        timeCard2.setClockIn(clockIn2);
        timeCard2.setClockOut(clockOut2);
        timeCard2.setWeekNumber(weekNumber);
        timeCard2.setYear(year);
        timeCard2.setHoursWorked(8.0);
        
        // Create test payroll
        payroll = new Payroll(employeeUser, weekNumber, year);
        payroll.setId(1L);
        payroll.setTotalHours(16.0);
        payroll.setTotalPay(320.0);
        payroll.setIsPaid(false);
        
        List<TimeCard> timeCards = new ArrayList<>();
        timeCards.add(timeCard1);
        timeCards.add(timeCard2);
        payroll.setTimeCards(timeCards);
        timeCard1.setPayroll(payroll);
        timeCard2.setPayroll(payroll);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up common mock behaviors
        when(userRepository.findById(1L)).thenReturn(ownerUser);
        when(userRepository.findById(2L)).thenReturn(executiveUser);
        when(userRepository.findById(3L)).thenReturn(managerUser);
        when(userRepository.findById(4L)).thenReturn(employeeUser);
        
        List<TimeCard> employeeTimeCards = new ArrayList<>();
        employeeTimeCards.add(timeCard1);
        employeeTimeCards.add(timeCard2);
        
        when(timeCardRepository.findByUserAndWeekNumberAndYear(eq(employeeUser), eq(weekNumber), eq(year)))
            .thenReturn(employeeTimeCards);
        
        when(payrollRepository.findByUserAndWeekNumberAndYear(eq(employeeUser), eq(weekNumber), eq(year)))
            .thenReturn(payroll);
        
        when(payrollRepository.save(any(Payroll.class))).thenReturn(payroll);
        
        List<User> employees = new ArrayList<>();
        employees.add(managerUser);
        employees.add(employeeUser);
        when(userService.getCompanyUsers(anyLong())).thenReturn(employees);
        
        when(userService.getUser(anyLong(), eq(4L))).thenReturn(employeeUser);
        
        // Initialize services in controller
        ReflectionTestUtils.setField(payrollController, "payrollService", payrollService);
        ReflectionTestUtils.setField(payrollController, "userRepository", userRepository);
        ReflectionTestUtils.setField(payrollController, "permissionsService", permissionsService);
        ReflectionTestUtils.setField(payrollController, "timeCardRepository", timeCardRepository);
        ReflectionTestUtils.setField(payrollController, "userService", userService);
        
        // Initialize repositories in service
        ReflectionTestUtils.setField(payrollService, "payrollRepository", payrollRepository);
        ReflectionTestUtils.setField(payrollService, "timeCardRepository", timeCardRepository);
        ReflectionTestUtils.setField(payrollService, "userService", userService);
        ReflectionTestUtils.setField(payrollService, "userRepository", userRepository);
    }

    // ===== Payroll.java Tests =====
    
    @Test
    void testPayrollConstructor() {
        // Setup
        Payroll newPayroll = new Payroll(employeeUser, weekNumber, year);
        
        // Verify
        assertEquals(employeeUser, newPayroll.getUser());
        assertEquals(weekNumber, newPayroll.getWeekNumber());
        assertEquals(year, newPayroll.getYear());
        assertEquals(employeeUser.getHourlyRate(), newPayroll.getHourlyRate());
        assertFalse(newPayroll.getIsPaid());
        assertNotNull(newPayroll.getProcessedDate());
        assertNotNull(newPayroll.getTimeCards());
        assertTrue(newPayroll.getTimeCards().isEmpty());
    }
    
    @Test
    void testEmptyPayrollConstructor() {
        // Setup
        Payroll emptyPayroll = new Payroll();
        
        // Verify
        assertEquals(0, emptyPayroll.getId());
        assertNull(emptyPayroll.getUser());
        assertNull(emptyPayroll.getTimeCards());
        assertEquals(0, emptyPayroll.getWeekNumber());
        assertEquals(0, emptyPayroll.getYear());
        assertNull(emptyPayroll.getTotalHours());
        assertNull(emptyPayroll.getHourlyRate());
        assertNull(emptyPayroll.getTotalPay());
        assertNull(emptyPayroll.getProcessedDate());
        assertFalse(emptyPayroll.getIsPaid());
    }
    
    @Test
    void testPayrollGettersAndSetters() {
        // Setup
        Payroll testPayroll = new Payroll();
        
        // Execute
        testPayroll.setId(2L);
        testPayroll.setUser(managerUser);
        testPayroll.setWeekNumber(45);
        testPayroll.setYear(2023);
        testPayroll.setTotalHours(40.0);
        testPayroll.setHourlyRate(30.0);
        testPayroll.setTotalPay(1200.0);
        LocalDateTime processedDate = LocalDateTime.now();
        testPayroll.setProcessedDate(processedDate);
        testPayroll.setIsPaid(true);
        
        List<TimeCard> timeCards = new ArrayList<>();
        timeCards.add(timeCard1);
        testPayroll.setTimeCards(timeCards);
        
        // Verify
        assertEquals(2L, testPayroll.getId());
        assertEquals(managerUser, testPayroll.getUser());
        assertEquals(45, testPayroll.getWeekNumber());
        assertEquals(2023, testPayroll.getYear());
        assertEquals(40.0, testPayroll.getTotalHours());
        assertEquals(30.0, testPayroll.getHourlyRate());
        assertEquals(1200.0, testPayroll.getTotalPay());
        assertEquals(processedDate, testPayroll.getProcessedDate());
        assertTrue(testPayroll.getIsPaid());
        assertEquals(1, testPayroll.getTimeCards().size());
        assertEquals(timeCard1, testPayroll.getTimeCards().get(0));
    }
    
    // ===== PayrollService.java Tests =====
    
    @Test
    void testCalculateWeekSummary() {
        // Execute
        Payroll result = payrollService.calculateWeekSummary(employeeUser, weekNumber, year);
        
        // Verify
        assertEquals(payroll, result);
        assertEquals(16.0, result.getTotalHours());
        assertEquals(320.0, result.getTotalPay());
        verify(timeCardRepository).findByUserAndWeekNumberAndYear(employeeUser, weekNumber, year);
        verify(payrollRepository).findByUserAndWeekNumberAndYear(employeeUser, weekNumber, year);
        verify(payrollRepository).save(payroll);
    }
    
    @Test
    void testCreateOrUpdatePayroll_NewPayroll() {
        // Setup
        when(payrollRepository.findByUserAndWeekNumberAndYear(eq(managerUser), eq(weekNumber), eq(year)))
            .thenReturn(null);
        
        Payroll newPayroll = new Payroll(managerUser, weekNumber, year);
        newPayroll.setTotalHours(0.0);
        newPayroll.setTotalPay(0.0);
        when(payrollRepository.save(any(Payroll.class))).thenReturn(newPayroll);
        
        // Execute
        Payroll result = payrollService.createOrUpdatePayroll(managerUser, weekNumber, year, false);
        
        // Verify
        assertNotNull(result);
        assertEquals(managerUser, result.getUser());
        assertEquals(weekNumber, result.getWeekNumber());
        assertEquals(year, result.getYear());
        verify(payrollRepository).findByUserAndWeekNumberAndYear(managerUser, weekNumber, year);
        verify(timeCardRepository).findByUserAndWeekNumberAndYear(managerUser, weekNumber, year);
        verify(payrollRepository).save(any(Payroll.class));
    }
    
    @Test
    void testCreateOrUpdatePayroll_ExistingPayroll() {
        // Execute
        Payroll result = payrollService.createOrUpdatePayroll(employeeUser, weekNumber, year, true);
        
        // Verify
        assertEquals(payroll, result);
        assertTrue(result.getIsPaid());
        verify(payrollRepository).findByUserAndWeekNumberAndYear(employeeUser, weekNumber, year);
        verify(timeCardRepository).findByUserAndWeekNumberAndYear(employeeUser, weekNumber, year);
        verify(payrollRepository).save(payroll);
    }
    
    @Test
    void testGetWeekPayroll_AsOwner() {
        // Setup
        List<Payroll> payrolls = new ArrayList<>();
        payrolls.add(payroll);
        
        // Execute
        PayrollWeekResponse result = payrollService.getWeekPayroll(1L, testDate);
        
        // Verify
        assertNotNull(result);
        assertEquals(weekNumber, result.getWeekNumber());
        assertEquals(year, result.getYear());
        assertEquals(2, result.getEmployees().size());
        verify(userRepository).findById(1L);
        verify(userService).getCompanyUsers(1L);
    }
    
    @Test
    void testGetWeekPayroll_AsEmployee() {
        // Setup
        when(userService.getCompanyUsers(4L)).thenReturn(List.of(employeeUser));
        
        // Execute
        PayrollWeekResponse result = payrollService.getWeekPayroll(4L, testDate);
        
        // Verify
        assertNotNull(result);
        assertEquals(weekNumber, result.getWeekNumber());
        assertEquals(year, result.getYear());
        assertEquals(1, result.getEmployees().size());
        verify(userRepository).findById(4L);
    }
    
    @Test
    void testUpdatePayrollStatus() {
        // Execute
        List<Payroll> results = payrollService.updatePayrollStatus(1L, testDate, true);
        
        // Verify
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(userService).getCompanyUsers(1L);
    }
    
    // ===== PayrollController.java Tests =====
    
    @Test
    void testGetWeekSummary_Success() {
        // Setup
        when(permissionsService.NoUserExists(ownerUser, ownerUser)).thenReturn(false);
        when(permissionsService.IsEmployee(ownerUser)).thenReturn(false);
        
        // Execute
        ResponseEntity<?> response = payrollController.getWeekSummary(1L, testDate);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof PayrollWeekResponse);
        verify(permissionsService).NoUserExists(ownerUser, ownerUser);
        verify(permissionsService).IsEmployee(ownerUser);
    }
    
    @Test
    void testGetWeekSummary_UserNotFound() {
        // Setup
        when(userRepository.findById(999L)).thenReturn(null);
        when(permissionsService.NoUserExists(null, null)).thenReturn(true);
        
        // Execute
        ResponseEntity<?> response = payrollController.getWeekSummary(999L, testDate);
        
        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User ID 999 not found", response.getBody());
        verify(userRepository).findById(999L);
        verify(permissionsService).NoUserExists(null, null);
    }
    
    @Test
    void testGetWeekSummary_Forbidden() {
        // Setup
        when(permissionsService.NoUserExists(employeeUser, employeeUser)).thenReturn(false);
        when(permissionsService.IsEmployee(employeeUser)).thenReturn(true);
        
        // Execute
        ResponseEntity<?> response = payrollController.getWeekSummary(4L, testDate);
        
        // Verify
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Only OWNER, EXECUTIVE, or MANAGER can view all payrolls", response.getBody());
        verify(userRepository).findById(4L);
        verify(permissionsService).NoUserExists(employeeUser, employeeUser);
        verify(permissionsService).IsEmployee(employeeUser);
    }
    
    @Test
    void testGetEmployeeWeekDetails_Success() {
        // Setup
        when(permissionsService.NoUserExists(ownerUser, ownerUser)).thenReturn(false);
        when(permissionsService.NoManagerPermissions(ownerUser, employeeUser)).thenReturn(false);
        
        // Execute
        ResponseEntity<?> response = payrollController.getEmployeeWeekDetails(1L, 4L, testDate);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof EmployeeWeekResponse);
        verify(userRepository).findById(1L);
        verify(userRepository).findById(4L);
        verify(permissionsService).NoUserExists(ownerUser, ownerUser);
        verify(permissionsService).NoManagerPermissions(ownerUser, employeeUser);
    }
    
    @Test
    void testGetEmployeeWeekDetails_UserNotFound() {
        // Setup
        when(userRepository.findById(999L)).thenReturn(null);
        when(permissionsService.NoUserExists(null, null)).thenReturn(true);
        
        // Execute
        ResponseEntity<?> response = payrollController.getEmployeeWeekDetails(999L, 4L, testDate);
        
        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User ID 999 not found", response.getBody());
        verify(userRepository).findById(999L);
        verify(permissionsService).NoUserExists(null, null);
    }
    
    @Test
    void testGetEmployeeWeekDetails_Forbidden() {
        // Setup
        when(permissionsService.NoUserExists(employeeUser, employeeUser)).thenReturn(false);
        when(permissionsService.NoManagerPermissions(employeeUser, ownerUser)).thenReturn(true);
        
        // Execute
        ResponseEntity<?> response = payrollController.getEmployeeWeekDetails(4L, 1L, testDate);
        
        // Verify
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Employees can only view their own records", response.getBody());
        verify(userRepository).findById(4L);
        verify(userRepository).findById(1L);
        verify(permissionsService).NoUserExists(employeeUser, employeeUser);
        verify(permissionsService).NoManagerPermissions(employeeUser, ownerUser);
    }
    
    @Test
    void testMarkWeekAsPaid_Success() {
        // Setup
        when(permissionsService.NoSinglePermissions(ownerUser)).thenReturn(false);
        
        // Execute
        ResponseEntity<?> response = payrollController.markWeekAsPaid(1L, testDate);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userRepository).findById(1L);
        verify(permissionsService).NoSinglePermissions(ownerUser);
    }
    
    @Test
    void testMarkWeekAsPaid_UserNotFound() {
        // Setup
        when(userRepository.findById(999L)).thenReturn(null);
        
        // Execute
        ResponseEntity<?> response = payrollController.markWeekAsPaid(999L, testDate);
        
        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User ID 999 not found", response.getBody());
        verify(userRepository).findById(999L);
    }
    
    @Test
    void testMarkWeekAsPaid_Forbidden() {
        // Setup
        when(permissionsService.NoSinglePermissions(employeeUser)).thenReturn(true);
        
        // Execute
        ResponseEntity<?> response = payrollController.markWeekAsPaid(4L, testDate);
        
        // Verify
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Only OWNER or EXECUTIVE can mark payroll as paid", response.getBody());
        verify(userRepository).findById(4L);
        verify(permissionsService).NoSinglePermissions(employeeUser);
    }
    
    @Test
    void testMarkWeekAsUnpaid_Success() {
        // Setup
        when(permissionsService.NoSinglePermissions(ownerUser)).thenReturn(false);
        
        // Execute
        ResponseEntity<?> response = payrollController.markWeekAsUnpaid(1L, testDate);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userRepository).findById(1L);
        verify(permissionsService).NoSinglePermissions(ownerUser);
    }
    
    @Test
    void testMarkWeekAsUnpaid_UserNotFound() {
        // Setup
        when(userRepository.findById(999L)).thenReturn(null);
        
        // Execute
        ResponseEntity<?> response = payrollController.markWeekAsUnpaid(999L, testDate);
        
        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User ID 999 not found", response.getBody());
        verify(userRepository).findById(999L);
    }
    
    @Test
    void testMarkWeekAsUnpaid_Forbidden() {
        // Setup
        when(permissionsService.NoSinglePermissions(employeeUser)).thenReturn(true);
        
        // Execute
        ResponseEntity<?> response = payrollController.markWeekAsUnpaid(4L, testDate);
        
        // Verify
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Only OWNER or EXECUTIVE can mark payroll as unpaid", response.getBody());
        verify(userRepository).findById(4L);
        verify(permissionsService).NoSinglePermissions(employeeUser);
    }

    // ===== Payroll Response Classes Tests =====
    


    @Test
    void testPayrollWeekResponse_UserInfo() {
        // Setup & Execute
        PayrollWeekResponse.EmployeeSummary.UserInfo userInfo = 
            new PayrollWeekResponse.EmployeeSummary.UserInfo(employeeUser);
        
        // Verify
        assertEquals("Alice", userInfo.getFirstName());
        assertEquals("Employee", userInfo.getLastName());
        assertEquals(0, userInfo.getId());
        assertEquals("EMPLOYEE", userInfo.getRole());
    }
    
    @Test
    void testPayrollWeekResponse_EmployeeSummary() {
        // Setup & Execute
        PayrollWeekResponse.EmployeeSummary summary = 
            new PayrollWeekResponse.EmployeeSummary(payroll);
        
        // Verify
        assertNotNull(summary.getUser());
        assertEquals("Alice", summary.getUser().getFirstName());
        assertEquals("Employee", summary.getUser().getLastName());
        assertEquals(16.0, summary.getTotalHours());
        assertEquals(20.0, summary.getHourlyRate());
        assertEquals(320.0, summary.getTotalPay());
    }
    
    @Test
    void testPayrollWeekResponse() {
        // Setup
        int testWeekNumber = 44;
        int testYear = 2023;
        LocalDateTime processedDate = LocalDateTime.now();
        boolean isPaid = false;
        
        List<Payroll> payrolls = new ArrayList<>();
        payrolls.add(payroll);
        
        // Execute
        PayrollWeekResponse response = new PayrollWeekResponse(
            testWeekNumber, testYear, processedDate, isPaid, payrolls);
        
        // Verify
        assertEquals(testWeekNumber, response.getWeekNumber());
        assertEquals(testYear, response.getYear());
        assertEquals(processedDate, response.getProcessedDate());
        assertEquals(isPaid, response.getIsPaid());
        assertEquals(1, response.getEmployees().size());
        
        PayrollWeekResponse.EmployeeSummary summary = response.getEmployees().get(0);
        assertEquals("Alice", summary.getUser().getFirstName());
        assertEquals("Employee", summary.getUser().getLastName());
        assertEquals(16.0, summary.getTotalHours());
        assertEquals(20.0, summary.getHourlyRate());
        assertEquals(320.0, summary.getTotalPay());
    }
    
    @Test
    void testPayrollWeekResponse_MultipleEmployees() {
        // Setup
        int testWeekNumber = 44;
        int testYear = 2023;
        LocalDateTime processedDate = LocalDateTime.now();
        boolean isPaid = true;
        
        // Create a second payroll for manager
        Payroll managerPayroll = new Payroll(managerUser, testWeekNumber, testYear);
        managerPayroll.setId(2L);
        managerPayroll.setTotalHours(20.0);
        managerPayroll.setTotalPay(600.0);
        managerPayroll.setIsPaid(true);
        
        List<Payroll> payrolls = new ArrayList<>();
        payrolls.add(payroll);
        payrolls.add(managerPayroll);
        
        // Execute
        PayrollWeekResponse response = new PayrollWeekResponse(
            testWeekNumber, testYear, processedDate, isPaid, payrolls);
        
        // Verify
        assertEquals(testWeekNumber, response.getWeekNumber());
        assertEquals(testYear, response.getYear());
        assertEquals(processedDate, response.getProcessedDate());
        assertEquals(isPaid, response.getIsPaid());
        assertEquals(2, response.getEmployees().size());
        
        // Verify first employee (Alice)
        PayrollWeekResponse.EmployeeSummary summary1 = response.getEmployees().get(0);
        assertEquals("Alice", summary1.getUser().getFirstName());
        assertEquals("Employee", summary1.getUser().getLastName());
        assertEquals(16.0, summary1.getTotalHours());
        assertEquals(20.0, summary1.getHourlyRate());
        assertEquals(320.0, summary1.getTotalPay());
        
        // Verify second employee (Bob)
        PayrollWeekResponse.EmployeeSummary summary2 = response.getEmployees().get(1);
        assertEquals("Bob", summary2.getUser().getFirstName());
        assertEquals("Manager", summary2.getUser().getLastName());
        assertEquals(20.0, summary2.getTotalHours());
        assertEquals(30.0, summary2.getHourlyRate());
        assertEquals(600.0, summary2.getTotalPay());
    }
} 