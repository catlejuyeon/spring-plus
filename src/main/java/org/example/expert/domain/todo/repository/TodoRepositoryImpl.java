package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.todo.dto.request.TodoSearchRequest;
import org.example.expert.domain.todo.dto.response.QTodoSearchResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;
import static org.springframework.util.StringUtils.hasText;

/**
 * QueryDSL구현체(핵심) - TodoRepositoryCustom 인터페이스의 실제 구현
 * QueryDSL 코드로 쿼리 작성
 * 반드시 Impl 접미사
 * 커스텀 repository 써보고 싶었는데 리포지토리 안더럽히고 깔끔 + 재밌다(다른애들도 바꿔 볼 것)
 */
@Slf4j
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

    /**
     * QueryDSL Projections를 활용한 일정 검색
     * 필요한 필드만 선택적으로 조회하여 성능 최적화
     */
    @Override
    public Page<TodoSearchResponse> searchTodos(TodoSearchRequest request, Pageable pageable) {

        // Projections: 필요한 필드만 조회 (제목, 담당자 수, 댓글 개수)
        List<TodoSearchResponse> content = queryFactory
                .select(new QTodoSearchResponse(
                        todo.title,
                        todo.managers.size().longValue(),  // 담당자 수
                        todo.comments.size().longValue()   // 댓글 개수
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)  // 담당자 닉네임 검색을 위한 조인
                .where(
                        titleContains(request.getTitle()),
                        createdBetween(request.getStartDate(), request.getEndDate()),
                        managerNicknameContains(request.getManagerNickname())
                )
                .groupBy(todo.id)  // 중복 제거 (manager, comment 조인으로 인한)
                .orderBy(todo.createdAt.desc())  // 생성일 최신순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(todo.countDistinct())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(
                        titleContains(request.getTitle()),
                        createdBetween(request.getStartDate(), request.getEndDate()),
                        managerNicknameContains(request.getManagerNickname())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    //제목 부분 일치 검색
    private BooleanExpression titleContains(String title) {
        return hasText(title) ? todo.title.containsIgnoreCase(title) : null;
    }

   //생성일 범위 검색
    private BooleanExpression createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return todo.createdAt.between(startDate, endDate);
        } else if (startDate != null) {
            return todo.createdAt.goe(startDate);
        } else if (endDate != null) {
            return todo.createdAt.loe(endDate);
        }
        return null;
    }

    //담당자 닉네임 부분 일치 검색
    private BooleanExpression managerNicknameContains(String nickname) {
        return hasText(nickname) ? user.nickname.containsIgnoreCase(nickname) : null;
    }
}