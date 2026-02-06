package com.example.ecommerce.service;

import com.example.ecommerce.dtos.AuthenticationRequestDto;
import com.example.ecommerce.dtos.AuthenticationResponseDto;
import com.example.ecommerce.dtos.RegisterRequestDto;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {
    ResponseEntity<String> verifyUser(String email, String otpCode);

    ResponseEntity<String> resendVerificationToken(String email);

    AuthenticationResponseDto authenticate(AuthenticationRequestDto request);

    AuthenticationResponseDto register(RegisterRequestDto request);
}


