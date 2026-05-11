package com.example.ecommerce.controller;

import com.example.ecommerce.dtos.AuthenticationRequestDto;
import com.example.ecommerce.dtos.AuthenticationResponseDto;
import com.example.ecommerce.service.AuthenticationService;
import com.example.ecommerce.dtos.RegisterRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDto> register(@Valid
            @RequestBody RegisterRequestDto request){
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> authenticate(
            @RequestBody @Valid AuthenticationRequestDto request){
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/verify-token")
    public ResponseEntity<String> verifyUser(@RequestParam("email") String email,
                                             @RequestParam("token") String token) {
       return authenticationService.verifyUser(email, token);
    }

    @PostMapping("/resend-token")
    public ResponseEntity<String> resendToken(@RequestParam("email") String email) {
        return authenticationService.resendVerificationToken(email);
    }
}
