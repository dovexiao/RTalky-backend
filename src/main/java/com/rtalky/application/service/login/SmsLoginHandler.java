package com.rtalky.application.service.login;

import com.rtalky.application.exception.SmsLoginException;
import com.rtalky.domain.model.AuthMethod;
import com.rtalky.domain.model.User;
import com.rtalky.domain.model.UserStatus;
import com.rtalky.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsLoginHandler implements LoginHandler {

    private final UserRepository userRepository;

    @Override
    public boolean supports(AuthMethod method) {
        return method == AuthMethod.SMS;
    }

    @Override
    public LoginResult handle(String phoneNumber, String deviceFingerprint, boolean agreedToTerms, String deviceInfo) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        if (userOptional.isEmpty()) {
            // Register new user for SMS login
            User newUser = User.newUser(phoneNumber, deviceFingerprint, agreedToTerms, AuthMethod.SMS);
            newUser = userRepository.save(newUser);
            log.info("SMS登录注册新用户: userId={}, phoneNumber={}", newUser.getUserId(), phoneNumber);
            return new LoginResult(newUser, true);
        }

        User user = getUser(agreedToTerms, userOptional);
        user = userRepository.save(user);
        log.info("SMS登录使用已有用户: userId={}, phoneNumber={}", user.getUserId(), phoneNumber);
        return new LoginResult(user, false);
    }

    private static User getUser(boolean agreedToTerms, Optional<User> userOptional) {
        User user = userOptional.get();
        // Check status
        if (!user.isAccountActive()) {
            if (user.getStatus() == UserStatus.DISABLED) {
                throw new SmsLoginException(SmsLoginException.SmsLoginErrorCode.ACCOUNT_DISABLED,
                    "账号已被禁用，请联系客服");
            } else if (user.getStatus() == UserStatus.LOCKED) {
                throw new SmsLoginException(SmsLoginException.SmsLoginErrorCode.ACCOUNT_LOCKED,
                    "账号已被锁定，请联系客服");
            } else {
                throw new SmsLoginException(SmsLoginException.SmsLoginErrorCode.ACCOUNT_DISABLED,
                    "账号不可用，状态: " + user.getStatus());
            }
        }

        // Update agreement if newly agreed
        if (agreedToTerms && !user.isAgreedToTerms()) {
            user.verifyAgreement(agreedToTerms);
        }
        // Update auth method if changed
        if (user.getAuthMethod() != AuthMethod.SMS) {
            user.setAuthMethod(AuthMethod.SMS);
        }
        return user;
    }
}


