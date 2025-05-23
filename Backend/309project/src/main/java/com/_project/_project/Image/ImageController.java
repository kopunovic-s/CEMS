package com._project._project.Image;

import com._project._project.User.User;
import com._project._project.User.UserRepository;
import com._project._project.Company.Company;
import com._project._project.Company.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com._project._project.Perminissions.PermissionsService;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PermissionsService permissionsService;

    @PutMapping("/user/{currentUser_id}/{targetUser_id}")
    public ResponseEntity<?> uploadUserImage(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id,
            @RequestParam("image") MultipartFile file) {
        
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);

        if (permissionsService.NoUserExists(currentUser, targetUser)) {
            return ResponseEntity.notFound().build();
        }

        // Check if user has permission to update the image
        if (permissionsService.NoDoublePermissions(currentUser, targetUser)) {
            return ResponseEntity.badRequest()
                .body("No permission to update other user's image");
        }

        try {
            String base64Image = imageService.processImage(file);
            targetUser.setProfileImage(base64Image);
            userRepository.save(targetUser);
            return ResponseEntity.ok().body("Image uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/company/{currentUser_id}/{companyId}")
    public ResponseEntity<?> uploadCompanyLogo(
            @PathVariable long currentUser_id,
            @PathVariable long companyId,
            @RequestParam("image") MultipartFile file) {
        
        User currentUser = userRepository.findById(currentUser_id);
        Company company = companyRepository.findById(companyId);

        if (permissionsService.NoCompanyExists(currentUser, company)) {
            return ResponseEntity.notFound().build();
        }

        // Check if user has permission to update company logo
        if (permissionsService.NoSinglePermissions(currentUser)) {
            return ResponseEntity.badRequest()
                .body("No permission to update company logo");
        }

        try {
            String base64Image = imageService.processImage(file);
            company.setCompanyLogo(base64Image);
            companyRepository.save(company);
            return ResponseEntity.ok().body("Logo uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{currentUser_id}/{targetUser_id}")
    public ResponseEntity<?> getUserImage(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id) {
        
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);

        if (permissionsService.NoUserExists(currentUser, targetUser)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(targetUser.getProfileImage());
    }

    @GetMapping("/company/{currentUser_id}/{companyId}")
    public ResponseEntity<?> getCompanyLogo(
            @PathVariable long currentUser_id,
            @PathVariable long companyId) {
        
        User currentUser = userRepository.findById(currentUser_id);
        Company company = companyRepository.findById(companyId);

        if (permissionsService.NoCompanyExists(currentUser, company)) {
            return ResponseEntity.notFound().build();
        }

        // Anyone can view their own company logo
        if (permissionsService.NoCompanyEditPermissions(currentUser, company)) {
            return ResponseEntity.badRequest()
                .body("Cannot view logos from other companies");
        }

        return ResponseEntity.ok().body(company.getCompanyLogo());
    }

    @DeleteMapping("/user/{currentUser_id}/{targetUser_id}")
    public ResponseEntity<?> deleteUserImage(
            @PathVariable long currentUser_id,
            @PathVariable long targetUser_id) {
        
        User currentUser = userRepository.findById(currentUser_id);
        User targetUser = userRepository.findById(targetUser_id);

        if (permissionsService.NoUserExists(currentUser, targetUser)) {
            return ResponseEntity.notFound().build();
        }

        // Check delete permissions
        if (permissionsService.NoDoublePermissions(currentUser, targetUser)) {
            return ResponseEntity.badRequest()
                .body("No permission to delete other user's image");
        }

        targetUser.setProfileImage(null);
        userRepository.save(targetUser);
        return ResponseEntity.ok().body("Image deleted successfully");
    }

    @DeleteMapping("/company/{currentUser_id}/{companyId}")
    public ResponseEntity<?> deleteCompanyLogo(
            @PathVariable long currentUser_id,
            @PathVariable long companyId) {
        
        User currentUser = userRepository.findById(currentUser_id);
        Company company = companyRepository.findById(companyId);

        if (permissionsService.NoCompanyExists(currentUser, company)) {
            return ResponseEntity.notFound().build();
        }

        // Check delete permissions
        if (permissionsService.NoSinglePermissions(currentUser)) {
            return ResponseEntity.badRequest()
                .body("No permission to delete company logo");
        }

        company.setCompanyLogo(null);
        companyRepository.save(company);
        return ResponseEntity.ok().body("Logo deleted successfully");
    }
} 