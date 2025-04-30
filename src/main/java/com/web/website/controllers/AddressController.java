package com.web.website.controllers;

import com.web.website.models.Address;
import com.web.website.services.AddressServices;
import com.web.website.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/web")
public class AddressController {
    @Autowired
    private AddressServices service;

    @Autowired
    private UserService userService;

    @GetMapping("/address")
    public List<Address> getAddress(){

        return service.getAddress();
    }

    @PostMapping("/addAddress")
    public Address addAddress(@RequestBody Address address){

        return  service.addAddress(address);
    }

    @PutMapping("/updateAddress")
    public Address updateAddress(@RequestBody Address address){

        return service.updateAddress(address);
    }

    @GetMapping("/userDetails")
    public Map<String, Object> getUsernameFromToken(HttpServletRequest request){
        return userService.getUserInfoFromToken(request);
    }

    @GetMapping("/user/address")
    public List<Address> getUserAddress(HttpServletRequest request) {
        Map<String, Object> userInfo = userService.getUserInfoFromToken(request);
        if (userInfo.containsKey("username") && userInfo.containsKey("email")) {
            String username = (String) userInfo.get("username");
            String email = (String) userInfo.get("email");
            return service.getAddressByUsernameAndEmail(username, email);
        }
        return List.of();
    }


}
