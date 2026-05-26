package com.hospital.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类 - 提供获取当前登录用户信息的方法
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            // JwtAuthenticationFilter将userId作为principal存入
            if (principal instanceof Long) {
                return (Long) principal;
            }
            // 兼容旧逻辑：如果principal是用户名String，尝试从details获取
            if (authentication.getDetails() instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> details = (java.util.Map<String, Object>) authentication.getDetails();
                Object userId = details.get("userId");
                if (userId instanceof Long) {
                    return (Long) userId;
                }
                if (userId instanceof Integer) {
                    return ((Integer) userId).longValue();
                }
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户角色
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> details = (java.util.Map<String, Object>) authentication.getDetails();
            Object role = details.get("role");
            if (role instanceof String) {
                return (String) role;
            }
        }
        return null;
    }
}
