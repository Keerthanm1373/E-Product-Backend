package com.web.website.services;

import com.web.website.models.CartDetails;
import com.web.website.models.Order;
import com.web.website.repo.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServices {
    @Autowired
    private OrderRepo orderRepo;

    public Order saveOrder(Order order) {
        if (order.getItems() != null) {
            for (CartDetails item : order.getItems()) {
                item.setOrder(order);
            }
        }
        return orderRepo.save(order);
    }


    public List<Order> getDetails() {
        return orderRepo.findAll();
    }


}
