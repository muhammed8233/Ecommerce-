package com.example.ecommerce.service;

import ch.qos.logback.classic.Logger;
import com.example.ecommerce.Enum.Role;
import com.example.ecommerce.dtos.RegisterRequestDto;
import com.example.ecommerce.exception.UserAlreadyExistException;
import com.example.ecommerce.exception.UserNotFoundException;
import com.example.ecommerce.model.Token;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TokenService tokenService;


    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new UserNotFoundException("user with email: "+ email + " not found"));
    }

    @Override
    public void deleteAll() {
        userRepository.deleteAll();
    }
    @Override
    public User saveUser(RegisterRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistException("User with email: "+ dto.getEmail() +" already exist");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail().toLowerCase().trim())
                .password(dto.getPassword())
                .role(Role.USER)
                .isEnabled(false)
                .build();

        User savedUser = userRepository.save(user);

        String secretToken = String.valueOf(new java.security.SecureRandom().nextInt(9000) + 1000);
        Token verificationToken = Token.builder()
                .token(secretToken)
                .userId(savedUser.getId())
                .createdAt(LocalDateTime.now())
                .build();
       tokenService.saveToken(verificationToken);

        try{
        emailService.sendVerificationEmail(savedUser.getEmail(), secretToken);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", savedUser.getEmail());
        }
        return savedUser;
    }

    @Override
    public void updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new UserNotFoundException("Cannot update: "+ user.getId() + " User not found");
        }
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new UserNotFoundException("User with email: "+ email +" not found"));
    }
}