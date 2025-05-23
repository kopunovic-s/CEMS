package com._project._project.Schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com._project._project.User.User;
import com._project._project.User.UserRepository;
import com._project._project.Availability.Availability;
import com._project._project.Availability.AvailabilityRepository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AvailabilityRepository availabilityRepository;

    public List<Schedule> getUserSchedule(long userId) {
        return scheduleRepository.findByUserId(userId);
    }

    public List<Schedule> getScheduleByDateRange(LocalDateTime start, LocalDateTime end) {
        return scheduleRepository.findByStartTimeBetween(start, end);
    }

    public List<Schedule> getScheduleByDate(LocalDate date, User user) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return scheduleRepository.findByUserIdAndStartTimeBetween(user.getId(), startOfDay, endOfDay);
    }

    public List<Schedule> getScheduleByWeek(LocalDate date, User user) {
        LocalDateTime startOfWeek = date.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);
        return scheduleRepository.findByUserIdAndStartTimeBetween(user.getId(), startOfWeek, endOfWeek);
    }

    @Transactional
    public Schedule createSchedule(long userId, Schedule schedule) {
        User user = userRepository.findById(userId);
        
        com._project._project.Availability.DayOfWeek day = 
            com._project._project.Availability.DayOfWeek.valueOf(
                schedule.getStartTime().getDayOfWeek().name());
                
        List<Availability> availability = availabilityRepository.findByUserAndDayOfWeek(user, day);
        
        if (availability.isEmpty() || !availability.get(0).getIsAvailable()) {
            throw new RuntimeException("Schedule conflicts with availability");
        }

        schedule.setUser(user);
        schedule.setUserId(userId);
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public List<Schedule> createWeekSchedule(long userId, List<Schedule> schedules) {
        return schedules.stream()
            .map(schedule -> createSchedule(userId, schedule))
            .collect(Collectors.toList());
    }

    @Transactional
    public List<Schedule> createWeekFromAvailability(long userId) {
        User user = userRepository.findById(userId);
        
        LocalDate saturday = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        List<Schedule> generatedSchedules = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = saturday.plusDays(i);
            
            // Check if schedule already exists for this date
            List<Schedule> existingSchedules = getScheduleByDate(currentDate, user);
            if (!existingSchedules.isEmpty()) {
                continue; // Skip this day if schedule exists
            }

            DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();
            List<Availability> availabilities = availabilityRepository.findByUserAndDayOfWeek(
                user,
                com._project._project.Availability.DayOfWeek.valueOf(currentDayOfWeek.name())
            );
            
            if (!availabilities.isEmpty() && availabilities.get(0).getIsAvailable()) {
                Availability avail = availabilities.get(0);
                Schedule schedule = new Schedule();
                schedule.setUserId(userId);
                schedule.setUser(user);
                
                LocalDateTime startDateTime = LocalDateTime.of(currentDate, avail.getStartTime());
                LocalDateTime endDateTime = LocalDateTime.of(currentDate, avail.getEndTime());
                
                schedule.setStartTime(startDateTime);
                schedule.setEndTime(endDateTime);
                
                generatedSchedules.add(schedule);
            }
        }
        
        if (generatedSchedules.isEmpty()) {
            throw new RuntimeException("No available days found for scheduling");
        }
        
        return scheduleRepository.saveAll(generatedSchedules);
    }

    @Transactional
    public List<Schedule> updateSchedules(List<Schedule> schedules) {
        schedules.forEach(schedule -> {
            if (schedule.getUserId() != null) {
                User user = userRepository.findById(schedule.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                schedule.setUser(user);
            }
        });
        return scheduleRepository.saveAll(schedules);
    }

    public List<Schedule> getAllDaySchedule(long companyId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return scheduleRepository.findByStartTimeBetween(startOfDay, endOfDay);
    }

    public List<Schedule> getAllWeekSchedule(long companyId, LocalDate date) {
        LocalDateTime startOfWeek = date.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);
        return scheduleRepository.findByStartTimeBetween(startOfWeek, endOfWeek);
    }

    @Transactional
    public List<Schedule> createBatchSchedule(List<Schedule> schedules) {
        return schedules.stream()
            .map(schedule -> {
                User user = userRepository.findById(schedule.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + schedule.getUserId()));
                schedule.setUser(user);
                return scheduleRepository.save(schedule);
            })
            .toList();
    }

    @Transactional
    public List<Schedule> updateBatchSchedule(List<Schedule> schedules) {
        return schedules.stream()
            .map(schedule -> {
                if (schedule.getId() == 0) {
                    throw new RuntimeException("Schedule ID is required for updates");
                }
                
                Schedule existingSchedule = scheduleRepository.findById(schedule.getId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
                
                existingSchedule.setStartTime(schedule.getStartTime());
                existingSchedule.setEndTime(schedule.getEndTime());
                
                return scheduleRepository.save(existingSchedule);
            })
            .toList();
    }

    @Transactional
    public Schedule updateSchedule(Schedule schedule) {
        Schedule existingSchedule = scheduleRepository.findById(schedule.getId())
            .orElseThrow(() -> new RuntimeException("Schedule not found"));
        
        existingSchedule.setStartTime(schedule.getStartTime());
        existingSchedule.setEndTime(schedule.getEndTime());
        
        return scheduleRepository.save(existingSchedule);
    }

    @Transactional
    public List<Schedule> createWeekFromDate(long userId, LocalDate startDate) {
        User user = userRepository.findById(userId);

        LocalDate saturday = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
        List<Schedule> generatedSchedules = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = saturday.plusDays(i);
            
            // Check if schedule already exists for this date
            List<Schedule> existingSchedules = getScheduleByDate(currentDate, user);
            if (!existingSchedules.isEmpty()) {
                continue; // Skip this day if schedule exists
            }

            DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();
            List<Availability> availabilities = availabilityRepository.findByUserAndDayOfWeek(
                user,
                com._project._project.Availability.DayOfWeek.valueOf(currentDayOfWeek.name())
            );
            
            if (!availabilities.isEmpty() && availabilities.get(0).getIsAvailable()) {
                Availability avail = availabilities.get(0);
                Schedule schedule = new Schedule();
                schedule.setUserId(userId);
                schedule.setUser(user);
                
                LocalDateTime startDateTime = LocalDateTime.of(currentDate, avail.getStartTime());
                LocalDateTime endDateTime = LocalDateTime.of(currentDate, avail.getEndTime());
                
                schedule.setStartTime(startDateTime);
                schedule.setEndTime(endDateTime);
                
                generatedSchedules.add(schedule);
            }
        }
        
        if (generatedSchedules.isEmpty()) {
            throw new RuntimeException("No available days found for scheduling");
        }
        
        return scheduleRepository.saveAll(generatedSchedules);
    }
} 