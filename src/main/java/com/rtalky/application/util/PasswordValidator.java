package com.rtalky.application.util;

import java.util.regex.Pattern;

/**
 * 密码强度校验工具类
 */
public class PasswordValidator {

    // 允许的字符集：A-Za-z0-9 和特殊字符 ~!@#$%^&*()_-+=[]{}|;:,.<>/?
    private static final String ALLOWED_CHARS = "A-Za-z0-9~!@#$%^&*()_\\-+=\\[\\]{}|;:,.<>\\/?";
    private static final Pattern ALLOWED_CHARS_PATTERN = Pattern.compile("^[" + ALLOWED_CHARS + "]+$");

    // 至少包含两类字符的正则表达式
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[~!@#$%^&*()_\\-+=\\[\\]{}|;:,.<>\\/?]");

    // 连续相同字符的正则表达式
    private static final Pattern REPEATED_CHARS = Pattern.compile("(.)\\1{2,}");

    // 连续递增或递减序列的正则表达式
    private static final Pattern SEQUENTIAL_CHARS = Pattern.compile("(?:abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz|321|432|543|654|765|876|987)");

    /**
     * 验证密码强度
     *
     * @param password 密码
     * @param phoneNumber 手机号（用于检查是否包含）
     * @param userId 用户ID（用于检查是否包含）
     * @return 验证结果
     */
    public static PasswordValidationResult validate(String password, String phoneNumber, String userId) {
        if (password == null || password.trim().isEmpty()) {
            return new PasswordValidationResult(false, "密码不能为空");
        }

        // 1. 长度检查
        if (password.length() < 8 || password.length() > 32) {
            return new PasswordValidationResult(false, "密码长度必须在8-32位之间");
        }

        // 2. 字符集检查
        if (!ALLOWED_CHARS_PATTERN.matcher(password).matches()) {
            return new PasswordValidationResult(false, "密码只能包含字母、数字和特殊字符 ~!@#$%^&*()_-+=[]{}|;:,.<>/?");
        }

        // 3. 至少包含两类字符检查
        int charTypeCount = 0;
        if (HAS_UPPERCASE.matcher(password).find()) charTypeCount++;
        if (HAS_LOWERCASE.matcher(password).find()) charTypeCount++;
        if (HAS_DIGIT.matcher(password).find()) charTypeCount++;
        if (HAS_SPECIAL.matcher(password).find()) charTypeCount++;

        if (charTypeCount < 2) {
            return new PasswordValidationResult(false, "密码必须至少包含大写字母、小写字母、数字、特殊字符中的任意两类");
        }

        // 4. 连续相同字符检查
        if (REPEATED_CHARS.matcher(password).find()) {
            return new PasswordValidationResult(false, "密码不能包含3个及以上完全相同的连续字符");
        }

        // 5. 连续递增或递减序列检查
        if (SEQUENTIAL_CHARS.matcher(password.toLowerCase()).find()) {
            return new PasswordValidationResult(false, "密码不能包含3位及以上的连续递增或递减序列");
        }

        // 6. 不包含手机号检查
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
            if (password.contains(cleanPhone) || 
                (cleanPhone.length() >= 6 && password.contains(cleanPhone.substring(cleanPhone.length() - 6)))) {
                return new PasswordValidationResult(false, "密码不能包含手机号");
            }
        }

        // 7. 不包含用户ID检查
        if (userId != null && !userId.trim().isEmpty() && password.contains(userId)) {
            return new PasswordValidationResult(false, "密码不能包含用户ID");
        }

        return new PasswordValidationResult(true, "密码强度符合要求");
    }

    /**
     * 密码验证结果
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final String message;

        public PasswordValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
