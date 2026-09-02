package com.ecopickup.dto;

import com.ecopickup.model.UserType;
import jakarta.validation.constraints.*;

public final class AuthDtos {
    private AuthDtos() {}
    public record SignupRequest(@NotBlank String name,@Email @NotBlank String email,@NotBlank String phone,@Size(min=6) String password,@NotNull UserType userType,@NotBlank String location){}
    public record LoginRequest(@Email @NotBlank String email,@NotBlank String password){}
    public record UserResponse(Long id,String name,String email,String phone,UserType userType,String location){}
}
