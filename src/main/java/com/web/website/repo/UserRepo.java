package com.web.website.repo;

import com.web.website.models.Users;
import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<Users,Integer> {
    Users findByUsername(String username);
    boolean existsByEmail(String email);
}
