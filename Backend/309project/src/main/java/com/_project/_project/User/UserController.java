package com._project._project.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//=====================================================================
//This controller is for backend testing,
// DO NOT USE THESE ENDPOINTS IN FRONTEND!!!
//=====================================================================
@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    private final String success = "Success";
    private final String failure = "Failure";

    @GetMapping(path = "/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

//    @GetMapping(path = "/users/{id}")
//    User getUserById(@PathVariable int id) {
//        return userRepository.findById(id);
//    }

    @PostMapping(path = "/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (user == null) {
            return ResponseEntity.badRequest().body(failure);
        }

        try {
                        User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(failure);
        }
    }

//    @PutMapping(path = "/Users/{id}")
//    User updateUser(@PathVariable int id, @RequestBody User request) {
//        User user = userRepository.findById(id);
//        if (user == null) {
//            throw new RuntimeException("User ID does not exist");
//        } else if (user.getId() != id) {
//            throw new RuntimeException("User ID does not match");
//        }
//
//        userRepository.save(request);
//        return userRepository.findById(id);
//    }

//    @DeleteMapping(path = "/Users/{id}")
//    String deleteUser(@PathVariable long id) {
//        userRepository.deleteById(id);
//        return success;
//    }

}
