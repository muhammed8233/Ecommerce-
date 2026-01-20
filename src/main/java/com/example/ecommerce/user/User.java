package com.example.ecommerce.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user")
public class User implements UserDetails {
    @Id
    private String id;

    @NotBlank(message = "name can not be empty")
    @Size(min = 2, message = "name size must not be < 2")
    private String name;

    @NotBlank(message = "email can not be empty")
    @Email(message = "Please provide a valid email address")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "password must not be empty")
    @Size(min = 4, message = "password size must not be < 4")
    private String password;

    @NotNull
    private Role role;

    @Version
    private int version;


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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
