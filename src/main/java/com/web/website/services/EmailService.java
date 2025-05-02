package com.web.website.services;

import com.web.website.models.CartDetails;
import com.web.website.models.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String toEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@eproduct.com");
        message.setTo(toEmail);
        message.setSubject("Welcome to Our Website!");
        message.setText("Hi " + username + ",\n\nThank you for registering with us!\n\nRegards,\nTeam");

        mailSender.send(message);
    }

    public void sendOrderConfirmationEmail(Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@eproduct.com");
        message.setTo(order.getEmail());
        message.setSubject("Order Confirmation - Thank You for Your Purchase!");

        StringBuilder sb = new StringBuilder();
        sb.append("Hi ").append(order.getUsername()).append(",\n\n");
        sb.append("Thank you for your purchase! \nHere are your order details:\n\n");

        for (CartDetails item : order.getItems()) {
            sb.append("-> ").append(item.getProductname())
                    .append(" \n Quantity: ").append(item.getQuantity())
                    .append(" | Price: ₹").append(item.getPrice()).append("\n");
        }
        sb.append("\nTotal Cost: ₹").append(order.getTotal()).append("\n");

        sb.append("\nDelivery Address:\n")
                .append(order.getUsername()).append(",\n")
                .append(order.getNumber()).append(",\n")
                .append(order.getLandmark()).append(",\n")
                .append(order.getCity()).append(", ")
                .append(order.getState()).append(" - ")
                .append(order.getPincode()).append("\n");

        sb.append("\nWe appreciate your business!\n\nBest regards,\nYour Company Team");

        message.setText(sb.toString());

        mailSender.send(message);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@eproduct.com");
        message.setTo(toEmail);
        message.setSubject("Your OTP for Registration");
        message.setText("Your OTP is: " + otp + "\nIt is valid for 5 minutes.");
        mailSender.send(message);
    }

}
