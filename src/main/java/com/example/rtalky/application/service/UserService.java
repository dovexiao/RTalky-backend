package com.example.rtalky.application.service;

import com.example.rtalky.application.dto.UserCreateDTO;
import com.example.rtalky.domain.model.User;
import com.example.rtalky.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void createUser(UserCreateDTO dto) {
        User user = new User();
        user.setUserId(generateUserId());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // 实际应该加密
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAvatar(dto.getAvatar());
        user.setNickname(dto.getNickname());
        user.setBio(dto.getBio());
        // 设置注册时间
        user.setRegistrationTime(java.time.LocalDateTime.now());
        // 领域逻辑...
        userRepository.save(user);
        // 领域逻辑...
        userRepository.save(user);
    }

    private String generateUserId() {
        // 可以使用UUID或其他ID生成策略
        return UUID.randomUUID().toString();
    }
}
