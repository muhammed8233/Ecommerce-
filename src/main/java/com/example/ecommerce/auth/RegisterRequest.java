package com.example.ecommerce.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "name can not be empty")
    private String name;

    @NotNull(message = "email can not be empty")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotNull(message = "password must not be empty")
    @Size(min = 4, message = "password size must not be < 4")
    private String password;

    public void setEmail(String email) {
        if (email != null ) {
            this.email = email.toLowerCase().trim();
        }else {
            this.email = null;
        }
    }

    public void setName(String name) {
        if(name != null) {
            this.name = name.toLowerCase().trim();
        }else {
            this.name = null;
        }
    }

}
