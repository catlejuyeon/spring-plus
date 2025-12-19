package org.example.expert.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 중앙 설정
 * JWT 기반 인증을 사용하므로 세션은 STATELESS로 설정
 * URL별 권한 규칙을 정의하고 커스텀 필터와 에러 핸들러 등록
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 기반 REST API이므로 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 관리: STATELESS (JWT 토큰 기반이므로 서버에 세션 저장 안 함)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 엔드포인트 (회원가입, 로그인)
                        .requestMatchers("/auth/**").permitAll()

                        // 관리자 전용 엔드포인트 (ADMIN 권한 필요)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 커스텀 예외 처리 핸들러 등록
                .exceptionHandling(exception -> exception
                        // 401 Unauthorized: 인증 실패 시
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        // 403 Forbidden: 권한 부족 시
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
