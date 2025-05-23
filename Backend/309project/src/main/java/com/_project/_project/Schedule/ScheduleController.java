package com._project._project.Schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com._project._project.User.User;
import com._project._project.User.UserRepository;
import com._project._project.User.UserRole;
import com._project._project.Perminissions.PermissionsService;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PermissionsService permissionsService;
    // Get user's day's schedule
    @GetMapping("/{currentUser_id}/user/{user_id}/{date}")
    public ResponseEntity<?> getDaySchedule(
            @PathVariable long currentUser_id,
            @PathVariable long user_id,
            @PathVariable @DateTimeFormat(pattern = "MM-dd-yyyy") LocalDate date) {
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(user_id);
        if (permissionsService.NoUserExists(currentUser, targetUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (permissionsService.NoDoublePermissions(currentUser, targetUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Employees are not authorized to view other users' schedules");
        }
        return ResponseEntity.ok(scheduleService.getScheduleByDate(date, targetUser));
    }

    // Get user's week's schedule
    @GetMapping("/{currentUser_id}/user/{targetUser_id}/week/{date}")
    public ResponseEntity<?> getWeekSchedule(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id,
            @PathVariable @DateTimeFormat(pattern = "MM-dd-yyyy") LocalDate date) {
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);
        if (permissionsService.NoUserExists(currentUser, targetUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (permissionsService.NoDoublePermissions(currentUser, targetUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Employees are not authorized to view other users' schedules");
        }
        return ResponseEntity.ok(scheduleService.getScheduleByWeek(date, targetUser));
    }

    // Get all company schedules for a day
    @GetMapping("/{currentUser_id}/all/{date}")
    public ResponseEntity<?> getAllDaySchedule(
            @PathVariable long currentUser_id,
            @PathVariable @DateTimeFormat(pattern = "MM-dd-yyyy") LocalDate date) {
        User currentUser = userRepository.findById(currentUser_id);
        if (permissionsService.NoUserExists(currentUser, currentUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (permissionsService.IsEmployee(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Employees are not authorized to view all company schedules");
        }
        return ResponseEntity.ok(scheduleService.getAllDaySchedule(currentUser.getCompany().getId(), date));
    }

    // Get all company schedules for a week
    @GetMapping("/{currentUser_id}/all/week/{date}")
    public ResponseEntity<?> getAllWeekSchedule(
            @PathVariable long currentUser_id,
            @PathVariable @DateTimeFormat(pattern = "MM-dd-yyyy") LocalDate date) {
        User currentUser = userRepository.findById(currentUser_id);
        if (permissionsService.NoUserExists(currentUser, currentUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (permissionsService.IsEmployee(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Employees are not authorized to view all company schedules");
        }
        return ResponseEntity.ok(scheduleService.getAllWeekSchedule(currentUser.getCompany().getId(), date));
    }



    
    // Create single schedule
    @PostMapping("/{currentUser_id}/create/{targetUser_id}")
    public ResponseEntity<?> createSchedule(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id,
            @RequestBody Schedule schedule) {
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);
        if (permissionsService.NoUserExists(currentUser, targetUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(scheduleService.createSchedule(targetUser_id, schedule));
    }

    // Create week schedule
    @PostMapping("/{userId}/createWeek")
    public ResponseEntity<?> createWeekSchedule(
        @PathVariable long userId,
        @RequestBody List<Schedule> schedules) {
        try {
            List<Schedule> createdSchedules = scheduleService.createWeekSchedule(userId, schedules);
            return ResponseEntity.ok(createdSchedules);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Create week schedule from availability
    @PostMapping("/{currentUser_id}/createWeekAvailability/{targetUser_id}")
    public ResponseEntity<?> createWeekFromAvailability(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id) {
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);
        if (permissionsService.NoUserExists(currentUser, targetUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        try {
            List<Schedule> createdSchedules = scheduleService.createWeekFromAvailability(targetUser_id);
            return ResponseEntity.ok(createdSchedules);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Create week schedule from date
    @PostMapping("/{currentUser_id}/createWeekAvailability/{targetUser_id}/date/{date}")
    public ResponseEntity<?> createWeekFromDate(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id,
            @PathVariable @DateTimeFormat(pattern = "MM-dd-yyyy") LocalDate date) {
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);
        if (permissionsService.NoUserExists(currentUser, targetUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        try {
            List<Schedule> createdSchedules = scheduleService.createWeekFromDate(targetUser_id, date);
            return ResponseEntity.ok(createdSchedules);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Update single schedule
    @PutMapping("/{currentUser_id}/update/{schedule_id}")
    public ResponseEntity<?> updateSchedule(
            @PathVariable long currentUser_id,
            @PathVariable long schedule_id,
            @RequestBody Schedule schedule) {
        User currentUser = userRepository.findById(currentUser_id);
        if (permissionsService.NoUserExists(currentUser, currentUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        schedule.setId(schedule_id);  // Ensure ID matches path
        return ResponseEntity.ok(scheduleService.updateSchedule(schedule));
    }

    // Create schedules for multiple users
    @PostMapping("/{currentUser_id}/createBatch")
    public ResponseEntity<?> createBatchSchedule(
            @PathVariable long currentUser_id,
            @RequestBody List<Schedule> schedules) {
        User currentUser = userRepository.findById(currentUser_id);
        if (permissionsService.NoUserExists(currentUser, currentUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        
        // Check if current user has permission
        if (permissionsService.IsEmployee(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Employees cannot create schedules for others");
        }

        return ResponseEntity.ok(scheduleService.createBatchSchedule(schedules));
    }

    // Update schedules for multiple users
    @PutMapping("/{currentUser_id}/updateBatch")
    public ResponseEntity<?> updateBatchSchedule(
            @PathVariable long currentUser_id,
            @RequestBody List<Schedule> schedules) {
        User currentUser = userRepository.findById(currentUser_id);
        if (permissionsService.NoUserExists(currentUser, currentUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        
        if (permissionsService.IsEmployee(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Employees cannot update schedules for others");
        }
        return ResponseEntity.ok(scheduleService.updateBatchSchedule(schedules));
    }


}
