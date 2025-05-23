package com._project._project.Availability;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com._project._project.User.User;
import com._project._project.User.UserRepository;
import com._project._project.User.UserRole;

import java.util.List;

@RestController
@RequestMapping("/availability")
public class AvailabilityController {
    @Autowired
    private AvailabilityService availabilityService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{currentUser_id}/user/{user_id}")
    public ResponseEntity<?> getUserAvailability(@PathVariable long currentUser_id, @PathVariable long user_id) {
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(user_id);
        if (currentUser == null || targetUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User ID " + currentUser_id + " or " + user_id + " not found");
        }   
        if (currentUser.getRole() == UserRole.EMPLOYEE && currentUser_id != targetUser.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Employee cannot view other employee's availability");
        }
        
        return ResponseEntity.ok(availabilityService.getUserAvailability(user_id));
    }

    @PostMapping("/{currentUser_id}/update/{user_id}")
    public ResponseEntity<?> updateAvailability(
            @PathVariable long currentUser_id,
            @PathVariable long user_id,
            @RequestBody List<Availability> availabilities) {
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(user_id);
        if (currentUser == null || targetUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User ID " + currentUser_id + " or " + user_id + " not found");
        }
        if (currentUser.getRole() == UserRole.EMPLOYEE && currentUser_id != targetUser.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Employee cannot update other employee's availability");
        }

        return ResponseEntity.ok(availabilityService.updateAvailability(user_id, availabilities));
    }

    @GetMapping("/{currentUser_id}/all")
    public ResponseEntity<?> getAllAvailabilities(@PathVariable long currentUser_id) {
        User currentUser = userRepository.findById(currentUser_id);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User ID " + currentUser_id + " not found");
        }
        if (currentUser.getRole() == UserRole.EMPLOYEE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only employees cannot view all availabilities");
        }
        
        return ResponseEntity.ok(availabilityService.getAllAvailabilities(currentUser_id));
    }
}
