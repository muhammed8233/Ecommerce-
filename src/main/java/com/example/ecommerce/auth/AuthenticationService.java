package com.example.ecommerce.auth;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse register(RegisterRequest request);
}
