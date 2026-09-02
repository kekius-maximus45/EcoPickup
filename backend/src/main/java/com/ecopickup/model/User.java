package com.ecopickup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String phone;
    @JsonIgnore @Column(nullable = false) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private UserType userType;
    @Column(nullable = false) private String location;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}
    public User(String name, String email, String phone, String password, UserType userType, String location) {
        this.name=name; this.email=email; this.phone=phone; this.password=password; this.userType=userType; this.location=location;
    }
    public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;} public UserType getUserType(){return userType;} public void setUserType(UserType v){userType=v;}
    public String getLocation(){return location;} public void setLocation(String v){location=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
