package com.example.ecommerce.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "name can not be empty")
    private String name;

    @NotBlank(message = "email can not be empty")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "password must not be empty")
    @Size(min = 4, message = "password size must not be < 4")
    private String password;
}
