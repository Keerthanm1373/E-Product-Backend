package com.web.website.controllers;

import com.web.website.models.Users;
import com.web.website.services.JwtService;
import com.web.website.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/web")
public class UserController {
    @Autowired
    private UserService service;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public Users userRegister(@RequestBody Users user){

        return service.registerUser(user);
    }
    @PostMapping("/login")
    public String loginUser(@RequestBody Users user) {

        return service.verify(user);
    }
    @GetMapping("/username")
    public Map<String, Object> getUsernameFromToken(HttpServletRequest request){
        return service.getUserInfoFromToken(request);
    }

}
