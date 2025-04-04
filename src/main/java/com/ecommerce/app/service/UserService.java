package com.ecommerce.app.service;

import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public void saveUser(User user) {
        if (user.getId() == null) {
            if (userExists(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                throw new IllegalArgumentException("Password is required for new user");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userRepository.save(user);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserDetails authenticateUser(String email, String password) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, password);

            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return (UserDetails) authentication.getPrincipal();
        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password", e);
        }
    }
}
