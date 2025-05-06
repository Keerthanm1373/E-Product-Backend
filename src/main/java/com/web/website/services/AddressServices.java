package com.web.website.services;

import com.web.website.models.Address;
import com.web.website.repo.AddressRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServices {
    @Autowired
    private AddressRepo repo;

    public List<Address> getAddress() {
        return repo.findAll();
    }

    public Address updateAddress(Address address) {
        return repo.save(address);
    }

    public Address addAddress(Address address) {
        return repo.save(address);
    }

    public List<Address> getAddressByUsernameAndEmail(String username, String email) {
        return repo.findByUsernameAndEmail(username, email);
    }

}
