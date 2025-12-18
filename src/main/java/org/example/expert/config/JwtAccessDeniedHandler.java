package org.example.expert.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security 권한 부족 시 403 Forbidden 응답을 반환하는 핸들러
 * 인증은 되었지만 해당 리소스에 접근할 권한이 없는 경우 호출됨 (예: ADMIN 권한 필요한데 USER인 경우)
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        log.error("Access denied: {}", accessDeniedException.getMessage());

        // 403 Forbidden 응답 반환
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"error\": \"관리자 권한이 없습니다.\"}");
    }
}
