package com.web.website.controllers;

import com.web.website.models.CartDetails;
import com.web.website.models.Order;
import com.web.website.services.CartServices;
import com.web.website.services.EmailService;
import com.web.website.services.OrderServices;
import com.web.website.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/web")
public class CartController {
    @Autowired
    private CartServices service;

    @Autowired
    private OrderServices orderService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/cart")
    public CartDetails getDetails(@RequestBody CartDetails cart) {
        return service.getDetails(cart);
    }

    @PostMapping("/order")
    public Order saveOrder(@RequestBody Order order) {
        Order savedOrder = orderService.saveOrder(order);
        emailService.sendOrderConfirmationEmail(savedOrder); // Send email
        return savedOrder;
    }

    @GetMapping("/details")
    public List<Order> getDetails() {
        return orderService.getDetails();
    }


}
