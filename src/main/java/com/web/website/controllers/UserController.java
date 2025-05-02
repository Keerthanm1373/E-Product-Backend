package com.web.website.controllers;

import com.web.website.models.Users;
import com.web.website.models.OtpRequest;
import com.web.website.repo.UserRepo;
import com.web.website.services.EmailService;
import com.web.website.services.JwtService;
import com.web.website.services.UserService;
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
        String email = payload.get("email");
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        otpStorage.put(email, otp);
        otpExpiry.put(email, LocalDateTime.now().plusMinutes(5));

        emailService.sendOtpEmail(email, otp);
        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest otpRequest) {
        String email = otpRequest.getEmail();
        String inputOtp = otpRequest.getOtp();

        String storedOtp = otpStorage.get(email);
        LocalDateTime expiry = otpExpiry.get(email);

        if (storedOtp != null && storedOtp.equals(inputOtp) && expiry != null && expiry.isAfter(LocalDateTime.now())) {
            otpStorage.remove(email);
            otpExpiry.remove(email);
            return ResponseEntity.ok("OTP verified");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP");
        }
    }
}
