package com.example.ecommerce.service;

import com.example.ecommerce.model.User;

public interface UserService {
    User findByEmail(String email);

    void deleteAll();
}
