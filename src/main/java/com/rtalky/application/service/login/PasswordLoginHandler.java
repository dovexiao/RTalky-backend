package com.rtalky.application.service.login;

import com.rtalky.application.exception.PasswordLoginException;
import com.rtalky.domain.model.AuthMethod;
import com.rtalky.domain.model.User;
import com.rtalky.domain.model.UserStatus;
import com.rtalky.domain.repository.UserRepository;
import com.rtalky.domain.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordLoginHandler implements LoginHandler {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    @Override
    public boolean supports(AuthMethod method) {
        return method == AuthMethod.PASSWORD;
    }

    @Override
    public LoginResult handle(String phoneNumber, String deviceFingerprint, boolean agreedToTerms, String deviceInfo) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        if (userOptional.isEmpty()) {
            throw new PasswordLoginException(PasswordLoginException.PasswordLoginErrorCode.USER_NOT_FOUND,
                "用户不存在");
        }

        User user = getUser(userOptional);

        // Update agreement if newly agreed
        if (agreedToTerms && !user.isAgreedToTerms()) {
            user.verifyAgreement(agreedToTerms);
        }
        // Update auth method if changed
        if (user.getAuthMethod() != AuthMethod.PASSWORD) {
            user.setAuthMethod(AuthMethod.PASSWORD);
        }
        user = userRepository.save(user);
        log.info("密码登录使用已有用户: userId={}, phoneNumber={}", user.getUserId(), phoneNumber);
        return new LoginResult(user, false); // 密码登录不会创建新用户
    }

    private static User getUser(Optional<User> userOptional) {
        User user = userOptional.get();
        // must have password set
        if (!user.hasPassword()) {
            throw new PasswordLoginException(PasswordLoginException.PasswordLoginErrorCode.PASSWORD_NOT_SET,
                "该用户未设置密码");
        }
        // Check status
        if (!user.isAccountActive()) {
            if (user.getStatus() == UserStatus.DISABLED) {
                throw new PasswordLoginException(PasswordLoginException.PasswordLoginErrorCode.ACCOUNT_DISABLED,
                    "账号已被禁用，请联系客服");
            } else if (user.getStatus() == UserStatus.LOCKED) {
                throw new PasswordLoginException(PasswordLoginException.PasswordLoginErrorCode.ACCOUNT_LOCKED,
                    "账号已被锁定，请联系客服");
            } else {
                throw new PasswordLoginException(PasswordLoginException.PasswordLoginErrorCode.ACCOUNT_DISABLED,
                    "账号不可用，状态: " + user.getStatus());
            }
        }
        return user;
    }
}


