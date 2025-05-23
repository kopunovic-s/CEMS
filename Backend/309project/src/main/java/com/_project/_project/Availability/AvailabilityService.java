package com._project._project.Availability;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com._project._project.User.UserRepository;
import com._project._project.User.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalTime;

@Service
public class AvailabilityService {
    @Autowired
    private AvailabilityRepository availabilityRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Availability> getUserAvailability(long userId) {
        User user = userRepository.findById(userId);
        return availabilityRepository.findByUser(user)
            .stream()
            .filter(Availability::getIsAvailable)
            .toList();
    }

    @Transactional
    public List<Availability> updateAvailability(long userId, List<Availability> updates) {
        User user = userRepository.findById(userId);
        List<Availability> existingAvailability = availabilityRepository.findByUser(user);
        
        if (existingAvailability.isEmpty()) {
            existingAvailability = initializeWeek(user);
        }

        // First, set all days to unavailable
        existingAvailability.forEach(day -> day.setIsAvailable(false));

        // Then update only the days in the request
        for (Availability update : updates) {
            existingAvailability.stream()
                .filter(existing -> existing.getDayOfWeek() == update.getDayOfWeek())
                .findFirst()
                .ifPresent(existing -> {
                    existing.setStartTime(update.getStartTime());
                    existing.setEndTime(update.getEndTime());
                    existing.setIsAvailable(update.getIsAvailable());
                });
        }
        
        return availabilityRepository.saveAll(existingAvailability)
            .stream()
            .filter(Availability::getIsAvailable)
            .toList();
    }

    private List<Availability> initializeWeek(User user) {
        List<Availability> week = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            week.add(new Availability(user, day, LocalTime.of(9, 0), LocalTime.of(17, 0), false));
        }
        return availabilityRepository.saveAll(week);
    }

    public List<Availability> getAllAvailabilities(long currentUserId) {
        User currentUser = userRepository.findById(currentUserId);
        return availabilityRepository.findAll()
            .stream()
            .filter(Availability::getIsAvailable)
            .filter(a -> a.getUser().getCompany().getId() == currentUser.getCompany().getId())
            .toList();
    }
} 