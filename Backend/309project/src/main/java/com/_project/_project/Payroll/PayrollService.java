package com._project._project.Payroll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com._project._project.User.User;
import com._project._project.Payroll.Responses.PayrollWeekResponse;
import com._project._project.TimeCard.TimeCard;
import com._project._project.TimeCard.TimeCardRepository;
import com._project._project.User.UserService;
import com._project._project.User.UserRole;
import com._project._project.User.UserRepository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

@Service
public class PayrollService {
    @Autowired
    private PayrollRepository payrollRepository;
    @Autowired
    private TimeCardRepository timeCardRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    
    public Payroll calculateWeekSummary(User employee, int weekNumber, int year) {
        List<TimeCard> timeCards = timeCardRepository
            .findByUserAndWeekNumberAndYear(employee, weekNumber, year);
        
        double totalHours = timeCards.stream()
            .mapToDouble(TimeCard::getHoursWorked)
            .sum();
            
        Payroll payroll = payrollRepository.findByUserAndWeekNumberAndYear(
            employee, weekNumber, year);
            
        if (payroll == null) {
            payroll = new Payroll(employee, weekNumber, year);
        }
        
        payroll.setTotalHours(totalHours);
        payroll.setTotalPay(employee.getHourlyRate() != null ? 
            totalHours * employee.getHourlyRate() : 0.0);
        
        return payrollRepository.save(payroll);
    }

    public Payroll createOrUpdatePayroll(User employee, int weekNumber, int year, boolean markAsPaid) {
        Payroll payroll = payrollRepository.findByUserAndWeekNumberAndYear(
            employee, weekNumber, year);
            
        if (payroll == null) {
            payroll = new Payroll(employee, weekNumber, year);
        }
        
        if (!payroll.getIsPaid()) {
            List<TimeCard> timeCards = timeCardRepository
                .findByUserAndWeekNumberAndYear(employee, weekNumber, year);
            
            double totalHours = timeCards.stream()
                .mapToDouble(TimeCard::getHoursWorked)
                .sum();
                
            payroll.getTimeCards().clear();
            for (TimeCard timeCard : timeCards) {
                payroll.getTimeCards().add(timeCard);
                timeCard.setPayroll(payroll);
            }
            
            payroll.setTotalHours(totalHours);
            payroll.setTotalPay(totalHours * payroll.getHourlyRate());
            
            if (markAsPaid) {
                payroll.setIsPaid(true);
                payroll.setProcessedDate(LocalDateTime.now());
            }
            
            return payrollRepository.save(payroll);
        }
        
        return payroll;
    }

    public PayrollWeekResponse getWeekPayroll(long currentUser_id, String date) {
        LocalDate localDate = LocalDate.parse(date);
        int weekNumber = localDate.get(WeekFields.SUNDAY_START.weekOfWeekBasedYear());
        int year = localDate.getYear();
        
        User currentUser = userRepository.findById(currentUser_id);
        List<User> employees;
        
        // If employee, only show their own record
        if (currentUser.getRole() == UserRole.EMPLOYEE) {
            employees = List.of(currentUser);
        } else {
            // For OWNER/EXECUTIVE/MANAGER show all employees
            employees = userService.getCompanyUsers(currentUser_id)
                .stream()
                .filter(user -> user.getRole() == UserRole.EMPLOYEE || 
                              user.getRole() == UserRole.MANAGER)
                .toList();
        }

        List<Payroll> payrolls = employees.stream()
            .map(employee -> calculateWeekSummary(employee, weekNumber, year))
            .toList();

        return new PayrollWeekResponse(
            weekNumber,
            year,
            LocalDateTime.now(),
            false,
            payrolls
        );
    }

    public List<Payroll> updatePayrollStatus(long currentUser_id, String date, boolean isPaid) {
        LocalDate localDate = LocalDate.parse(date);
        int weekNumber = localDate.get(WeekFields.SUNDAY_START.weekOfWeekBasedYear());  // Sunday-Saturday
        int year = localDate.getYear();
        
        List<User> employees = userService.getCompanyUsers(currentUser_id)
            .stream()
            .filter(user -> user.getRole() == UserRole.EMPLOYEE || 
                          user.getRole() == UserRole.MANAGER)
            .toList();

        return employees.stream()
            .filter(employee -> employee.getHourlyRate() != null)
            .map(employee -> createOrUpdatePayroll(employee, weekNumber, year, isPaid))
            .toList();
    }
} 