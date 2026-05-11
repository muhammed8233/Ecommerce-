package com.example.ecommerce.dtos;
import com.example.ecommerce.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponseDto {
    private String id;
    private String token;
    private String name;
    private String email;
    private Role role;
}