package com.ecopickup.controller;

import com.ecopickup.dto.AuthDtos.*;
import com.ecopickup.model.User;
import com.ecopickup.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.NoSuchElementException;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final PasswordEncoder passwords;
    public AuthController(UserRepository users,PasswordEncoder passwords){this.users=users;this.passwords=passwords;}

    @PostMapping("/signup") @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest input){
        if(users.findByEmailIgnoreCase(input.email()).isPresent()) throw new IllegalArgumentException("An account already exists with this email");
        User user=users.save(new User(input.name(),input.email().toLowerCase(),input.phone(),passwords.encode(input.password()),input.userType(),input.location()));
        return response(user);
    }
    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest input){
        User user=users.findByEmailIgnoreCase(input.email()).orElseThrow(()->new NoSuchElementException("Account not found"));
        if(!passwords.matches(input.password(),user.getPassword())) throw new IllegalArgumentException("Incorrect password");
        return response(user);
    }
    private UserResponse response(User u){return new UserResponse(u.getId(),u.getName(),u.getEmail(),u.getPhone(),u.getUserType(),u.getLocation());}
}
