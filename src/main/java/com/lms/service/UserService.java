package com.lms.service;

import com.lms.model.User;
import com.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository, 
                      BCryptPasswordEncoder passwordEncoder,
                      JwtService jwtService,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // Hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        
        // Send welcome email
        emailService.sendWelcomeEmail(savedUser);
        
        return savedUser;
    }

    public String authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateToken(user);
    }

    public User updateUserRole(Long userId, User.Role newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public List<User> findUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Send password change notification
        emailService.sendPasswordChangeNotification(user);
    }
}
