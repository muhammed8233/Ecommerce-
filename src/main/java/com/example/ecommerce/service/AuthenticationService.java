package com.example.ecommerce.service;

import com.example.ecommerce.auth.AuthenticationRequest;
import com.example.ecommerce.auth.AuthenticationResponse;
import com.example.ecommerce.auth.RegisterRequest;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse register(RegisterRequest request);
}
