package com.web.website.repo;

import com.web.website.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepo extends JpaRepository<Address, Integer> {
    List<Address> findByUsernameAndEmail(String username, String email);

}
