package com._project._project.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class CompanyUserController {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @GetMapping(path = "/{user_id}")
    public List<User> getCompanyUsers(@PathVariable long user_id) {
        return userService.getCompanyUsers(user_id);
    }

    @GetMapping(path = "/{currentUser_id}/{userToView_id}")
    public User getUser(@PathVariable long currentUser_id, @PathVariable long userToView_id) {
        return userService.getUser(currentUser_id, userToView_id);
    }

    @PostMapping(path = "/{currentUser_id}/user-add")
    public void createUser(@PathVariable long currentUser_id, @RequestBody User newUser) {
        userService.createUser(currentUser_id, newUser);
    }

    @PutMapping(path = "/{currentUser_id}/user-edit/{userToUpdate_id}")
    public void updateUser(@PathVariable long currentUser_id, @PathVariable long userToUpdate_id, @RequestBody User updatedUser) {
        userService.updateUser(currentUser_id, userToUpdate_id, updatedUser);
    }

    @DeleteMapping(path = "/{currentUser_id}/user-delete/{userToDelete_id}")
    public void deleteUser(@PathVariable long currentUser_id, @PathVariable long userToDelete_id){
        userService.deleteUser(currentUser_id, userToDelete_id);
    }

    @PutMapping("/{currentUser_id}/updateWage/{targetUser_id}/{newWage}")
    public ResponseEntity<?> updateUserWage(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id,
            @PathVariable Double newWage) {
        
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);
        
        if (currentUser == null || targetUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Only OWNER/EXECUTIVE can update wages
        if (currentUser.getRole() != UserRole.OWNER && 
            currentUser.getRole() != UserRole.EXECUTIVE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Only update if target is EMPLOYEE or MANAGER
        if (targetUser.getRole() == UserRole.EMPLOYEE || 
            targetUser.getRole() == UserRole.MANAGER) {
            targetUser.setHourlyRate(newWage);
            userRepository.save(targetUser);
            return ResponseEntity.ok(targetUser);
        }
        
        return ResponseEntity.badRequest().body("Can only set wage for EMPLOYEE or MANAGER");
    }

    @PutMapping("/{currentUser_id}/update-info/{targetUser_id}")
    public ResponseEntity<?> updateUserInfo(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id,
            @RequestBody Map<String, String> info) {
        
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);

        if (currentUser == null || targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if user has permission (self, OWNER, EXECUTIVE, or MANAGER)
        if (currentUser_id != targetUser_id && 
            currentUser.getRole() != UserRole.OWNER && 
            currentUser.getRole() != UserRole.EXECUTIVE &&
            currentUser.getRole() != UserRole.MANAGER) {
            return ResponseEntity.badRequest()
                .body("No permission to update other user's information");
        }

        if (info.containsKey("firstName")) targetUser.setFirstName(info.get("firstName"));
        if (info.containsKey("lastName")) targetUser.setLastName(info.get("lastName"));
        if (info.containsKey("email")) targetUser.setEmail(info.get("email"));
        if (info.containsKey("password")) targetUser.setPassword(info.get("password"));
        if (info.containsKey("role")) targetUser.setRole(UserRole.valueOf(info.get("role")));
        if (info.containsKey("streetAddress")) targetUser.setStreetAddress(info.get("streetAddress"));
        if (info.containsKey("city")) targetUser.setCity(info.get("city"));
        if (info.containsKey("state")) targetUser.setState(info.get("state"));
        if (info.containsKey("zipCode")) targetUser.setZipCode(info.get("zipCode"));
        if (info.containsKey("country")) targetUser.setCountry(info.get("country"));
        if (info.containsKey("ssn")) targetUser.setSsn(info.get("ssn"));
        
        // Handle hourly rate update - convert from String to Double
        if (info.containsKey("hourlyRate") && currentUser.getRole() != UserRole.EMPLOYEE && currentUser.getRole() != UserRole.MANAGER) {
            try {
                Double rate = Double.parseDouble(info.get("hourlyRate"));
                targetUser.setHourlyRate(rate);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("Invalid hourly rate format");
            }
        }

        userRepository.save(targetUser);
        return ResponseEntity.ok(targetUser);
    }

    @GetMapping("/{currentUser_id}/info/{targetUser_id}")
    public ResponseEntity<?> getUserInfo(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id) {
        
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);

        if (currentUser == null || targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if users are in same company
        if (currentUser.getCompany().getId() != targetUser.getCompany().getId()) {
            return ResponseEntity.badRequest()
                .body("Cannot view information from other companies");
        }

        Map<String, String> userInfo = Map.of(
            "streetAddress", targetUser.getStreetAddress() != null ? targetUser.getStreetAddress() : "",
            "city", targetUser.getCity() != null ? targetUser.getCity() : "",
            "state", targetUser.getState() != null ? targetUser.getState() : "",
            "zipCode", targetUser.getZipCode() != null ? targetUser.getZipCode() : "",
            "country", targetUser.getCountry() != null ? targetUser.getCountry() : "",
            "ssn", targetUser.getSsn() != null ? targetUser.getSsn() : "",
            "hourlyRate", targetUser.getHourlyRate() != null ? targetUser.getHourlyRate().toString() : ""
        );

        return ResponseEntity.ok(userInfo);
    }

}
