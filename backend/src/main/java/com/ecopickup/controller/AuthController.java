package com.ecopickup.controller;

import com.ecopickup.dto.AuthDtos.*;
import com.ecopickup.model.User;
import com.ecopickup.repository.UserRepository;
import com.ecopickup.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.NoSuchElementException;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final PasswordEncoder passwords; private final OtpService otpService;
    public AuthController(UserRepository users,PasswordEncoder passwords,OtpService otpService){this.users=users;this.passwords=passwords;this.otpService=otpService;}

    @PostMapping("/signup") @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest input){
        if(users.findByEmailIgnoreCase(input.email()).isPresent()) throw new IllegalArgumentException("An account already exists with this email");
        User user=users.save(new User(input.name(),input.email().toLowerCase(),input.phone(),passwords.encode(input.password()),input.userType(),input.location()));
        return response(user);
    }
    @PostMapping("/login")
    public LoginChallengeResponse login(@Valid @RequestBody LoginRequest input){
        User user=users.findByEmailIgnoreCase(input.email()).orElseThrow(()->new NoSuchElementException("Account not found"));
        if(!passwords.matches(input.password(),user.getPassword())) throw new IllegalArgumentException("Incorrect password");
        return otpService.createChallenge(user);
    }
    @PostMapping("/verify-otp")
    public UserResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest input){return response(otpService.verify(input.challengeId(),input.code()));}
    private UserResponse response(User u){return new UserResponse(u.getId(),u.getName(),u.getEmail(),u.getPhone(),u.getUserType(),u.getLocation());}
}
