package com.example.ecommerce.service;

import com.example.ecommerce.dtos.AuthenticationRequestDto;
import com.example.ecommerce.dtos.AuthenticationResponseDto;
import com.example.ecommerce.dtos.RegisterRequestDto;
import com.example.ecommerce.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private  final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;


    @Override
    public AuthenticationResponseDto register(RegisterRequestDto request) {

        RegisterRequestDto user = RegisterRequestDto
                .builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        User saved = userService.saveUser(user);
        String jwtToken = jwtService.generateToken(saved);
        return AuthenticationResponseDto
                .builder()
                .token(jwtToken)
                .build();
    }

    @Override
    public ResponseEntity<String> verifyToken(String token) {
        User user = userService.findByVerificationToken(token);

        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token has expired. please try again with new token");
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        userService.updateUser(user);

        return ResponseEntity.ok("Account verified successfully! You can now log in.");
    }

    @Override
    public ResponseEntity<String> resendVerificationToken(String email) {
        User user = userService.findByEmail(email);

        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body("Account is already verified.");
        }

        String newToken = String.valueOf(new java.security.SecureRandom().nextInt(9000) + 1000);
        user.setVerificationToken(newToken);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(15));

        userService.updateUser(user);

        try {
            emailService.sendVerificationEmail(user.getEmail(), newToken);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error sending email. Please try again.");
        }

        return ResponseEntity.ok("A new 4-digit verification code has been sent to your email.");
    }

    @Override
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())
        );
        User user = userService.findByEmail(request.getEmail());
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponseDto
                .builder()
                .token(jwtToken)
                .build();
    }
}
