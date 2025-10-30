package org.example.toy_zhiri.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.example.toy_zhiri.auth.service.TokenBlacklistService;

import java.io.IOException;
import java.util.UUID;

/**
 * Фильтр для аутентификации запросов на основе JWT токенов.
 */
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Обрабатывает каждый HTTP запрос, извлекает и валидирует JWT токен.
     *
     * @param request HTTP запрос
     * @param response HTTP ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException при ошибке обработки запроса
     * @throws IOException при ошибке ввода-вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String token = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        if (token != null && tokenProvider.validateToken(token)) {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                // Токен отозван - не аутентифицируем
                filterChain.doFilter(request, response);
                return;
            }

            UUID userId = tokenProvider.getUserId(token);

            UserDetails userDetails = userDetailsService.loadUserById(userId);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}