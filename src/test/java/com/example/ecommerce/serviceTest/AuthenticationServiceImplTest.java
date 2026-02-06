package com.example.ecommerce.serviceTest;

import com.example.ecommerce.dtos.AuthenticationRequestDto;
import com.example.ecommerce.dtos.AuthenticationResponseDto;
import com.example.ecommerce.dtos.RegisterRequestDto;
import com.example.ecommerce.model.Token;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.AuthenticationService;
import com.example.ecommerce.service.TokenService;
import com.example.ecommerce.service.TokenServiceImpl;
import com.example.ecommerce.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthenticationServiceImplTest {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        userService.deleteAll();
    }

    @Test
    void register_ShouldSaveUserAndEncodePassword() {

        RegisterRequestDto request = RegisterRequestDto.builder()
                .name("John")
                .email("john@example.com")
                .password("password123")
                .build();

        AuthenticationResponseDto response = authService.register(request);

        assertNotNull(response.getToken());

        User savedUser = userService.findByEmail("john@example.com");
        assertEquals("John", savedUser.getName());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsAreValid() {
        RegisterRequestDto user = RegisterRequestDto.builder()
                .name("Jane")
                .email("jane@example.com")
                .password(passwordEncoder.encode("secret"))
                .build();
        User user1 = userService.saveUser(user);
        String userToken = String.valueOf(new java.security.SecureRandom().nextInt(9000) + 1000);
        Token token = Token.builder()
                .token(userToken)
                .userId(user1.getId())
                .createdAt(LocalDateTime.now())
                .build();
        tokenService.saveToken(token);
        authService.verifyUser(user1.getEmail(), userToken);

        AuthenticationRequestDto authRequest = AuthenticationRequestDto.builder()
                .email("jane@example.com")
                .password("secret")
                .build();

        AuthenticationResponseDto response = authService.authenticate(authRequest);

        assertNotNull(response.getToken());

    }

    @Test
    void authenticate_ShouldThrowException_WhenPasswordIsIncorrect() {

        RegisterRequestDto user = RegisterRequestDto.builder()
                .email("fail@example.com")
                .password(passwordEncoder.encode("correct-password"))
                .build();
        userService.saveUser(user);

        AuthenticationRequestDto badRequest = AuthenticationRequestDto.builder()
                .email("fail@example.com")
                .password("wrong-password")
                .build();

        assertThrows(Exception.class, () -> {
            authService.authenticate(badRequest);
        });
    }
}
