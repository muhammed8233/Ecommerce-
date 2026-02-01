package com.example.ecommerce.serviceTest;

import com.example.ecommerce.dtos.AuthenticationRequestDto;
import com.example.ecommerce.dtos.AuthenticationResponseDto;
import com.example.ecommerce.dtos.RegisterRequestDto;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.AuthenticationService;
import com.example.ecommerce.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

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
        assertEquals("john", savedUser.getName());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsAreValid() {
        RegisterRequestDto user = RegisterRequestDto.builder()
                .name("Jane")
                .email("jane@example.com")
                .password(passwordEncoder.encode("secret"))
                .build();
        userService.saveUser(user);

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
