package com.rtalky.interfaces.converter;

import com.rtalky.application.dto.LoginResponse;
import com.rtalky.application.dto.LoginApiResponse;
import com.rtalky.application.dto.SmsSendResponse;
import com.rtalky.application.dto.UserInfoResponse;
import com.rtalky.application.dto.NotePageResponse;
import com.rtalky.application.dto.NoteResponse;
import com.rtalky.domain.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 响应构建器
 * 构建各种认证响应
 */
@Component("rtalkyResponseBuilder")
public class ResponseBuilder {

    /**
     * 构建登录成功响应
     */
    public LoginResponse buildLoginResponse(User user, String sessionToken, int tokenExpiryHours) {
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getUserId());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setSessionToken(sessionToken);
        response.setLoginTime(LocalDateTime.now());
        response.setTokenExpiryTime(LocalDateTime.now().plusHours(tokenExpiryHours));

        // 构建用户资料
        LoginResponse.UserProfile profile = new LoginResponse.UserProfile();
        profile.setNickname(user.getNickname());
        profile.setAvatar(user.getAvatar());
        profile.setBio(user.getBio());
        response.setUserProfile(profile);

        return response;
    }

    /**
     * 构建错误响应
     */
    public LoginResponse buildErrorResponse(String errorMessage) {
        LoginResponse response = new LoginResponse();
        response.setUserId(null);
        response.setPhoneNumber(null);
        response.setSessionToken(null);
        response.setLoginTime(LocalDateTime.now());

        // 可以设置错误信息到用户资料或其他字段
        LoginResponse.UserProfile profile = new LoginResponse.UserProfile();
        profile.setNickname("Error");
        profile.setBio(errorMessage);
        response.setUserProfile(profile);

        return response;
    }

    /**
     * 构建简单的成功响应
     */
    public SimpleResponse buildSimpleResponse(boolean success, String message) {
        return new SimpleResponse(success, message, LocalDateTime.now());
    }

    /**
     * 构建短信发送成功响应
     */
    public SmsSendResponse buildSmsSendSuccessResponse(String message, Integer codeDigits) {
        return new SmsSendResponse(true, message, LocalDateTime.now(), codeDigits);
    }

    /**
     * 构建短信发送失败响应
     */
    public SmsSendResponse buildSmsSendFailureResponse(String message) {
        return new SmsSendResponse(false, message, LocalDateTime.now(), null);
    }

    /**
     * 构建登录成功API响应
     */
    public LoginApiResponse buildLoginSuccessResponse(String message, LoginResponse loginData) {
        return new LoginApiResponse(true, message, LocalDateTime.now(), loginData);
    }

    /**
     * 构建登录失败API响应
     */
    public LoginApiResponse buildLoginFailureResponse(String message) {
        return new LoginApiResponse(false, message, LocalDateTime.now(), null);
    }

    /**
     * 构建用户信息成功响应
     */
    public UserInfoResponse buildUserInfoSuccessResponse(String message, UserInfoResponse.UserInfo userInfo) {
        return new UserInfoResponse(true, message, LocalDateTime.now(), userInfo);
    }

    /**
     * 构建用户信息失败响应
     */
    public UserInfoResponse buildUserInfoFailureResponse(String message) {
        return new UserInfoResponse(false, message, LocalDateTime.now(), null);
    }

    /**
     * 从User对象构建UserInfo
     */
    public UserInfoResponse.UserInfo buildUserInfo(User user) {
        return new UserInfoResponse.UserInfo(
            user.getUserId(),
            user.getNickname(),
            user.getBio(),
            user.getAvatar()
        );
    }

    /**
     * 构建笔记分页成功响应
     */
    public NotePageResponse buildNotePageSuccessResponse(String message, NotePageResponse data) {
        // NotePageResponse本身就是分页数据，直接返回
        return data;
    }

    /**
     * 构建笔记分页失败响应
     */
    public NotePageResponse buildNotePageFailureResponse(String message) {
        NotePageResponse response = new NotePageResponse();
        response.setContent(List.of());
        response.setPage(0);
        response.setSize(0);
        response.setTotalElements(0);
        response.setTotalPages(0);
        response.setFirst(true);
        response.setLast(true);
        response.setHasNext(false);
        response.setHasPrevious(false);
        return response;
    }

    /**
     * 构建笔记成功响应
     */
    public NoteResponse buildNoteSuccessResponse(String message, NoteResponse data) {
        // NoteResponse本身就是笔记数据，直接返回
        return data;
    }

    /**
     * 构建笔记失败响应
     */
    public NoteResponse buildNoteFailureResponse(String message) {
        NoteResponse response = new NoteResponse();
        response.setNoteId("");
        response.setDisplayId("");
        response.setTitle("");
        response.setDescription("");
        response.setContent("");
        response.setTags(List.of());
        return response;
    }

    /**
     * 简单的响应对象
     *
     * @param success getters
     */
    public record SimpleResponse(boolean success, String message, LocalDateTime timestamp) {}
}
