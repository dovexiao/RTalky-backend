package com.rtalky.application.service.login;

import com.rtalky.domain.model.AuthMethod;
import com.rtalky.domain.model.User;

/**
 * Strategy interface for handling login flows per authentication method.
 * Located in application layer to orchestrate use-case specific logic while
 * delegating domain behaviors to entities/services.
 */
public interface LoginHandler {

    boolean supports(AuthMethod method);

    /**
     * Execute the login-specific user flow: find/create user, validate status,
     * update agreement/auth method as needed, and persist the user.
     * Method-specific validations (e.g., SMS code, password check) should be
     * performed by the caller before invoking this method.
     */
    User handle(String phoneNumber, String deviceFingerprint, boolean agreedToTerms, String deviceInfo);
}


