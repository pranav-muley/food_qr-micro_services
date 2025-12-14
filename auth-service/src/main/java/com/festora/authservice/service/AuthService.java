package com.festora.authservice.service;

import com.festora.authservice.model.User;
import com.festora.authservice.repository.UserRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String getLoginDetails(User user) {
        Optional<User> userOptional = userRepository.getUserByEmail(user.getEmail());
        if (userOptional.isEmpty()) {
            return "Please register your account first";
        }
        User foundUser = userOptional.get();
        if (!passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            return "Password does not match, check password";
        }
        return "logged in successfully";
    }

    public String registerUser(User user) {
        if (ObjectUtils.isEmpty(user)) {
            return "Please enter your account details";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }
}
