package com._project._project.Payroll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com._project._project.User.User;
import com._project._project.Payroll.Responses.DailyTimeCardResponse;
import com._project._project.Payroll.Responses.EmployeeWeekResponse;
import com._project._project.TimeCard.TimeCard;
import com._project._project.TimeCard.TimeCardRepository;
import com._project._project.User.UserService;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import org.springframework.http.HttpStatus;
import com._project._project.User.UserRole;
import com._project._project.User.UserRepository;
import com._project._project.Perminissions.PermissionsService;

@RestController
@RequestMapping("/payroll")
public class PayrollController {
        
    @Autowired
    private TimeCardRepository timeCardRepository;
    
    @Autowired
    private UserService userService;

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionsService permissionsService;

    @GetMapping("/{currentUser_id}/weekSummary/{date}")
    public ResponseEntity<?> getWeekSummary(
            @PathVariable long currentUser_id,
            @PathVariable String date) {
        
        User currentUser = userRepository.findById(currentUser_id);
        if (permissionsService.NoUserExists(currentUser, currentUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User ID " + currentUser_id + " not found");
        }
        
        // Only OWNER/EXECUTIVE/MANAGER can view all payrolls
        if (permissionsService.IsEmployee(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only OWNER, EXECUTIVE, or MANAGER can view all payrolls");
        }
        
        return ResponseEntity.ok(payrollService.getWeekPayroll(currentUser_id, date));
    }

    @GetMapping("/{currentUser_id}/employeeSummary/{employeeId}/{date}")
    public ResponseEntity<?> getEmployeeWeekDetails(
            @PathVariable long currentUser_id,
            @PathVariable long employeeId,
            @PathVariable String date) {
        
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(employeeId);

        if (permissionsService.NoUserExists(currentUser, currentUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User ID " + currentUser_id + " not found");
        }

        // Employees can only view their own records
        if (permissionsService.NoManagerPermissions(currentUser, targetUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Employees can only view their own records");
        }

        LocalDate localDate = LocalDate.parse(date);
        int weekNumber = localDate.get(WeekFields.SUNDAY_START.weekOfWeekBasedYear());
        int year = localDate.getYear();
        
        User employee = userService.getUser(currentUser_id, employeeId);
        List<TimeCard> timeCards = timeCardRepository.findByUserAndWeekNumberAndYear(
            employee, weekNumber, year);

        // Create response with daily breakdown
        List<DailyTimeCardResponse> dailyDetails = timeCards.stream()
            .map(tc -> new DailyTimeCardResponse(
                tc.getClockIn().toLocalDate().toString(),
                tc.getHoursWorked(),
                tc.getClockIn().format(DateTimeFormatter.ofPattern("h:mm a")) + " - " +
                (tc.getClockOut() != null ? tc.getClockOut().format(DateTimeFormatter.ofPattern("h:mm a")) : "")
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(new EmployeeWeekResponse(
            employee.getFirstName() + " " + employee.getLastName(),
            employee.getRole().toString(),
            dailyDetails
        ));
    }

    @PutMapping("/{currentUser_id}/markPaid/{date}")
    public ResponseEntity<?> markWeekAsPaid(
            @PathVariable long currentUser_id,
            @PathVariable String date) {
        
        User currentUser = userRepository.findById(currentUser_id);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User ID " + currentUser_id + " not found");
        }
        
        if (permissionsService.NoSinglePermissions(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only OWNER or EXECUTIVE can mark payroll as paid");
        }
        
        return ResponseEntity.ok(payrollService.updatePayrollStatus(currentUser_id, date, true));
    }

    @PutMapping("/{currentUser_id}/markUnpaid/{date}")
    public ResponseEntity<?> markWeekAsUnpaid(
            @PathVariable long currentUser_id,
            @PathVariable String date) {
        
        User currentUser = userRepository.findById(currentUser_id);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User ID " + currentUser_id + " not found");
        }
        
        if (permissionsService.NoSinglePermissions(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only OWNER or EXECUTIVE can mark payroll as unpaid");
        }
        
        return ResponseEntity.ok(payrollService.updatePayrollStatus(currentUser_id, date, false));
    }
}
