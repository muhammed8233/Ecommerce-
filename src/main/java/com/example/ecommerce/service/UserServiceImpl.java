package com.example.ecommerce.service;

import com.example.ecommerce.Enum.Role;
import com.example.ecommerce.dtos.RegisterRequestDto;
import com.example.ecommerce.exception.UserNotFoundException;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.example.ecommerce.service.PaymentGatewayServiceImpl.log;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(user.getEmail(), user.getPassword(), new
                ArrayList<>());
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("user with email: "+ email + " not found"));
    }

    @Override
    public void deleteAll() {
        userRepository.deleteAll();
    }
    @Override
    public User saveUser(RegisterRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail().toLowerCase().trim())
                .password(dto.getPassword())
                .role(Role.USER)
                .verificationToken(String.valueOf(new java.security.SecureRandom().nextInt(9000) + 1000))
                .tokenExpiry(LocalDateTime.now().plusMinutes(30))
                .isEnabled(false)
                .build();

        User savedUser = userRepository.save(user);

        try{
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationToken());
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", savedUser.getEmail());
        }
        return savedUser;
    }

    @Override
    public User findByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
    }

    @Override
    public void updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new UserNotFoundException("Cannot update: User not found");
        }
        userRepository.save(user);
    }

}
