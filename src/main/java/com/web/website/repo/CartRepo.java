package com.web.website.repo;

import com.web.website.models.CartDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepo extends JpaRepository<CartDetails, Integer> {
}
