package com.example.ecommerce.service;

import com.example.ecommerce.Enum.Role;
import com.example.ecommerce.dtos.RegisterRequestDto;
import com.example.ecommerce.exception.UserAlreadyExistException;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(user.getEmail(), user.getPassword(), new
                ArrayList<>());
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("user with email: "+ email + " not found"));
    }

    @Override
    public void deleteAll() {
        userRepository.deleteAll();
    }

    @Override
    public User saveUser(RegisterRequestDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistException("Email already exists");
        }

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        user.setPassword(userDto.getPassword());
        user.setRole(Role.ADMIN);

        return userRepository.save(user);
    }
}
