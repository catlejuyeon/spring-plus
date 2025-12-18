package org.example.expert.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL설정
 * TodoRepositoryImpl에서 JPAQueryFactory를 주입받아 사용
 * 애플리케이션 시작하면 QueryDslConfig의 jpaQueryFactory() 메소드 실행
 * JPAQueryFactory 객체 생성 후 Spring Container에 등록
 * 다른 클래스에서 @RequiredArgsConstructor로 주입받아 사용
 */
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}