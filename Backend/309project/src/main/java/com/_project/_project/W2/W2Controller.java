package com._project._project.W2;

import com._project._project.User.User;
import com._project._project.User.UserRepository;
import com._project._project.Company.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com._project._project.User.UserRole;
import java.io.IOException;

//t2asdasdadgt
@RestController
@RequestMapping("/w2")
public class W2Controller {

    @Autowired
    private W2Service w2Service;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{currentUser_id}/{targetUser_id}/{year}")
    public ResponseEntity<?> getW2(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id,
            @PathVariable int year) {
        
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);

        if (currentUser == null || targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        // Check permissions
        if (currentUser_id != targetUser_id && 
            currentUser.getRole() != UserRole.OWNER && 
            currentUser.getRole() != UserRole.EXECUTIVE) {
            return ResponseEntity.badRequest()
                .body("No permission to access other user's W2");
        }

        try {
            Company company = targetUser.getCompany();
            W2 w2 = w2Service.generateAndSaveW2(targetUser, company, year, false);
            return ResponseEntity.ok(w2);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating W2: " + e.getMessage());
        }
    }

    @GetMapping("/{currentUser_id}/{targetUser_id}/{year}/pdf")
    public ResponseEntity<?> getW2Pdf(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id,
            @PathVariable int year) {
        
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);

        if (currentUser == null || targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        // Check permissions
        if (currentUser_id != targetUser_id && 
            currentUser.getRole() != UserRole.OWNER && 
            currentUser.getRole() != UserRole.EXECUTIVE) {
            return ResponseEntity.badRequest()
                .body("No permission to access other user's W2");
        }

        try {
            W2 w2;
            Company company = targetUser.getCompany();
            
            try {
                // Try to get existing W2
                w2 = w2Service.getW2ByEmployeeAndYear(targetUser.getId(), year);
                // Update the existing W2
                w2 = w2Service.generateAndSaveW2(targetUser, company, year, true);
            } catch (RuntimeException e) {
                // If W2 not found, generate a new one
                w2 = w2Service.generateAndSaveW2(targetUser, company, year, false);
            }
            
            byte[] pdfData = w2Service.generatePdf(w2);
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", 
                    "attachment; filename=\"W2-" + year + "-" + targetUser.getLastName() + ".pdf\"")
                .body(pdfData);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body("Error generating PDF: " + e.getMessage());
        }
    }

    @PostMapping("/generate/{currentUser_id}/{targetUser_id}/{year}")
    public ResponseEntity<?> generateAndSaveW2(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id,
            @PathVariable int year) {
        
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);

        if (currentUser == null || targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        // Check permissions
        if (currentUser_id != targetUser_id && 
            currentUser.getRole() != UserRole.OWNER && 
            currentUser.getRole() != UserRole.EXECUTIVE) {
            return ResponseEntity.badRequest()
                .body("No permission to access other user's W2");
        }

        try {
            Company company = targetUser.getCompany();
            W2 w2 = w2Service.generateAndSaveW2(targetUser, company, year, false);
            return ResponseEntity.ok(w2);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating W2: " + e.getMessage());
        }
    }

    @PutMapping("/{currentUser_id}/{targetUser_id}/{year}")
    public ResponseEntity<?> editW2(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id,
            @PathVariable int year) {
        
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);
        
        if (currentUser == null || targetUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Check permissions
        if (currentUser.getRole() != UserRole.OWNER && 
            currentUser.getRole() != UserRole.EXECUTIVE) {
            return ResponseEntity.badRequest()
                .body("No permission to access other user's W2");
        }
        
        try {
            Company company = targetUser.getCompany();
            W2 w2 = w2Service.generateAndSaveW2(targetUser, company, year, true);
            return ResponseEntity.ok(w2);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating W2: " + e.getMessage());
        }
    }

    @DeleteMapping("/{currentUser_id}/{w2_id}")
    public ResponseEntity<?> deleteW2(
            @PathVariable long currentUser_id,
            @PathVariable Long w2_id) {
        
        User currentUser = userRepository.findById(currentUser_id);
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }

        if (currentUser.getRole() != UserRole.OWNER && 
            currentUser.getRole() != UserRole.EXECUTIVE) {
            return ResponseEntity.badRequest()
                .body("No permission to delete W2s");
        }

        try {
            w2Service.deleteW2(w2_id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
} 