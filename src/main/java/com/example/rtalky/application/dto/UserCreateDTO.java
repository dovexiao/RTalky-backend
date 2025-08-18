package com.example.rtalky.application.dto;

import lombok.Data;

@Data
public class UserCreateDTO {
    private String username;
    private String password;
    private String phoneNumber;
    private String avatar;
    private String nickname;
    private String bio;
}
