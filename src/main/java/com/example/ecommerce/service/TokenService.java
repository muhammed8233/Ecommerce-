package com.example.ecommerce.service;

import com.example.ecommerce.model.Token;


public interface TokenService {
    void saveToken(Token verificationToken);

    Token findByTokenAndUserId(String otpCode, String id);

    void deleteToken(String id);

    void deleteByUserId(String id);

}
