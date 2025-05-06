package com.web.website.controllers;

import com.web.website.models.Users;
import com.web.website.models.OtpRequest;
import com.web.website.repo.UserRepo;
import com.web.website.services.EmailService;
import com.web.website.services.JwtService;
import com.web.website.services.UserService;
import dto.User_dto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/web")
public class UserController {

    @Autowired private UserService service;
    @Autowired private JwtService jwtService;
    @Autowired private EmailService emailService;
    @Autowired private UserRepo usersRepository;

    private Map<String, String> otpStorage = new HashMap<>();
    private Map<String, LocalDateTime> otpExpiry = new HashMap<>();

    @PostMapping("/register")
    public Users userRegister(@RequestBody Users user) {
        return service.registerUser(user);
    }

    @PostMapping("/login")
    public String loginUser(@RequestBody Users user) {
        return service.verify(user);
    }

    @GetMapping("/username")
    public Map<String, Object> getUsernameFromToken(HttpServletRequest request) {
        return service.getUserInfoFromToken(request);
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailExists(@RequestParam String email) {
        boolean exists = usersRepository.existsByEmail(email);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> payload) {
        return service.sendOtp(payload);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest otpRequest) {
        return service.verifyOtp(otpRequest);
    }

    @PostMapping("/get-otp")
    public ResponseEntity<String> sendOtpForPassword(@RequestBody Map<String, String> payload) {
        return service.sendOtpForPassword(payload);
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody User_dto userDto) {
        return service.resetPassword(userDto);

    }

}
