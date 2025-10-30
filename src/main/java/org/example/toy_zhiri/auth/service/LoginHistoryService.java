package org.example.toy_zhiri.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.auth.dto.LoginHistoryResponse;
import org.example.toy_zhiri.auth.entity.LoginHistory;
import org.example.toy_zhiri.auth.repository.LoginHistoryRepository;
import org.example.toy_zhiri.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Сервис для логирования и просмотра истории входов.
 */
@Service
@RequiredArgsConstructor
public class LoginHistoryService {
    private final LoginHistoryRepository loginHistoryRepository;

    /**
     * Логирует попытку входа.
     *
     * @param user пользователь
     * @param success успешность попытки
     * @param failureReason причина неудачи (если не успешно)
     * @param request HTTP запрос для получения IP и User-Agent
     */
    @Transactional
    public void logLogin(User user, boolean success, String failureReason, HttpServletRequest request) {
        LoginHistory history = LoginHistory.builder()
                .user(user)
                .email(user.getEmail())
                .ipAddress(getClientIp(request))
                .userAgent(getUserAgent(request))
                .loginType(LoginHistory.LoginType.LOGIN)
                .success(success)
                .failureReason(failureReason)
                .build();

        loginHistoryRepository.save(history);
    }

    /**
     * Логирует выход пользователя.
     *
     * @param user пользователь
     * @param request HTTP запрос
     */
    @Transactional
    public void logLogout(User user, HttpServletRequest request) {
        LoginHistory history = LoginHistory.builder()
                .user(user)
                .email(user.getEmail())
                .ipAddress(getClientIp(request))
                .userAgent(getUserAgent(request))
                .loginType(LoginHistory.LoginType.LOGOUT)
                .success(true)
                .build();

        loginHistoryRepository.save(history);
    }

    /**
     * Получает историю входов пользователя.
     *
     * @param userId идентификатор пользователя
     * @param pageable параметры пагинации
     * @return страница с историей входов
     */
    public Page<LoginHistoryResponse> getUserLoginHistory(UUID userId, Pageable pageable) {
        Page<LoginHistory> history = loginHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return history.map(this::mapToResponse);
    }

    /**
     * Подсчитывает общее количество успешных входов пользователя.
     *
     * @param userId идентификатор пользователя
     * @return long количество входов
     */
    public long getTotalLogins(UUID userId) {
        return loginHistoryRepository.countByUserIdAndSuccess(userId, true);
    }

    /**
     * Извлекает IP адрес клиента из запроса.
     * Учитывает proxy и load balancer.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Если несколько IP (через запятую), берем первый
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Извлекает User-Agent из запроса.
     */
    private String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 500) {
            userAgent = userAgent.substring(0, 500);
        }
        return userAgent;
    }

    /**
     * Маппинг LoginHistory -> LoginHistoryResponse.
     */
    private LoginHistoryResponse mapToResponse(LoginHistory history) {
        return LoginHistoryResponse.builder()
                .id(history.getId())
                .email(history.getEmail())
                .ipAddress(history.getIpAddress())
                .userAgent(history.getUserAgent())
                .loginType(history.getLoginType().name())
                .success(history.getSuccess())
                .failureReason(history.getFailureReason())
                .createdAt(history.getCreatedAt())
                .build();
    }
}