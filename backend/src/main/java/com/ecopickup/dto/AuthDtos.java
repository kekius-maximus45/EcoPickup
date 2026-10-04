package com.ecopickup.dto;

import com.ecopickup.model.UserType;
import jakarta.validation.constraints.*;

public final class AuthDtos {
    private AuthDtos() {}
    public record SignupRequest(@NotBlank String name,@Email @NotBlank String email,@NotBlank String phone,@Size(min=6) String password,@NotNull UserType userType,@NotBlank String location){}
    public record LoginRequest(@Email @NotBlank String email,@NotBlank String password){}
    public record LoginChallengeResponse(String challengeId,String maskedEmail,long expiresInSeconds,String deliveryMode){}
    public record VerifyOtpRequest(@NotBlank String challengeId,@Pattern(regexp="\\d{6}",message="Enter the 6-digit verification code") String code){}
    public record UserResponse(Long id,String name,String email,String phone,UserType userType,String location){}
}
