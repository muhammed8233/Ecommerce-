package com.example.ecommerce.service;

import com.example.ecommerce.dtos.AuthenticationRequestDto;
import com.example.ecommerce.dtos.AuthenticationResponseDto;
import com.example.ecommerce.dtos.RegisterRequestDto;

public interface AuthenticationService {
    AuthenticationResponseDto authenticate(AuthenticationRequestDto request);

    AuthenticationResponseDto register(RegisterRequestDto request);
}
