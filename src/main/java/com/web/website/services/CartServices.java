package com.web.website.services;

import com.web.website.models.CartDetails;
import com.web.website.repo.CartRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServices {
    @Autowired
    private CartRepo repo;

    public CartDetails getDetails(CartDetails cart){
        return repo.save(cart);
    }




}
