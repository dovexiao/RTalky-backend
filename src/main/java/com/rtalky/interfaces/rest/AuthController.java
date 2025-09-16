package com.rtalky.interfaces.rest;

import com.rtalky.application.dto.LoginRequest;
import com.rtalky.application.dto.LoginResponse;
import com.rtalky.application.dto.LoginApiResponse;
import com.rtalky.application.dto.SmsSendRequest;
import com.rtalky.application.dto.SmsSendResponse;
import com.rtalky.application.dto.UserInfoResponse;
import com.rtalky.application.dto.UpdateUserInfoRequest;
import com.rtalky.application.exception.SmsSendException;
import com.rtalky.application.exception.SmsLoginException;
import com.rtalky.application.exception.SessionValidationException;
import com.rtalky.application.service.AuthenticationAppService;
import com.rtalky.domain.model.AuthMethod;
import com.rtalky.interfaces.converter.AuthParameterConverter;
import com.rtalky.interfaces.converter.DeviceInfoConverter;
import com.rtalky.interfaces.converter.LoginConverter;
import com.rtalky.interfaces.converter.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证REST控制器
 * 处理所有认证相关的HTTP请求
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口，包括登录、注册、短信验证等")
public class AuthController {

    private final AuthenticationAppService authenticationAppService;
    private final LoginConverter loginConverter;
    private final DeviceInfoConverter deviceInfoConverter;
    private final AuthParameterConverter authParameterConverter;

    @Qualifier("rtalkyResponseBuilder")
    private final ResponseBuilder responseBuilder;

    @Value("${sms.code.digits:6}")
    private int smsCodeDigits;

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms-code")
    @Operation(
        summary = "发送短信验证码",
        description = "向指定手机号发送短信验证码，用于短信登录验证"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "短信发送成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SmsSendResponse.class),
                examples = @ExampleObject(
                    name = "发送成功",
                    value = "{\"success\":true,\"message\":\"短信验证码发送成功\",\"timestamp\":\"2024-01-01T12:00:00\",\"codeDigits\":6}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SmsSendResponse.class),
                examples = @ExampleObject(
                    name = "未同意协议",
                    value = "{\"success\":false,\"message\":\"必须同意用户协议和隐私政策\",\"timestamp\":\"2024-01-01T12:00:00\",\"codeDigits\":null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "短信发送失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SmsSendResponse.class),
                examples = @ExampleObject(
                    name = "发送失败",
                    value = "{\"success\":false,\"message\":\"短信验证码发送失败，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\",\"codeDigits\":null}"
                )
            )
        )
    })
    public ResponseEntity<SmsSendResponse> sendSmsCode(
        @Parameter(
            description = "短信发送请求参数",
            required = true,
            schema = @Schema(implementation = SmsSendRequest.class)
        )
        @Valid @RequestBody SmsSendRequest request
    ) {
        log.info("请求发送短信验证码: phoneNumber={}", request.getPhoneNumber());

        try {
        boolean result = authenticationAppService.sendSmsCode(request);

        if (result) {
                log.info("短信验证码发送成功: phoneNumber={}, codeDigits={}", request.getPhoneNumber(), smsCodeDigits);
                return ResponseEntity.ok(responseBuilder.buildSmsSendSuccessResponse("短信验证码发送成功", smsCodeDigits));
        } else {
            log.error("短信验证码发送失败: phoneNumber={}", request.getPhoneNumber());
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildSmsSendFailureResponse("短信验证码发送失败，请稍后重试"));
            }
        } catch (SmsSendException e) {
            log.warn("短信发送业务异常: phoneNumber={}, errorCode={}, message={}",
                request.getPhoneNumber(), e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SmsSendException.SmsSendErrorCode.AGREEMENT_NOT_ACCEPTED ||
                e.getErrorCode() == SmsSendException.SmsSendErrorCode.PHONE_NUMBER_REQUIRED ||
                e.getErrorCode() == SmsSendException.SmsSendErrorCode.INVALID_PHONE_NUMBER) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildSmsSendFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == SmsSendException.SmsSendErrorCode.DEVICE_LIMITED ||
                       e.getErrorCode() == SmsSendException.SmsSendErrorCode.SMS_ALREADY_SENT) {
                // 请求过于频繁，返回429
                return ResponseEntity.status(429)
                        .body(responseBuilder.buildSmsSendFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildSmsSendFailureResponse(e.getMessage()));
            }
        } catch (Exception e) {
            log.error("短信发送系统异常: phoneNumber={}, error={}", request.getPhoneNumber(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildSmsSendFailureResponse("系统异常，请稍后重试"));
        }
    }

    /**
     * 一键登录
     */
//    @PostMapping("/one-click")
//    @Operation(
//        summary = "一键登录",
//        description = "使用一键登录令牌进行用户认证，支持自动注册新用户"
//    )
//    @ApiResponses(value = {
//        @ApiResponse(
//            responseCode = "200",
//            description = "登录成功",
//            content = @Content(
//                mediaType = "application/json",
//                schema = @Schema(implementation = LoginResponse.class),
//                examples = @ExampleObject(
//                    name = "登录成功",
//                    value = "{\"userId\":\"U123456789\",\"phoneNumber\":\"13800138000\",\"sessionToken\":\"token123\",\"loginTime\":\"2024-01-01T12:00:00\",\"tokenExpiryTime\":\"2024-01-02T12:00:00\",\"userProfile\":{\"nickname\":\"用户昵称\",\"avatar\":\"data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...\",\"bio\":\"个人简介\"}}"
//                )
//            )
//        ),
//        @ApiResponse(
//            responseCode = "400",
//            description = "请求参数错误",
//            content = @Content(
//                mediaType = "application/json",
//                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
//                examples = @ExampleObject(
//                    name = "认证方式不匹配",
//                    value = "{\"success\":false,\"message\":\"认证方式不匹配\",\"timestamp\":\"2024-01-01T12:00:00\"}"
//                )
//            )
//        )
//    })
//    public ResponseEntity<?> oneClickLogin(
//        @Parameter(
//            description = "一键登录请求参数",
//            required = true,
//            schema = @Schema(implementation = LoginRequest.class)
//        )
//        @Valid @RequestBody LoginRequest request
//    ) {
//        log.info("一键登录请求: phoneNumber={}", request.getPhoneNumber());
//
//        // 验证认证方式
//        if (request.getAuthMethod() != AuthMethod.ONE_CLICK) {
//            return ResponseEntity.badRequest()
//                    .body(responseBuilder.buildSimpleResponse(false, "认证方式不匹配"));
//        }
//
//        // 验证协议同意状态
//        if (!request.getAgreedToTerms()) {
//            return ResponseEntity.badRequest()
//                    .body(responseBuilder.buildSimpleResponse(false, "必须同意用户协议和隐私政策"));
//        }
//
//        try {
//            LoginResponse response = authenticationAppService.oneClickLogin(request);
//            log.info("一键登录成功: userId={}", response.getUserId());
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            log.error("一键登录失败: phoneNumber={}, error={}",
//                    request.getPhoneNumber(), e.getMessage(), e);
//            return handleLoginException(e);
//        }
//    }

    /**
     * 短信验证码登录
     */
    @PostMapping("/sms-login")
    @Operation(
        summary = "短信验证码登录",
        description = "使用短信验证码进行用户认证，支持自动注册新用户。需要先调用发送短信验证码接口获取验证码。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "登录成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginApiResponse.class),
                examples = @ExampleObject(
                    name = "登录成功",
                    value = "{\"success\":true,\"message\":\"登录成功\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":{\"userId\":\"U123456789\",\"phoneNumber\":\"13800138000\",\"sessionToken\":\"token123\",\"loginTime\":\"2024-01-01T12:00:00\",\"tokenExpiryTime\":\"2024-01-02T12:00:00\",\"userProfile\":{\"nickname\":\"用户昵称\",\"avatar\":\"data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...\",\"bio\":\"个人简介\"}}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "认证方式不匹配",
                        value = "{\"success\":false,\"message\":\"认证方式不匹配，当前请求不是短信登录\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "手机号为空",
                        value = "{\"success\":false,\"message\":\"手机号不能为空\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "验证码为空",
                        value = "{\"success\":false,\"message\":\"短信验证码不能为空\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "未同意协议",
                        value = "{\"success\":false,\"message\":\"必须同意用户协议和隐私政策\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "验证码错误",
                        value = "{\"success\":false,\"message\":\"短信验证码错误或已过期\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "账号被禁用",
                        value = "{\"success\":false,\"message\":\"账号已被禁用，请联系客服\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "账号被锁定",
                        value = "{\"success\":false,\"message\":\"账号已被锁定，请联系客服\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginApiResponse.class),
                examples = @ExampleObject(
                    name = "设备受限",
                    value = "{\"success\":false,\"message\":\"设备或手机号操作受限，请稍后再试\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginApiResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"短信登录过程中发生异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                )
            )
        )
    })
    public ResponseEntity<LoginApiResponse> smsLogin(
        @Parameter(
            description = "短信登录请求参数，包含手机号、短信验证码、设备信息等",
            required = true,
            schema = @Schema(implementation = LoginRequest.class)
        )
        @Valid @RequestBody LoginRequest request
    ) {
        log.info("短信验证码登录请求: phoneNumber={}", request.getPhoneNumber());

        try {
            LoginResponse loginData = authenticationAppService.smsLogin(request);
            log.info("短信验证码登录成功: userId={}", loginData.getUserId());
            return ResponseEntity.ok(responseBuilder.buildLoginSuccessResponse("登录成功", loginData));
        } catch (SmsLoginException e) {
            log.warn("短信登录业务异常: phoneNumber={}, errorCode={}, message={}",
                request.getPhoneNumber(), e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SmsLoginException.SmsLoginErrorCode.AUTH_METHOD_MISMATCH ||
                e.getErrorCode() == SmsLoginException.SmsLoginErrorCode.PHONE_NUMBER_REQUIRED ||
                e.getErrorCode() == SmsLoginException.SmsLoginErrorCode.SMS_CODE_REQUIRED) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildLoginFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == SmsLoginException.SmsLoginErrorCode.SMS_CODE_INVALID ||
                       e.getErrorCode() == SmsLoginException.SmsLoginErrorCode.SMS_CODE_EXPIRED ||
                       e.getErrorCode() == SmsLoginException.SmsLoginErrorCode.SMS_CODE_NOT_FOUND ||
                       e.getErrorCode() == SmsLoginException.SmsLoginErrorCode.ACCOUNT_DISABLED ||
                       e.getErrorCode() == SmsLoginException.SmsLoginErrorCode.ACCOUNT_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildLoginFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == SmsLoginException.SmsLoginErrorCode.DEVICE_LIMITED) {
                // 请求过于频繁，返回429
                return ResponseEntity.status(429)
                        .body(responseBuilder.buildLoginFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildLoginFailureResponse(e.getMessage()));
            }
        } catch (Exception e) {
            log.error("短信登录系统异常: phoneNumber={}, error={}", request.getPhoneNumber(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildLoginFailureResponse("系统异常，请稍后重试"));
        }
    }

    /**
     * 密码登录
     */
//    @PostMapping("/password-login")
//    @Operation(
//        summary = "密码登录",
//        description = "使用手机号和密码进行用户认证，支持自动注册新用户"
//    )
//    @ApiResponses(value = {
//        @ApiResponse(
//            responseCode = "200",
//            description = "登录成功",
//            content = @Content(
//                mediaType = "application/json",
//                schema = @Schema(implementation = LoginResponse.class)
//            )
//        ),
//        @ApiResponse(
//            responseCode = "400",
//            description = "请求参数错误",
//            content = @Content(
//                mediaType = "application/json",
//                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class)
//            )
//        )
//    })
//    public ResponseEntity<?> passwordLogin(
//        @Parameter(
//            description = "密码登录请求参数",
//            required = true,
//            schema = @Schema(implementation = LoginRequest.class)
//        )
//        @Valid @RequestBody LoginRequest request
//    ) {
//        log.info("密码登录请求: phoneNumber={}", request.getPhoneNumber());
//
//        // 验证认证方式
//        if (request.getAuthMethod() != AuthMethod.PASSWORD) {
//            return ResponseEntity.badRequest()
//                    .body(responseBuilder.buildSimpleResponse(false, "认证方式不匹配"));
//        }
//
//        // 验证协议同意状态
//        if (!request.getAgreedToTerms()) {
//            return ResponseEntity.badRequest()
//                    .body(responseBuilder.buildSimpleResponse(false, "必须同意用户协议和隐私政策"));
//        }
//
//        try {
//            LoginResponse response = authenticationAppService.passwordLogin(request);
//            log.info("密码登录成功: userId={}", response.getUserId());
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            log.error("密码登录失败: phoneNumber={}, error={}",
//                    request.getPhoneNumber(), e.getMessage(), e);
//            return handleLoginException(e);
//        }
//    }

    /**
     * 统一登录接口（根据authMethod自动路由）
     */
//    @PostMapping("/login")
//    @Operation(
//        summary = "统一登录接口",
//        description = "根据认证方式自动路由到对应的登录逻辑，支持一键登录、短信登录、密码登录"
//    )
//    @ApiResponses(value = {
//        @ApiResponse(
//            responseCode = "200",
//            description = "登录成功",
//            content = @Content(
//                mediaType = "application/json",
//                schema = @Schema(implementation = LoginResponse.class)
//            )
//        ),
//        @ApiResponse(
//            responseCode = "400",
//            description = "请求参数错误",
//            content = @Content(
//                mediaType = "application/json",
//                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class)
//            )
//        )
//    })
//    public ResponseEntity<?> login(
//        @Parameter(
//            description = "统一登录请求参数",
//            required = true,
//            schema = @Schema(implementation = LoginRequest.class)
//        )
//        @Valid @RequestBody LoginRequest request
//    ) {
//        log.info("登录请求: phoneNumber={}, authMethod={}, device={}",
//                request.getPhoneNumber(),
//                request.getAuthMethod(),
//                deviceInfoConverter.convertToLogString(request.getDeviceInfo()));
//
//        // 验证参数完整性
//        if (!authParameterConverter.validateLoginParameters(request)) {
//            String paramName = authParameterConverter.getAuthMethodParameterName(request.getAuthMethod());
//            return ResponseEntity.badRequest()
//                    .body(responseBuilder.buildSimpleResponse(false, "缺少必要参数: " + paramName));
//        }
//
//        try {
//            LoginResponse response = authenticationAppService.login(request);
//            log.info("登录成功: userId={}", response.getUserId());
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            log.error("登录失败: phoneNumber={}, error={}",
//                    request.getPhoneNumber(), e.getMessage(), e);
//            LoginResponse errorResponse = responseBuilder.buildErrorResponse(e.getMessage());
//            return ResponseEntity.badRequest().body(errorResponse);
//        }
//    }

    /**
     * 退出登录
     */
//    @PostMapping("/logout")
//    @Operation(
//        summary = "退出登录",
//        description = "使用会话令牌退出当前用户登录状态"
//    )
//    @ApiResponses(value = {
//        @ApiResponse(
//            responseCode = "200",
//            description = "退出成功",
//            content = @Content(
//                mediaType = "application/json",
//                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
//                examples = @ExampleObject(
//                    name = "退出成功",
//                    value = "{\"success\":true,\"message\":\"退出登录成功\",\"timestamp\":\"2024-01-01T12:00:00\"}"
//                )
//            )
//        ),
//        @ApiResponse(
//            responseCode = "400",
//            description = "请求参数错误",
//            content = @Content(
//                mediaType = "application/json",
//                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
//                examples = @ExampleObject(
//                    name = "无效授权头",
//                    value = "{\"success\":false,\"message\":\"无效的授权头\",\"timestamp\":\"2024-01-01T12:00:00\"}"
//                )
//            )
//        ),
//        @ApiResponse(
//            responseCode = "500",
//            description = "退出失败",
//            content = @Content(
//                mediaType = "application/json",
//                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
//                examples = @ExampleObject(
//                    name = "退出失败",
//                    value = "{\"success\":false,\"message\":\"退出登录失败\",\"timestamp\":\"2024-01-01T12:00:00\"}"
//                )
//            )
//        )
//    })
//    public ResponseEntity<?> logout(
//        @Parameter(
//            description = "Bearer Token，格式：Bearer {token}",
//            required = true,
//            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
//        )
//        @RequestHeader("Authorization") String authorization
//    ) {
//        String sessionToken = extractTokenFromHeader(authorization);
//
//        if (sessionToken == null) {
//            return ResponseEntity.badRequest()
//                    .body(responseBuilder.buildSimpleResponse(false, "无效的授权头"));
//        }
//
//        boolean result = authenticationAppService.logout(sessionToken);
//
//        if (result) {
//            log.info("退出登录成功: sessionToken={}", sessionToken);
//            return ResponseEntity.ok(responseBuilder.buildSimpleResponse(true, "退出登录成功"));
//        } else {
//            log.warn("退出登录失败: sessionToken={}", sessionToken);
//            return ResponseEntity.internalServerError()
//                    .body(responseBuilder.buildSimpleResponse(false, "退出登录失败"));
//        }
//    }

    /**
     * 验证会话
     */
    @GetMapping("/validate")
    @Operation(
        summary = "验证会话",
        description = "验证当前会话令牌是否有效，用于软件启动时的初始化检查。支持检查令牌格式、过期状态、用户状态等。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "会话有效",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "会话有效",
                    value = "{\"success\":true,\"message\":\"会话验证成功\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "缺少授权头",
                        value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "授权头格式错误",
                        value = "{\"success\":false,\"message\":\"Authorization头格式无效\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "令牌为空",
                        value = "{\"success\":false,\"message\":\"会话令牌不能为空\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "令牌不存在",
                        value = "{\"success\":false,\"message\":\"会话令牌不存在或已过期\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户不存在",
                        value = "{\"success\":false,\"message\":\"用户不存在\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被禁用",
                        value = "{\"success\":false,\"message\":\"用户已被禁用\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被锁定",
                        value = "{\"success\":false,\"message\":\"用户已被锁定\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"会话验证过程中发生异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        )
    })
    public ResponseEntity<?> validateSession(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}。用于验证用户会话的有效性。",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildSimpleResponse(false, "缺少Authorization头"));
            }

        String sessionToken = extractTokenFromHeader(authorization);

        if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
            return ResponseEntity.badRequest()
                        .body(responseBuilder.buildSimpleResponse(false, "Authorization头格式无效"));
        }

        String userId = authenticationAppService.validateSession(sessionToken);
            log.debug("会话验证成功: sessionToken={}, userId={}", sessionToken, userId);
            return ResponseEntity.ok(responseBuilder.buildSimpleResponse(true, "会话验证成功"));

        } catch (SessionValidationException e) {
            log.warn("会话验证业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildSimpleResponse(false, e.getMessage()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildSimpleResponse(false, e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildSimpleResponse(false, e.getMessage()));
            }
        } catch (Exception e) {
            log.error("会话验证系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildSimpleResponse(false, "系统异常，请稍后重试"));
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user-info")
    @Operation(
        summary = "获取用户信息",
        description = "根据会话令牌获取当前用户的基本信息，包括用户ID、昵称、个人简介和头像(base64编码)。需要有效的会话令牌。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "获取用户信息成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class),
                examples = @ExampleObject(
                    name = "获取成功",
                    value = "{\"success\":true,\"message\":\"获取用户信息成功\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":{\"userId\":\"U123456789\",\"nickname\":\"用户昵称\",\"bio\":\"个人简介\",\"avatar\":\"data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...\"}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class),
                examples = {
                    @ExampleObject(
                        name = "缺少授权头",
                        value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "授权头格式错误",
                        value = "{\"success\":false,\"message\":\"Authorization头格式无效\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "令牌为空",
                        value = "{\"success\":false,\"message\":\"会话令牌不能为空\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class),
                examples = {
                    @ExampleObject(
                        name = "令牌不存在",
                        value = "{\"success\":false,\"message\":\"会话令牌不存在或已过期\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "用户不存在",
                        value = "{\"success\":false,\"message\":\"用户不存在\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "用户被禁用",
                        value = "{\"success\":false,\"message\":\"用户已被禁用\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "用户被锁定",
                        value = "{\"success\":false,\"message\":\"用户已被锁定\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"获取用户信息过程中发生异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                )
            )
        )
    })
    public ResponseEntity<UserInfoResponse> getUserInfo(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}。用于验证用户身份。",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildUserInfoFailureResponse("缺少Authorization头"));
            }

            String sessionToken = extractTokenFromHeader(authorization);

            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildUserInfoFailureResponse("Authorization头格式无效"));
            }

            UserInfoResponse.UserInfo userInfo = authenticationAppService.getUserInfo(sessionToken);
            log.debug("获取用户信息成功: userId={}", userInfo.getUserId());
            return ResponseEntity.ok(responseBuilder.buildUserInfoSuccessResponse("获取用户信息成功", userInfo));

        } catch (SessionValidationException e) {
            log.warn("获取用户信息业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildUserInfoFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildUserInfoFailureResponse(e.getMessage()));
        } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildUserInfoFailureResponse(e.getMessage()));
            }
        } catch (Exception e) {
            log.error("获取用户信息系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildUserInfoFailureResponse("系统异常，请稍后重试"));
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/user-info")
    @Operation(
        summary = "更新用户信息",
        description = "更新当前用户的基本信息，包括昵称、个人简介和头像(base64编码)。支持部分字段更新，只更新提供的字段。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "更新用户信息成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class),
                examples = @ExampleObject(
                    name = "更新成功",
                    value = "{\"success\":true,\"message\":\"更新用户信息成功\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":{\"userId\":\"U123456789\",\"nickname\":\"新昵称\",\"bio\":\"新的个人简介\",\"avatar\":\"data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...\"}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class),
                examples = {
                    @ExampleObject(
                        name = "缺少授权头",
                        value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "授权头格式错误",
                        value = "{\"success\":false,\"message\":\"Authorization头格式无效\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "令牌为空",
                        value = "{\"success\":false,\"message\":\"会话令牌不能为空\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "没有更新字段",
                        value = "{\"success\":false,\"message\":\"没有提供任何需要更新的字段\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "字段长度超限",
                        value = "{\"success\":false,\"message\":\"昵称长度不能超过10个字符\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "头像数据过大",
                        value = "{\"success\":false,\"message\":\"头像base64数据过大，请选择较小的图片\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class),
                examples = {
                    @ExampleObject(
                        name = "令牌不存在",
                        value = "{\"success\":false,\"message\":\"会话令牌不存在或已过期\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "用户不存在",
                        value = "{\"success\":false,\"message\":\"用户不存在\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "用户被禁用",
                        value = "{\"success\":false,\"message\":\"用户已被禁用\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "用户被锁定",
                        value = "{\"success\":false,\"message\":\"用户已被锁定\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"更新用户信息过程中发生异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                )
            )
        )
    })
    public ResponseEntity<UserInfoResponse> updateUserInfo(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}。用于验证用户身份。",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Parameter(
            description = "更新用户信息请求参数，所有字段都是可选的，支持部分更新",
            required = true,
            schema = @Schema(implementation = UpdateUserInfoRequest.class)
        )
        @Valid @RequestBody UpdateUserInfoRequest request
    ) {
        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildUserInfoFailureResponse("缺少Authorization头"));
            }

            String sessionToken = extractTokenFromHeader(authorization);

            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildUserInfoFailureResponse("Authorization头格式无效"));
            }

            UserInfoResponse.UserInfo userInfo = authenticationAppService.updateUserInfo(sessionToken, request);
            log.info("更新用户信息成功: userId={}", userInfo.getUserId());
            return ResponseEntity.ok(responseBuilder.buildUserInfoSuccessResponse("更新用户信息成功", userInfo));

        } catch (SessionValidationException e) {
            log.warn("更新用户信息业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildUserInfoFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
            return ResponseEntity.status(401)
                        .body(responseBuilder.buildUserInfoFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildUserInfoFailureResponse(e.getMessage()));
            }
        } catch (Exception e) {
            log.error("更新用户信息系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildUserInfoFailureResponse("系统异常，请稍后重试"));
        }
    }

    /**
     * 处理登录异常
     */
    private ResponseEntity<?> handleLoginException(Exception e) {
        String errorMessage = "登录失败，请稍后重试";

        if (e instanceof SecurityException) {
            errorMessage = e.getMessage();
        } else if (e instanceof IllegalArgumentException) {
            errorMessage = e.getMessage();
        } else if (e instanceof IllegalStateException) {
            errorMessage = e.getMessage();
        }

        return ResponseEntity.badRequest()
                .body(responseBuilder.buildErrorResponse(errorMessage));
    }

    /**
     * 从Authorization头中提取令牌
     */
    private String extractTokenFromHeader(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
