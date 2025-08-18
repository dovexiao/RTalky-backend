package com.example.rtalky.interfaces.rest;

import com.example.rtalky.application.dto.UserCreateDTO;
import com.example.rtalky.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserCreateDTO dto) {
        userService.createUser(dto);
        return ResponseEntity.ok().build();
    }
}
