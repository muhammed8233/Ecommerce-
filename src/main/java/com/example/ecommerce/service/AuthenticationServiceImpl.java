package com.example.ecommerce.service;

import com.example.ecommerce.dtos.AuthenticationRequestDto;
import com.example.ecommerce.dtos.AuthenticationResponseDto;
import com.example.ecommerce.dtos.RegisterRequestDto;
import com.example.ecommerce.Enum.Role;
import com.example.ecommerce.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private  final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;


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
