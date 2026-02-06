package com.example.ecommerce.repository;

import com.example.ecommerce.model.Token;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TokenRepository extends MongoRepository<Token, String> {


    Token findByTokenAndUserId(String token, String id);

    void deleteByUserId(String id);
}
