package com.example.ecommerce.service;

import com.example.ecommerce.dtos.RegisterRequestDto;
import com.example.ecommerce.model.User;

public interface UserService {
    User findByEmail(String email);

    void deleteAll();

    User saveUser(RegisterRequestDto userDto);

    void updateUser(User user);
}
