package com.rtalky.interfaces.rest;

import com.rtalky.application.dto.LoginRequest;
import com.rtalky.application.dto.LoginResponse;
import com.rtalky.application.dto.LoginApiResponse;
import com.rtalky.application.dto.SmsSendRequest;
import com.rtalky.application.dto.SmsSendResponse;
import com.rtalky.application.dto.UserInfoResponse;
import com.rtalky.application.dto.UpdateUserInfoRequest;
import com.rtalky.application.dto.PasswordSmsSendRequest;
import com.rtalky.application.dto.PasswordSetRequest;
import com.rtalky.application.dto.PasswordChangeRequest;
import com.rtalky.application.dto.PasswordResponse;
import com.rtalky.application.dto.AccountDeletionSmsRequest;
import com.rtalky.application.dto.AccountDeletionRequest;
import com.rtalky.application.dto.AccountDeletionResponse;
import com.rtalky.application.exception.SmsSendException;
import com.rtalky.domain.model.AuthMethod;
import com.rtalky.application.exception.SmsLoginException;
import com.rtalky.application.exception.PasswordLoginException;
import com.rtalky.application.exception.SessionValidationException;
import com.rtalky.application.service.AuthenticationAppService;
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
    @PostMapping("/password-login")
    @Operation(
        summary = "密码登录",
        description = "使用手机号和密码进行用户认证。需要用户已设置密码且同意用户协议。"
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
                        value = "{\"success\":false,\"message\":\"认证方式不匹配，当前请求不是密码登录\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "手机号为空",
                        value = "{\"success\":false,\"message\":\"手机号不能为空\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "密码为空",
                        value = "{\"success\":false,\"message\":\"密码不能为空\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
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
                        name = "密码错误",
                        value = "{\"success\":false,\"message\":\"密码错误\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "用户不存在",
                        value = "{\"success\":false,\"message\":\"用户不存在\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "账号被禁用",
                        value = "{\"success\":false,\"message\":\"用户账号不可用\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                    ),
                    @ExampleObject(
                        name = "未设置密码",
                        value = "{\"success\":false,\"message\":\"请先设置密码\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
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
                    value = "{\"success\":false,\"message\":\"操作受限，请稍后再试\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
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
                    value = "{\"success\":false,\"message\":\"系统异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\",\"data\":null}"
                )
            )
        )
    })
    public ResponseEntity<LoginApiResponse> passwordLogin(
        @Parameter(
            description = "密码登录请求参数，包含手机号、密码、设备信息和协议同意状态",
            required = true,
            schema = @Schema(implementation = LoginRequest.class)
        )
        @Valid @RequestBody LoginRequest request
    ) {
        log.info("密码登录请求: phoneNumber={}", request.getPhoneNumber());

        try {
            LoginResponse loginData = authenticationAppService.passwordLogin(request);
            log.info("密码登录成功: userId={}, phoneNumber={}", loginData.getUserId(), request.getPhoneNumber());
            return ResponseEntity.ok(responseBuilder.buildLoginSuccessResponse("登录成功", loginData));

        } catch (PasswordLoginException e) {
            log.warn("密码登录业务异常: phoneNumber={}, errorCode={}, message={}",
                request.getPhoneNumber(), e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == PasswordLoginException.PasswordLoginErrorCode.AUTH_METHOD_MISMATCH ||
                e.getErrorCode() == PasswordLoginException.PasswordLoginErrorCode.PHONE_NUMBER_REQUIRED ||
                e.getErrorCode() == PasswordLoginException.PasswordLoginErrorCode.PASSWORD_REQUIRED ||
                e.getErrorCode() == PasswordLoginException.PasswordLoginErrorCode.AGREEMENT_NOT_ACCEPTED) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildLoginFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == PasswordLoginException.PasswordLoginErrorCode.PASSWORD_INVALID ||
                       e.getErrorCode() == PasswordLoginException.PasswordLoginErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == PasswordLoginException.PasswordLoginErrorCode.ACCOUNT_DISABLED ||
                       e.getErrorCode() == PasswordLoginException.PasswordLoginErrorCode.ACCOUNT_LOCKED ||
                       e.getErrorCode() == PasswordLoginException.PasswordLoginErrorCode.PASSWORD_NOT_SET) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildLoginFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == PasswordLoginException.PasswordLoginErrorCode.DEVICE_LIMITED) {
                // 请求过于频繁，返回429
                return ResponseEntity.status(429)
                        .body(responseBuilder.buildLoginFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildLoginFailureResponse(e.getMessage()));
            }
        } catch (Exception e) {
            log.error("密码登录系统异常: phoneNumber={}, error={}",
                request.getPhoneNumber(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildLoginFailureResponse("系统异常，请稍后重试"));
        }
    }

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
     * 从Authorization头中提取令牌
     */
    private String extractTokenFromHeader(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    /**
     * 发送密码设置短信验证码
     */
    @PostMapping("/password/sms-code")
    @Operation(
        summary = "发送密码设置短信验证码",
        description = "向指定手机号发送短信验证码，用于密码设置验证。需要用户已登录，支持限流保护，防止恶意请求。"
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
                    name = "缺少Authorization头",
                    value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\",\"codeDigits\":null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SmsSendResponse.class),
                examples = @ExampleObject(
                    name = "会话无效",
                    value = "{\"success\":false,\"message\":\"会话已过期，请重新登录\",\"timestamp\":\"2024-01-01T12:00:00\",\"codeDigits\":null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SmsSendResponse.class),
                examples = @ExampleObject(
                    name = "设备限流",
                    value = "{\"success\":false,\"message\":\"操作受限，请稍后再试\",\"timestamp\":\"2024-01-01T12:00:00\",\"codeDigits\":null}"
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
    public ResponseEntity<SmsSendResponse> sendPasswordSmsCode(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}。用于验证用户身份。",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Parameter(
            description = "密码设置短信发送请求参数，包含手机号、设备信息和新密码",
            required = true,
            schema = @Schema(implementation = PasswordSmsSendRequest.class)
        )
        @Valid @RequestBody PasswordSmsSendRequest request
    ) {
        log.info("请求发送密码设置短信验证码: phoneNumber={}", request.getPhoneNumber());

        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildSmsSendFailureResponse("缺少Authorization头"));
            }

            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
        return ResponseEntity.badRequest()
                        .body(responseBuilder.buildSmsSendFailureResponse("Authorization头格式无效"));
            }

            PasswordResponse response = authenticationAppService.sendPasswordSmsCode(sessionToken, request);

            if (response.isSuccess()) {
                log.info("密码设置短信验证码发送成功: phoneNumber={}, codeDigits={}", request.getPhoneNumber(), smsCodeDigits);
                return ResponseEntity.ok(responseBuilder.buildSmsSendSuccessResponse("短信验证码发送成功", smsCodeDigits));
            } else {
                log.warn("密码设置短信验证码发送失败: phoneNumber={}, message={}",
                    request.getPhoneNumber(), response.getMessage());
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildSmsSendFailureResponse(response.getMessage()));
            }
        } catch (SessionValidationException e) {
            log.warn("密码设置短信发送会话验证异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildSmsSendFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildSmsSendFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildSmsSendFailureResponse(e.getMessage()));
            }
        } catch (SmsSendException e) {
            log.warn("密码设置短信发送业务异常: phoneNumber={}, errorCode={}, message={}",
                request.getPhoneNumber(), e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SmsSendException.SmsSendErrorCode.INVALID_PHONE_NUMBER ||
                e.getErrorCode() == SmsSendException.SmsSendErrorCode.PHONE_NUMBER_REQUIRED) {
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
            log.error("发送密码设置短信验证码系统异常: phoneNumber={}, error={}",
                request.getPhoneNumber(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildSmsSendFailureResponse("系统异常，请稍后重试"));
        }
    }

    /**
     * 设置密码（首次补设/重置密码）
     */
    @PostMapping("/password/set")
    @Operation(
        summary = "设置密码",
        description = "设置用户密码，支持首次补设和重置密码。需要先调用发送密码设置短信验证码接口，然后使用验证码完成密码设置。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "密码设置成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "设置成功",
                    value = "{\"success\":true,\"message\":\"密码设置成功\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "缺少Authorization头",
                    value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "会话无效",
                    value = "{\"success\":false,\"message\":\"会话已过期，请重新登录\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "业务逻辑错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "验证码无效",
                    value = "{\"success\":false,\"message\":\"短信验证码无效或已过期\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "410",
            description = "会话已过期",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "密码设置会话过期",
                    value = "{\"success\":false,\"message\":\"密码设置会话已过期，请重新开始\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "操作受限",
                    value = "{\"success\":false,\"message\":\"操作受限，请稍后再试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "系统异常",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"系统异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        )
    })
    public ResponseEntity<PasswordResponse> setPassword(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}。用于验证用户身份。",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Parameter(
            description = "密码设置请求参数，包含手机号和短信验证码",
            required = true,
            schema = @Schema(implementation = PasswordSetRequest.class)
        )
        @Valid @RequestBody PasswordSetRequest request
    ) {
        log.info("请求设置密码: phoneNumber={}", request.getPhoneNumber());

        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(new PasswordResponse(false, "缺少Authorization头", java.time.LocalDateTime.now()));
            }

            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(new PasswordResponse(false, "Authorization头格式无效", java.time.LocalDateTime.now()));
            }

            PasswordResponse response = authenticationAppService.setPassword(sessionToken, request);

            if (response.isSuccess()) {
                log.info("密码设置成功: phoneNumber={}", request.getPhoneNumber());
                return ResponseEntity.ok(response);
            } else {
                log.warn("密码设置失败: phoneNumber={}, message={}",
                    request.getPhoneNumber(), response.getMessage());

                // 根据错误类型返回不同的HTTP状态码
                if (response.getMessage().contains("会话") || response.getMessage().contains("登录")) {
                    return ResponseEntity.status(401).body(response);
                } else if (response.getMessage().contains("验证码")) {
                    return ResponseEntity.status(422).body(response);
                } else if (response.getMessage().contains("会话已过期") || response.getMessage().contains("重新开始")) {
                    return ResponseEntity.status(410).body(response);
                } else {
                    return ResponseEntity.internalServerError().body(response);
                }
            }
        } catch (SessionValidationException e) {
            log.warn("设置密码会话验证异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(new PasswordResponse(false, e.getMessage(), java.time.LocalDateTime.now()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(new PasswordResponse(false, e.getMessage(), java.time.LocalDateTime.now()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(new PasswordResponse(false, e.getMessage(), java.time.LocalDateTime.now()));
            }
        } catch (SmsSendException e) {
            log.warn("设置密码短信验证异常: phoneNumber={}, errorCode={}, message={}",
                request.getPhoneNumber(), e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SmsSendException.SmsSendErrorCode.INVALID_PHONE_NUMBER ||
                e.getErrorCode() == SmsSendException.SmsSendErrorCode.PHONE_NUMBER_REQUIRED) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(new PasswordResponse(false, e.getMessage(), java.time.LocalDateTime.now()));
            } else if (e.getErrorCode() == SmsSendException.SmsSendErrorCode.DEVICE_LIMITED ||
                       e.getErrorCode() == SmsSendException.SmsSendErrorCode.SMS_ALREADY_SENT) {
                // 请求过于频繁，返回429
                return ResponseEntity.status(429)
                        .body(new PasswordResponse(false, e.getMessage(), java.time.LocalDateTime.now()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(new PasswordResponse(false, e.getMessage(), java.time.LocalDateTime.now()));
            }
        } catch (Exception e) {
            log.error("设置密码系统异常: phoneNumber={}, error={}",
                request.getPhoneNumber(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new PasswordResponse(false, "系统异常，请稍后重试", java.time.LocalDateTime.now()));
        }
    }

    /**
     * 修改密码（知道旧密码）
     */
    @PostMapping("/password/change")
    @Operation(
        summary = "修改密码",
        description = "修改用户密码，需要提供旧密码进行验证。适用于用户记得旧密码的情况。需要用户已登录且已设置密码。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "密码修改成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "修改成功",
                    value = "{\"success\":true,\"message\":\"密码修改成功\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "缺少Authorization头",
                    value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "会话无效",
                    value = "{\"success\":false,\"message\":\"会话已过期，请重新登录\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "业务逻辑错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "旧密码错误",
                    value = "{\"success\":false,\"message\":\"旧密码不正确\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "410",
            description = "前置条件不满足",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "未设置密码",
                    value = "{\"success\":false,\"message\":\"请先设置密码\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "操作受限",
                    value = "{\"success\":false,\"message\":\"操作受限，请稍后再试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "系统异常",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"系统异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        )
    })
    public ResponseEntity<PasswordResponse> changePassword(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}。用于验证用户身份。",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Parameter(
            description = "密码修改请求参数",
            required = true,
            schema = @Schema(implementation = PasswordChangeRequest.class)
        )
        @Valid @RequestBody PasswordChangeRequest request
    ) {
        log.info("请求修改密码");

        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(new PasswordResponse(false, "缺少Authorization头", java.time.LocalDateTime.now()));
            }

            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(new PasswordResponse(false, "Authorization头格式无效", java.time.LocalDateTime.now()));
            }

            PasswordResponse response = authenticationAppService.changePassword(sessionToken, request);

            if (response.isSuccess()) {
                log.info("密码修改成功");
                return ResponseEntity.ok(response);
            } else {
                log.warn("密码修改失败: message={}", response.getMessage());

                // 根据错误类型返回不同的HTTP状态码
                if (response.getMessage().contains("会话") || response.getMessage().contains("登录")) {
                    return ResponseEntity.status(401).body(response);
                } else if (response.getMessage().contains("旧密码") || response.getMessage().contains("密码强度")) {
                    return ResponseEntity.status(422).body(response);
                } else if (response.getMessage().contains("请先设置密码")) {
                    return ResponseEntity.status(410).body(response);
                } else {
                    return ResponseEntity.internalServerError().body(response);
                }
            }
        } catch (SessionValidationException e) {
            log.warn("修改密码会话验证异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(new PasswordResponse(false, e.getMessage(), java.time.LocalDateTime.now()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(new PasswordResponse(false, e.getMessage(), java.time.LocalDateTime.now()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(new PasswordResponse(false, e.getMessage(), java.time.LocalDateTime.now()));
            }
        } catch (SmsSendException e) {
            log.warn("修改密码短信验证异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SmsSendException.SmsSendErrorCode.INVALID_PHONE_NUMBER ||
                e.getErrorCode() == SmsSendException.SmsSendErrorCode.PHONE_NUMBER_REQUIRED) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(new PasswordResponse(false, e.getMessage(), java.time.LocalDateTime.now()));
            } else if (e.getErrorCode() == SmsSendException.SmsSendErrorCode.DEVICE_LIMITED ||
                       e.getErrorCode() == SmsSendException.SmsSendErrorCode.SMS_ALREADY_SENT) {
                // 请求过于频繁，返回429
                return ResponseEntity.status(429)
                        .body(new PasswordResponse(false, e.getMessage(), java.time.LocalDateTime.now()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(new PasswordResponse(false, e.getMessage(), java.time.LocalDateTime.now()));
            }
        } catch (Exception e) {
            log.error("修改密码系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new PasswordResponse(false, "系统异常，请稍后重试", java.time.LocalDateTime.now()));
        }
    }

    /**
     * 发送账号注销短信验证码
     */
    @PostMapping("/account/deletion/sms-code")
    @Operation(
        summary = "发送账号注销短信验证码",
        description = "向用户手机号发送短信验证码，用于账号注销验证。需要用户已登录且已同意注销说明。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "验证码发送成功",
            content = @Content(schema = @Schema(implementation = SmsSendResponse.class),
                examples = @ExampleObject(value = "{\"success\":true,\"message\":\"验证码发送成功\",\"codeDigits\":6,\"timestamp\":\"2024-01-01T12:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "请求参数错误",
            content = @Content(schema = @Schema(implementation = SmsSendResponse.class),
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"必须同意注销说明\",\"codeDigits\":null,\"timestamp\":\"2024-01-01T12:00:00\"}"))),
        @ApiResponse(responseCode = "401", description = "未授权",
            content = @Content(schema = @Schema(implementation = SmsSendResponse.class),
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"会话已过期，请重新登录\",\"codeDigits\":null,\"timestamp\":\"2024-01-01T12:00:00\"}"))),
        @ApiResponse(responseCode = "429", description = "请求过于频繁",
            content = @Content(schema = @Schema(implementation = SmsSendResponse.class),
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"操作受限，请稍后再试\",\"codeDigits\":null,\"timestamp\":\"2024-01-01T12:00:00\"}"))),
        @ApiResponse(responseCode = "500", description = "系统异常",
            content = @Content(schema = @Schema(implementation = SmsSendResponse.class),
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"系统异常，请稍后重试\",\"codeDigits\":null,\"timestamp\":\"2024-01-01T12:00:00\"}")))
    })
    public ResponseEntity<SmsSendResponse> sendDeletionSmsCode(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody AccountDeletionSmsRequest request
    ) {
        try {
            log.info("请求发送账号注销短信验证码");

            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(new SmsSendResponse(false, "缺少Authorization头", java.time.LocalDateTime.now(), null));
            }

            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(new SmsSendResponse(false, "Authorization头格式无效", java.time.LocalDateTime.now(), null));
            }

            // 调用应用服务
            SmsSendResponse response = authenticationAppService.sendDeletionSmsCode(sessionToken, request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                // 根据错误消息返回不同的HTTP状态码
                if (response.getMessage().contains("必须同意") || response.getMessage().contains("用户不存在")) {
                    return ResponseEntity.badRequest().body(response);
                } else if (response.getMessage().contains("会话") || response.getMessage().contains("登录")) {
                    return ResponseEntity.status(401).body(response);
                } else if (response.getMessage().contains("操作受限")) {
                    return ResponseEntity.status(429).body(response);
                } else {
                    return ResponseEntity.internalServerError().body(response);
                }
            }

        } catch (SessionValidationException e) {
            log.warn("发送账号注销短信验证码会话验证异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(new SmsSendResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(new SmsSendResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(new SmsSendResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
            }
        } catch (IllegalArgumentException e) {
            log.warn("发送账号注销短信验证码参数异常: message={}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new SmsSendResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
        } catch (SecurityException e) {
            log.warn("发送账号注销短信验证码安全异常: message={}", e.getMessage());
            return ResponseEntity.status(429)
                    .body(new SmsSendResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
        } catch (SmsSendException e) {
            log.warn("发送账号注销短信验证码短信发送异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SmsSendException.SmsSendErrorCode.INVALID_PHONE_NUMBER ||
                e.getErrorCode() == SmsSendException.SmsSendErrorCode.PHONE_NUMBER_REQUIRED) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(new SmsSendResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
            } else if (e.getErrorCode() == SmsSendException.SmsSendErrorCode.DEVICE_LIMITED ||
                       e.getErrorCode() == SmsSendException.SmsSendErrorCode.SMS_ALREADY_SENT) {
                // 请求过于频繁，返回429
                return ResponseEntity.status(429)
                        .body(new SmsSendResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(new SmsSendResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
            }
        } catch (Exception e) {
            log.error("发送账号注销短信验证码系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new SmsSendResponse(false, "系统异常，请稍后重试", java.time.LocalDateTime.now(), null));
        }
    }

    /**
     * 提交账号注销申请
     */
    @PostMapping("/account/deletion/submit")
    @Operation(
        summary = "提交账号注销申请",
        description = "提交账号注销申请，需要提供短信验证码进行验证。注销后账号将被软删除。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "注销成功",
            content = @Content(schema = @Schema(implementation = AccountDeletionResponse.class),
                examples = @ExampleObject(value = "{\"success\":true,\"message\":\"账号注销成功\",\"timestamp\":\"2024-01-01T12:00:00\",\"deletionId\":\"deletion-123456\"}"))),
        @ApiResponse(responseCode = "400", description = "请求参数错误",
            content = @Content(schema = @Schema(implementation = AccountDeletionResponse.class),
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"用户不存在\",\"timestamp\":\"2024-01-01T12:00:00\",\"deletionId\":null}"))),
        @ApiResponse(responseCode = "401", description = "认证失败",
            content = @Content(schema = @Schema(implementation = AccountDeletionResponse.class),
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"会话已过期，请重新登录\",\"timestamp\":\"2024-01-01T12:00:00\",\"deletionId\":null}"))),
        @ApiResponse(responseCode = "500", description = "系统异常",
            content = @Content(schema = @Schema(implementation = AccountDeletionResponse.class),
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"系统异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\",\"deletionId\":null}")))
    })
    public ResponseEntity<AccountDeletionResponse> submitDeletion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody AccountDeletionRequest request) {

        try {
            log.info("请求提交账号注销申请");

            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(new AccountDeletionResponse(false, "缺少Authorization头", java.time.LocalDateTime.now(), null));
            }

            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(new AccountDeletionResponse(false, "Authorization头格式无效", java.time.LocalDateTime.now(), null));
            }

            // 调用应用服务
            AccountDeletionResponse response = authenticationAppService.submitDeletion(sessionToken, request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                // 根据错误消息返回不同的HTTP状态码
                if (response.getMessage().contains("用户不存在")) {
                    return ResponseEntity.badRequest().body(response);
                } else if (response.getMessage().contains("会话") || response.getMessage().contains("登录")) {
                    return ResponseEntity.status(401).body(response);
                } else {
                    return ResponseEntity.internalServerError().body(response);
                }
            }

        } catch (SessionValidationException e) {
            log.warn("提交账号注销申请会话验证异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(new AccountDeletionResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(new AccountDeletionResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(new AccountDeletionResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
            }
        } catch (IllegalArgumentException e) {
            log.warn("提交账号注销申请参数异常: message={}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AccountDeletionResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
        } catch (SecurityException e) {
            log.warn("提交账号注销申请安全异常: message={}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(new AccountDeletionResponse(false, e.getMessage(), java.time.LocalDateTime.now(), null));
        } catch (Exception e) {
            log.error("提交账号注销申请系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new AccountDeletionResponse(false, "系统异常，请稍后重试", java.time.LocalDateTime.now(), null));
        }
    }
}
