package com._project._project.W2;

import com._project._project.User.User;
import com._project._project.TimeCard.TimeCard;
import com._project._project.TimeCard.TimeCardRepository;
import com._project._project.Company.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.ClassPathResource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import java.util.List;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.lang.reflect.Field;
import java.util.ArrayList;

@Service
public class W2Service {
    
    @Autowired
    public TimeCardRepository timeCardRepository;

    @Autowired
    public W2Repository w2Repository;

    public W2 generateAndSaveW2(User employee, Company company, int year, boolean update) throws IOException {
        // Validate required information
        validateRequiredFields(employee, "Employee", 
            "ssn", "firstName", "lastName", "streetAddress", "city", "state", "zipCode");
        validateRequiredFields(company, "Company", 
            "ein", "streetAddress", "city", "state", "zipCode");
        
        // Create new W2
        W2 w2;
        if (update) {
            w2 = getW2ByEmployeeAndYear(employee.getId(), year);
        } else {
            w2 = new W2();
        }
        
        // Set relationships
        w2.setEmployee(employee);
        w2.setCompany(company);
        w2.setYear(year);
        
        // Set employee info
        w2.setEmployeeSsn(employee.getSsn());
        w2.setEmployeeFirstName(employee.getFirstName());
        w2.setEmployeeLastName(employee.getLastName());
        w2.setEmployeeAddress(employee.getStreetAddress());
        w2.setEmployeeCity(employee.getCity());
        w2.setEmployeeState(employee.getState());
        w2.setEmployeeZip(employee.getZipCode());

        // Set employer info
        User owner = company.getOwner();
        String ownerName = owner != null ? 
            owner.getFirstName() + " " + owner.getLastName() : 
            "Company Owner";
            
        w2.setEmployerEin(company.getEin());
        w2.setEmployerName(ownerName);
        w2.setEmployerAddress(company.getStreetAddress());
        w2.setEmployerCity(company.getCity());
        w2.setEmployerState(company.getState());
        w2.setEmployerZip(company.getZipCode());

        // Calculate wages and taxes
        List<TimeCard> timeCards = timeCardRepository.findByUserAndYear(employee.getId(), year);
        double totalWages = calculateTotalWages(timeCards);
        
        w2.setWagesTipsOtherComp(totalWages);
        w2.setFederalIncomeTax(calculateFederalTax(totalWages));
        w2.setSocialSecurityWages(totalWages);
        w2.setSocialSecurityTax(calculateSocialSecurityTax(totalWages));
        w2.setMedicareWages(totalWages);
        w2.setMedicareTax(calculateMedicareTax(totalWages));
        
        w2.setStateCode(company.getState());
        w2.setStateId(company.getEin());
        w2.setStateWages(totalWages);
        w2.setStateIncomeTax(calculateStateTax(totalWages));

        // Save to database
        return w2Repository.save(w2);
    }

    public byte[] generatePdf(W2 w2) throws IOException {
        // Use the new template file name
        ClassPathResource resource = new ClassPathResource("W2/w2_form.pdf");
        
        // Add logging to help debug file access issues
        System.out.println("Looking for PDF template at: " + resource.getPath());
        if (!resource.exists()) {
            throw new IOException("PDF template file not found: " + resource.getPath());
        }
        
        PDDocument document = PDDocument.load(resource.getInputStream());
        PDPage page = document.getPage(0);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
        
        // Set font
        PDType1Font font = PDType1Font.HELVETICA;
        float fontSize = 9;  
        contentStream.setFont(font, fontSize);

        try {
            // Box a - SSN
            drawText(contentStream, w2.getEmployeeSsn(), 162, 737);
            
            // Box b - EIN
            drawText(contentStream, w2.getEmployerEin(), 48, 712);
            
            // Box c - Employer info    
            drawMultilineText(contentStream, formatAddress(
                w2.getEmployerName(),   
                w2.getEmployerAddress(),
                w2.getEmployerCity(),
                w2.getEmployerState(),
                w2.getEmployerZip()
            ), 43, 687);
            
            // Box e - Employee name
            drawText(contentStream, formatName(w2.getEmployeeFirstName(), w2.getEmployeeLastName()), 43, 590);
            
            // Box f - Employee address
            drawMultilineText(contentStream, formatAddress(
                null,
                w2.getEmployeeAddress(),
                w2.getEmployeeCity(),
                w2.getEmployeeState(),
                w2.getEmployeeZip()
            ), 43, 550);
            
            // Right side boxes
            // Box 1 - Wages
            drawText(contentStream, formatMoney(w2.getWagesTipsOtherComp()), 355, 712);
            
            // Box 2 - Federal Tax
            drawText(contentStream, formatMoney(w2.getFederalIncomeTax()), 475, 712);
            
            // Box 3 - Social Security Wages
            drawText(contentStream, formatMoney(w2.getSocialSecurityWages()), 355, 688);
            
            // Box 4 - Social Security Tax
            drawText(contentStream, formatMoney(w2.getSocialSecurityTax()), 475, 688);
            
            // Box 5 - Medicare Wages
            drawText(contentStream, formatMoney(w2.getMedicareWages()), 355, 665);
            
            // Box 6 - Medicare Tax
            drawText(contentStream, formatMoney(w2.getMedicareTax()), 475, 665);

            // State Information
            // Box 15 - State & ID
            drawText(contentStream, w2.getStateCode(), 40, 482);
            drawText(contentStream, w2.getStateId(), 70, 482);
            
            // Box 16 - State Wages
            drawText(contentStream, formatMoney(w2.getStateWages()), 203, 482);
            
            // Box 17 - State Tax
            drawText(contentStream, formatMoney(w2.getStateIncomeTax()), 290, 482);

            // Box 18 - State Tax Withheld
            drawText(contentStream, formatMoney(w2.getStateWages()), 370, 482);

            // Box 19 - Local Income Tax / State Income Tax
            drawText(contentStream, formatMoney(w2.getStateIncomeTax()), 460, 482);

            contentStream.close();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } finally {
            document.close();
        }
    }

    public void drawText(PDPageContentStream contentStream, String text, float x, float y) throws IOException {
        if (text != null) {
            contentStream.beginText();
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text);
            contentStream.endText();
        }
    }

    public void drawMultilineText(PDPageContentStream contentStream, String text, float x, float y) throws IOException {
        if (text == null) return;
        
        String[] lines = text.split("\n");
        float leading = 12;
        
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        
        for (String line : lines) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -leading);
        }
        
        contentStream.endText();
    }

    public String formatMoney(Double amount) {
        if (amount == null) return "0.00";
        return new DecimalFormat("#,##0.00").format(amount);
    }

    public String formatName(String firstName, String lastName) {
        firstName = firstName != null ? firstName : "";
        lastName = lastName != null ? lastName : "";
        return firstName + " " + lastName;
    }

    public String formatAddress(String name, String address, String city, String state, String zip) {
        StringBuilder sb = new StringBuilder();
        if (name != null && !name.isEmpty()) {
            sb.append(name).append("\n");
        }
        if (address != null) {
            sb.append(address).append("\n");
        }
        if (city != null || state != null || zip != null) {
            sb.append(city != null ? city : "").append(", ")
              .append(state != null ? state : "").append(" ")
              .append(zip != null ? zip : "");
        }
        return sb.toString();
    }

    public double calculateTotalWages(List<TimeCard> timeCards) {
        return timeCards.stream()
                .mapToDouble(tc -> tc.getHoursWorked() * tc.getUser().getHourlyRate())
                .sum();
    }

    public double calculateFederalTax(double wages) {
        return wages * 0.22;  // Example: 22% federal tax rate
    }

    public double calculateSocialSecurityTax(double wages) {
        return Math.min(wages, 160200) * 0.062;  // 6.2% up to wage base limit
    }

    public double calculateMedicareTax(double wages) {
        return wages * 0.0145;  // 1.45% Medicare tax
    }

    public double calculateStateTax(double wages) {
        return wages * 0.05;  // Example: 5% state tax rate
    }

    public List<W2> getAllW2s() {
        return w2Repository.findAll();
    }

    public W2 getW2ById(Long id) {
        return w2Repository.findById(id)
            .orElseThrow(() -> new RuntimeException("W2 not found"));
    }

    public List<W2> getW2sByEmployee(Long employeeId) {
        return w2Repository.findByEmployeeId(employeeId);
    }

    public List<W2> getW2sByCompany(Long companyId) {
        return w2Repository.findByCompanyId(companyId);
    }

    public W2 getW2ByEmployeeAndYear(Long employeeId, int year) {
        return w2Repository.findByEmployeeIdAndYear(employeeId, year)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("W2 not found"));
    }

    public void deleteW2(Long id) {
        w2Repository.deleteById(id);
    }

    /**
     * Generic method to validate required fields on any object
     * @param object The object to validate
     * @param objectName Name of the object for error messages
     * @param fieldNames Array of field names that are required
     */
    public <T> void validateRequiredFields(T object, String objectName, String... fieldNames) {
        if (object == null) {
            throw new IllegalArgumentException(objectName + " information is missing");
        }
        
        List<String> missingFields = new ArrayList<>();
        
        for (String fieldName : fieldNames) {
            try {
                Field field = findField(object.getClass(), fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    Object value = field.get(object);
                    
                    if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                        missingFields.add(formatFieldName(fieldName));
                    }
                }
            } catch (Exception e) {
                missingFields.add(formatFieldName(fieldName));
            }
        }
        
        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException(objectName + " information incomplete. Missing: " + 
                String.join(", ", missingFields));
        }
    }

    /**
     * Find a field in a class or its superclasses
     */
    public Field findField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // Field not found in this class, check superclass
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Format field name for error messages (e.g., "firstName" becomes "First Name")
     */
    public String formatFieldName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        result.append(Character.toUpperCase(fieldName.charAt(0)));
        
        for (int i = 1; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append(' ');
            }
            result.append(c);
        }
        
        return result.toString();
    }
} 