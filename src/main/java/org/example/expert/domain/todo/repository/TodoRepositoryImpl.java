package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

/**
 * QueryDSL구현체(핵심) - TodoRepositoryCustom 인터페이스의 실제 구현
 * QueryDSL 코드로 쿼리 작성
 * 반드시 Impl 접미사
 * 커스텀 repository 써보고 싶었는데 리포지토리 안더럽히고 깔끔 + 재밌다(다른애들도 바꿔 볼 것)
 */
@Repository
@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // findByIdWithUser는 QueryDSL로 구현
    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()  // N+1 방지
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}