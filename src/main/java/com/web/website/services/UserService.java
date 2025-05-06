package com.web.website.services;

import com.web.website.models.OtpRequest;
import com.web.website.models.Users;
import com.web.website.repo.UserRepo;
import dto.User_dto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    AuthenticationManager authManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    private Map<String, String> otpStorage = new HashMap<>();
    private Map<String, LocalDateTime> otpExpiry = new HashMap<>();


    @Autowired
    private UserRepo repo;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public Users registerUser(Users user) {
        user.setPassword(encoder.encode(user.getPassword()));
        Users savedUser = repo.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());

        return savedUser;
    }

    public Users userByName(String username) {
        return repo.findByUsername(username);
    }

    public String verify(Users user) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );

        if (authentication.isAuthenticated()) {
            String role = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .findFirst()
                    .orElse(""); // Single role string

            Users userFromDb = repo.findByUsername(user.getUsername());
            String email = userFromDb.getEmail();

            return jwtService.generateToken(user.getUsername(), role, email);
        }

        return "fail";
    }


    public Map<String, Object> getUserInfoFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            String username = jwtService.extractUserName(token);
            String role = jwtService.extractRole(token);
            String email = jwtService.extractEmail(token);

            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("role", role);
            response.put("email", email);

            return response;
        }

        return Map.of("error", "No valid Authorization header found");
    }

    public String resetPassword(User_dto userDto) {
        Optional<Users> optionalUser = repo.findByEmail(userDto.getEmail());
        if (optionalUser.isEmpty()) {
            return "Email not found";
        }

        Users user = optionalUser.get();
        user.setPassword(encoder.encode(userDto.getPassword()));
        repo.save(user);
        return "Password reset successful";
    }

    public ResponseEntity<String> verifyOtp(OtpRequest otpRequest) {
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

    public ResponseEntity<String> sendOtpForPassword(Map<String, String> payload) {
        String email = payload.get("email");
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        otpStorage.put(email, otp);
        otpExpiry.put(email, LocalDateTime.now().plusMinutes(5));

        emailService.sendOtpForPassword(email, otp);
        return ResponseEntity.ok("OTP sent to email");
    }

    public ResponseEntity<String> sendOtp(Map<String, String> payload) {
        String email = payload.get("email");
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        otpStorage.put(email, otp);
        otpExpiry.put(email, LocalDateTime.now().plusMinutes(5));

        emailService.sendOtpEmail(email, otp);
        return ResponseEntity.ok("OTP sent to email");
    }


}
