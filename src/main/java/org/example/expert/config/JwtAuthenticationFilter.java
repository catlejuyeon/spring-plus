package org.example.expert.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 토큰을 검증하고 Spring Security 인증 객체를 생성하는 필터
 * OncePerRequestFilter를 상속하여 요청당 한 번만 실행되도록 보장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String url = request.getRequestURI();

        // /auth 경로는 JWT 검증 제외 (회원가입, 로그인)
        if (url.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String bearerToken = request.getHeader("Authorization");

        // Authorization 헤더가 없는 경우
        if (bearerToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // "Bearer " 접두사 제거
            String jwt = jwtUtil.substringToken(bearerToken);

            // JWT 유효성 검사 및 클레임 추출
            Claims claims = jwtUtil.extractClaims(jwt);

            if (claims == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 JWT 토큰입니다.");
                return;
            }

            // 클레임에서 사용자 정보 추출
            Long userId = Long.parseLong(claims.getSubject());
            String email = claims.get("email", String.class);
            String nickname = claims.get("nickname", String.class);
            UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

            // AuthUser 객체 생성 (Principal로 사용)
            AuthUser authUser = new AuthUser(userId, email, nickname, userRole);

            // Spring Security에서 권한 체크를 위한 GrantedAuthority 생성
            // UserRole.ADMIN → "ROLE_ADMIN", UserRole.USER → "ROLE_USER"
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userRole.name());

            // Authentication 객체 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authUser,      // principal
                    null,          // credentials (JWT는 stateless이므로 null)
                    List.of(authority)  // authorities
            );

            // SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 다음 필터로 진행
            filterChain.doFilter(request, response);

        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않는 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact of handler are invalid.", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 JWT 토큰입니다.");
        } catch (Exception e) {
            log.error("JWT 토큰 검증 중 오류가 발생했습니다.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
