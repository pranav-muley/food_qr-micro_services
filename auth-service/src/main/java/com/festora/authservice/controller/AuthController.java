package com.festora.authservice.controller;

import com.festora.authservice.enums.VerificationEnum;
import com.festora.authservice.model.User;
import com.festora.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User userModel) {
        String response = null;
        try {
            response = authService.getLoginDetails(userModel);
        } catch (Exception e) {
            response = e.getMessage();
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User userModel) {
        try {
            String response = authService.registerUser(userModel);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

//    @GetMapping("/verifyRegistration")
//    public ResponseEntity<String> verifyToken(@RequestParam String token) {
//        VerificationEnum status = authService.verifyVerificationToken(token);
//        if (status == VerificationEnum.VALID_TOKEN) {
//            return ResponseEntity.ok("verified");
//        }
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(status.name());
//    }
}
