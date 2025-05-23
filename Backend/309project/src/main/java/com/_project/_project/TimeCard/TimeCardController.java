package com._project._project.TimeCard;


import com._project._project.User.User;
import com._project._project.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@RestController
public class TimeCardController {

    @Autowired
    TimeCardRepository timeCardRepository;

    @Autowired
    UserRepository userRepository;

    private final String success = "success";
    private final String failure = "failure";

    @GetMapping(path = "/users/timecards")
    public List<TimeCard> getAllTimeCards() {
        return timeCardRepository.findAll();
    }

    @GetMapping(path = "/users/timecards/{userNumber}")
    public List<TimeCard> getTimeCardById(@PathVariable long userNumber) {
        return timeCardRepository.findAllByUserNumber(userNumber);
    }

    @PostMapping("/users/timecards/clockIn/{userNumber}")
    public String clockIn(@PathVariable long userNumber) {
        User user = userRepository.findById(userNumber);
        if (user == null) return failure;

        // Check if user already has an active timecard
        TimeCard activeTimeCard = timeCardRepository.findByUserAndClockOutIsNull(user);
        if (activeTimeCard != null) return failure;

        TimeCard timeCard = new TimeCard(user, LocalDateTime.now());
        timeCard.setUserNumber(userNumber);
        
        // Set week number and year
        LocalDateTime currDate = LocalDateTime.now();
        timeCard.setWeekNumber(currDate.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()));
        timeCard.setYear(currDate.getYear());

        timeCardRepository.save(timeCard);
        return success;
    }

    @PutMapping("/users/timecards/clockOut/{userNumber}")
    public String clockOut(@PathVariable long userNumber) {
        // Find the active timecard for this user (one without a clock out time)
        TimeCard timeCard = timeCardRepository.findByUserNumberAndClockOutIsNull(userNumber);
        if (timeCard == null) return failure;

        timeCard.setClockOut(LocalDateTime.now());
        
        // Calculate hours worked
        Duration duration = Duration.between(timeCard.getClockIn(), timeCard.getClockOut());
        timeCard.setHoursWorked(duration.toMinutes() / 60.0);

        timeCardRepository.save(timeCard);
        return success;
    }

    @GetMapping("/user/timecards/{userNumber}")
    public ResponseEntity<?> getUserTimeCards(@PathVariable Long userNumber) {
        List<TimeCard> timeCards = timeCardRepository.findByUserNumber(userNumber);
        if (timeCards.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No time cards found for user number: " + userNumber);
        }
        return ResponseEntity.ok(timeCards);
    }

    @GetMapping("/users/timecards/latest/{userNumber}")
    public ResponseEntity<?> getLatestTimeCard(@PathVariable long userNumber) {
        // First try to find an active (unclosed) timecard
        TimeCard activeCard = timeCardRepository.findByUserNumberAndClockOutIsNull(userNumber);
        if (activeCard != null) {
            return ResponseEntity.ok(activeCard);
        }

        // If no active timecard, get the most recent closed timecard
        TimeCard latestCard = timeCardRepository.findFirstByUserNumberOrderByClockInDesc(userNumber);
        if (latestCard != null) {
            return ResponseEntity.ok(latestCard);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("No timecards found for user number: " + userNumber);
    }

    @PostMapping("/users/timecards/setDay/{userNumber}")
    public ResponseEntity<?> setDayTimeCard(
            @PathVariable long userNumber,
            @RequestBody DayTimeCardRequest request) {
        
        User user = userRepository.findById(userNumber);
        if (user == null) return ResponseEntity.notFound().build();

        TimeCard timeCard = new TimeCard(user, request.getClockIn());
        timeCard.setClockOut(request.getClockOut());
        timeCard.setUserNumber(userNumber);
        
        // Set week number and year
        timeCard.setWeekNumber(request.getClockIn().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()));
        timeCard.setYear(request.getClockIn().getYear());

        // Calculate hours worked
        Duration duration = Duration.between(request.getClockIn(), request.getClockOut());
        timeCard.setHoursWorked(duration.toMinutes() / 60.0);

        timeCardRepository.save(timeCard);
        return ResponseEntity.ok(timeCard);
    }
}
