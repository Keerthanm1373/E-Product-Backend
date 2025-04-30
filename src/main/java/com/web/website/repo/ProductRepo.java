package com.web.website.repo;

import com.web.website.models.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Products,Long> {

    Optional<Products> findById(Long id);
}
