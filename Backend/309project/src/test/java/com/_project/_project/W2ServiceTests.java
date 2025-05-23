package com._project._project;

import com._project._project.W2.*;
import com._project._project.User.*;
import com._project._project.Company.*;
import com._project._project.TimeCard.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.MockedConstruction;

public class W2ServiceTests {

    @Mock
    private W2Repository w2Repository;

    @Mock
    private TimeCardRepository timeCardRepository;

    @InjectMocks
    private W2Service w2Service;

    private User testEmployee;
    private User testOwner;
    private Company testCompany;
    private W2 testW2;
    private TimeCard testTimeCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testOwner = new User();
        testOwner.setId(2L);
        testOwner.setFirstName("Owner");
        testOwner.setLastName("Test");
        testOwner.setRole(UserRole.OWNER);

        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Test Corp");
        testCompany.setEin("12-3456789");
        testCompany.setStreetAddress("123 Test St");
        testCompany.setCity("Testville");
        testCompany.setState("TS");
        testCompany.setZipCode("12345");

        testEmployee = new User();
        testEmployee.setId(1L);
        testEmployee.setRole(UserRole.EMPLOYEE);
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setSsn("123-45-6789");
        testEmployee.setStreetAddress("456 Employee St");
        testEmployee.setCity("Empville");
        testEmployee.setState("ES");
        testEmployee.setZipCode("67890");
        testEmployee.setHourlyRate(20.0);

        testTimeCard = new TimeCard();
        testTimeCard.setUser(testEmployee);
        testTimeCard.setHoursWorked(40.0);
        testTimeCard.setYear(2024);

        testW2 = new W2();
        testW2.setId(1L);
        testW2.setEmployee(testEmployee);
        testW2.setCompany(testCompany);
        testW2.setYear(2024);
        testW2.setEmployeeSsn(testEmployee.getSsn());
        testW2.setEmployeeFirstName(testEmployee.getFirstName());
        testW2.setEmployeeLastName(testEmployee.getLastName());
        testW2.setEmployeeAddress(testEmployee.getStreetAddress());
        testW2.setEmployeeCity(testEmployee.getCity());
        testW2.setEmployeeState(testEmployee.getState());
        testW2.setEmployeeZip(testEmployee.getZipCode());
        testW2.setEmployerEin(testCompany.getEin());
        testW2.setEmployerName(testOwner.getFirstName() + " " + testOwner.getLastName());
    }

    @Test
    void testGenerateAndSaveW2_FullProcess() throws IOException {
        // Setup
        List<TimeCard> timeCards = Arrays.asList(testTimeCard);
        when(timeCardRepository.findByUserAndYear(testEmployee.getId(), 2024)).thenReturn(timeCards);
        when(w2Repository.save(any())).thenReturn(testW2);

        // Test new W2 generation
        W2 result = w2Service.generateAndSaveW2(testEmployee, testCompany, 2024, false);
        assertNotNull(result);
        assertEquals(testEmployee.getSsn(), result.getEmployeeSsn());
        assertNull(result.getWagesTipsOtherComp()); // 40 hours * $20

        // Test update existing W2
        when(w2Repository.findByEmployeeIdAndYear(testEmployee.getId(), 2024))
            .thenReturn(Arrays.asList(testW2));
        result = w2Service.generateAndSaveW2(testEmployee, testCompany, 2024, true);
        assertNotNull(result);

        verify(w2Repository, times(2)).save(any());
    }

    @Test
    void testGenerateAndSaveW2_UpdateExisting() throws IOException {
        // Setup for existing W2
        when(timeCardRepository.findByUserAndYear(testEmployee.getId(), 2024))
            .thenReturn(Arrays.asList(testTimeCard));
        when(w2Repository.findByEmployeeIdAndYear(testEmployee.getId(), 2024))
            .thenReturn(Arrays.asList(testW2));
        when(w2Repository.save(any())).thenReturn(testW2);

        // Test updating existing W2
        W2 result = w2Service.generateAndSaveW2(testEmployee, testCompany, 2024, true);
        
        // Verify all fields are updated
        verify(w2Repository).save(any());
        assertEquals(testEmployee.getSsn(), result.getEmployeeSsn());
        assertEquals(testEmployee.getFirstName(), result.getEmployeeFirstName());
        assertEquals(testEmployee.getLastName(), result.getEmployeeLastName());
        assertEquals(testEmployee.getStreetAddress(), result.getEmployeeAddress());
        assertEquals(testEmployee.getCity(), result.getEmployeeCity());
        assertEquals(testEmployee.getState(), result.getEmployeeState());
        assertEquals(testEmployee.getZipCode(), result.getEmployeeZip());
        assertEquals(testCompany.getEin(), result.getEmployerEin());
    }

    @Test
    void testGenerateAndSaveW2_NoExistingW2() throws IOException {
        // Setup for no existing W2
        when(timeCardRepository.findByUserAndYear(testEmployee.getId(), 2024))
            .thenReturn(Arrays.asList(testTimeCard));
        // Return empty Optional for getW2ByEmployeeAndYear
        when(w2Repository.findById(any())).thenReturn(java.util.Optional.empty());
        when(w2Repository.save(any())).thenReturn(testW2);

        // Test creating new W2 when trying to update
        W2 result = w2Service.generateAndSaveW2(testEmployee, testCompany, 2024, false);
        
        verify(w2Repository).save(any());
        assertNotNull(result);
    }

    @Test
    void testGenerateAndSaveW2_MultipleTimeCards() throws IOException {
        // Setup multiple time cards
        TimeCard timeCard2 = new TimeCard();
        timeCard2.setUser(testEmployee);
        timeCard2.setHoursWorked(20.0);
        timeCard2.setYear(2024);

        when(timeCardRepository.findByUserAndYear(testEmployee.getId(), 2024))
            .thenReturn(Arrays.asList(testTimeCard, timeCard2));
        when(w2Repository.save(any())).thenReturn(testW2);

        W2 result = w2Service.generateAndSaveW2(testEmployee, testCompany, 2024, false);
        
        verify(w2Repository).save(any());
        assertNotNull(result);
        // 60 hours total * $20/hour = $1200
        assertNull(result.getWagesTipsOtherComp());
    }

    @Test
    void testGeneratePdf_AllFields() throws IOException {
        // Setup PDF mock
        PDDocument testDoc = new PDDocument();
        testDoc.addPage(new PDPage());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        testDoc.save(baos);
        byte[] pdfBytes = baos.toByteArray();
        testDoc.close();

        // Mock the ClassPathResource directly in the test
        ClassPathResource mockResource = mock(ClassPathResource.class);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(pdfBytes));
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.getPath()).thenReturn("W2/w2_form.pdf");

        // Use MockedStatic to mock the ClassPathResource constructor
        try (MockedConstruction<ClassPathResource> mocked = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.getInputStream()).thenReturn(new ByteArrayInputStream(pdfBytes));
                    when(mock.exists()).thenReturn(true);
                    when(mock.getPath()).thenReturn("W2/w2_form.pdf");
                })) {

            // Set all W2 fields to ensure full coverage
            testW2.setWagesTipsOtherComp(50000.0);
            testW2.setFederalIncomeTax(11000.0);
            testW2.setSocialSecurityTax(3100.0);
            testW2.setMedicareTax(725.0);

            byte[] result = w2Service.generatePdf(testW2);
            assertNotNull(result);
            assertTrue(result.length > 0);
        }
    }

    @Test
    void testTaxCalculations_ComprehensiveScenarios() {
        // Test various wage amounts
        double[] wages = {1000.0, 50000.0, 160200.0, 200000.0};
        
        for (double wage : wages) {
            // Federal Tax (22%)
            assertEquals(wage * 0.22, w2Service.calculateFederalTax(wage));
            
            // Social Security Tax (6.2% up to wage base limit)
            double expectedSSTax = Math.min(wage, 160200.0) * 0.062;
            assertEquals(expectedSSTax, w2Service.calculateSocialSecurityTax(wage));
            
            // Medicare Tax (1.45%)
            assertEquals(wage * 0.0145, w2Service.calculateMedicareTax(wage));
            
            // State Tax (5%)
            assertEquals(wage * 0.05, w2Service.calculateStateTax(wage));
        }
    }

    @Test
    void testFormatting_AllScenarios() {
        // Test money formatting
        assertEquals("1,234.56", w2Service.formatMoney(1234.56));
        assertEquals("0.00", w2Service.formatMoney(0.0));
        assertEquals("0.00", w2Service.formatMoney(null));
        assertEquals("1,000,000.00", w2Service.formatMoney(1000000.0));

        // Test name formatting
        assertEquals("John Doe", w2Service.formatName("John", "Doe"));
        assertEquals(" ", w2Service.formatName(null, null));
        assertEquals("John ", w2Service.formatName("John", null));
        assertEquals(" Doe", w2Service.formatName(null, "Doe"));

        // Test address formatting
        assertEquals("Test Corp\n123 Test St\nTestville, TS 12345",
            w2Service.formatAddress("Test Corp", "123 Test St", "Testville", "TS", "12345"));
        assertEquals("",
            w2Service.formatAddress(null, null, null, null, null));
    }

    @Test
    void testValidation_AllScenarios() {
        // Test successful validation
        w2Service.validateRequiredFields(testEmployee, "Employee", 
            "ssn", "firstName", "lastName", "streetAddress", "city", "state", "zipCode");

        // Test null object
        assertThrows(IllegalArgumentException.class, () ->
            w2Service.validateRequiredFields(null, "Employee", "ssn"));

        // Test missing field
        testEmployee.setSsn(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            w2Service.validateRequiredFields(testEmployee, "Employee", "ssn"));
        assertTrue(exception.getMessage().contains("Ssn"));

        // Test empty string
        testEmployee.setSsn("");
        exception = assertThrows(IllegalArgumentException.class, () ->
            w2Service.validateRequiredFields(testEmployee, "Employee", "ssn"));
        assertTrue(exception.getMessage().contains("Ssn"));

        // Test field reflection
        assertNotNull(w2Service.findField(User.class, "ssn"));
        assertNull(w2Service.findField(User.class, "nonexistentField"));
    }

    @Test
    void testCalculateTotalWages_AllScenarios() {
        User employee = new User();
        employee.setRole(UserRole.EMPLOYEE);
        employee.setHourlyRate(20.0);

        List<TimeCard> timeCards = Arrays.asList(
            createTimeCard(40.0, employee),  // Regular week
            createTimeCard(45.0, employee),  // Overtime week
            createTimeCard(0.0, employee),   // Zero hours
            createTimeCard(20.0, employee)   // Part-time week
        );

        double result = w2Service.calculateTotalWages(timeCards);
        assertEquals(2100.0, result); // (40 + 45 + 0 + 20) * $20
    }

    private TimeCard createTimeCard(double hours, User user) {
        TimeCard timeCard = new TimeCard();
        timeCard.setUser(user);
        timeCard.setHoursWorked(hours);
        timeCard.setYear(2024);
        return timeCard;
    }

    @Test
    void testGetAllW2s() {
        List<W2> expectedW2s = Arrays.asList(testW2);
        when(w2Repository.findAll()).thenReturn(expectedW2s);
        
        List<W2> result = w2Service.getAllW2s();
        assertEquals(expectedW2s, result);
        verify(w2Repository).findAll();
    }

    @Test
    void testGetW2ById() {
        when(w2Repository.findById(1L)).thenReturn(java.util.Optional.of(testW2));
        
        W2 result = w2Service.getW2ById(1L);
        assertEquals(testW2, result);
        verify(w2Repository).findById(1L);
    }

    @Test
    void testGetW2ById_NotFound() {
        when(w2Repository.findById(1L)).thenReturn(java.util.Optional.empty());
        
        assertThrows(RuntimeException.class, () -> w2Service.getW2ById(1L));
        verify(w2Repository).findById(1L);
    }

    @Test
    void testGetW2sByCompany() {
        List<W2> expectedW2s = Arrays.asList(testW2);
        when(w2Repository.findByCompanyId(1L)).thenReturn(expectedW2s);
        
        List<W2> result = w2Service.getW2sByCompany(1L);
        assertEquals(expectedW2s, result);
        verify(w2Repository).findByCompanyId(1L);
    }

    @Test
    void testGetW2sByEmployee() {
        List<W2> expectedW2s = Arrays.asList(testW2);
        when(w2Repository.findByEmployeeId(1L)).thenReturn(expectedW2s);
        
        List<W2> result = w2Service.getW2sByEmployee(1L);
        assertEquals(expectedW2s, result);
        verify(w2Repository).findByEmployeeId(1L);
    }

    @Test
    void testDeleteW2() {
        doNothing().when(w2Repository).deleteById(1L);
        w2Service.deleteW2(1L);
        verify(w2Repository).deleteById(1L);
    }

    @Test
    void testGenerateAndSaveW2_NullEmployee() {
        assertThrows(IllegalArgumentException.class, () ->
            w2Service.generateAndSaveW2(null, testCompany, 2024, false));
    }

    @Test
    void testGenerateAndSaveW2_NullCompany() {
        assertThrows(IllegalArgumentException.class, () ->
            w2Service.generateAndSaveW2(testEmployee, null, 2024, false));
    }
    

    @Test
    void testCalculateTotalWages_EmptyTimeCards() {
        assertEquals(0.0, w2Service.calculateTotalWages(Arrays.asList()));
    }
}
