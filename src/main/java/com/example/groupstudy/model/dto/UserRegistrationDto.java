package com.example.groupstudy.model.dto;

import lombok.Data;

@Data
public class UserRegistrationDto {
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private String fullName;
}
