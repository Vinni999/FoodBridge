package com.foodbridges.service;

import org.springframework.stereotype.Service;

import com.foodbridges.dto.RegisterRequest;
import com.foodbridges.entity.User;
import com.foodbridges.repositary.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(RegisterRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User(
                req.getName(),
                req.getEmail(),
                req.getPassword(), 
                req.getRole(),
                req.getPhone()
        );

        return userRepository.save(user);
    }
}
