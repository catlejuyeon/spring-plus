package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.request.TodoSearchRequest;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 커스텀 리포지토리 인터페이스
 * Spring Data JPA가 기본 제공하지 않는 커스텀 쿼리 정의
 * 왜 인터페이스로 분리?
 * - Spring Data JPA의 Repository 패턴 유지
 * - TodoRepository는 JPA 기본 기능 + 커스텀 기능 모두 사용 가능
 */
public interface TodoRepositoryCustom {
    Optional<Todo> findByIdWithUser(Long todoId);

    /**
     * 일정 검색 (QueryDSL Projections 사용)
     * @param request 검색 조건
     * @return 검색 결과 (제목, 담당자 수, 댓글 개수)
     */
    Page<TodoSearchResponse> searchTodos(TodoSearchRequest request, Pageable pageable);
}