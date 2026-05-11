package com.example.ecommerce.service;

import com.example.ecommerce.model.Token;
import com.example.ecommerce.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService{
    @Autowired
    private TokenRepository tokenRepository;

    @Override
    public void saveToken(Token verificationToken) {
        tokenRepository.save(verificationToken);
    }

    @Override
    public Token findByTokenAndUserId(String token, String id) {
        return tokenRepository.findByTokenAndUserId(token, id);
    }

    @Override
    public void deleteToken(String id) {
        tokenRepository.deleteById(id);

    }

    @Override
    public void deleteByUserId(String id) {
        tokenRepository.deleteByUserId(id);
    }
}
