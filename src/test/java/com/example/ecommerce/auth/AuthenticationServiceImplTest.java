package com.example.ecommerce.auth;

import com.example.ecommerce.user.Role;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_ShouldSaveUserAndEncodePassword() {

        RegisterRequest request = RegisterRequest.builder()
                .name("John")
                .email("john@example.com")
                .password("password123")
                .build();

        AuthenticationResponse response = authService.register(request);

        assertNotNull(response.getToken(), "Access token should be generated");

        User savedUser = userRepository.findByEmail("john@example.com").orElseThrow();
        assertEquals("John", savedUser.getName());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()),
                "Password should be stored in an encoded format");
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsAreValid() {
        User user = User.builder()
                .name("Jane")
                .email("jane@example.com")
                .password(passwordEncoder.encode("secret"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .email("jane@example.com")
                .password("secret")
                .build();

        AuthenticationResponse response = authService.authenticate(authRequest);

        assertNotNull(response.getToken());

    }

    @Test
    void authenticate_ShouldThrowException_WhenPasswordIsIncorrect() {

        User user = User.builder()
                .email("fail@example.com")
                .password(passwordEncoder.encode("correct-password"))
                .build();
        userRepository.save(user);

        AuthenticationRequest badRequest = AuthenticationRequest.builder()
                .email("fail@example.com")
                .password("wrong-password")
                .build();

        assertThrows(Exception.class, () -> {
            authService.authenticate(badRequest);
        });
    }
}
