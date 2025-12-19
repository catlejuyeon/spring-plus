package org.example.expert.domain.todo.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

/**
 * QueryDSL Projections를 활용한 검색 응답 DTO
 * 필요한 필드만 선택적으로 조회하여 성능 최적화
 */
@Getter
public class TodoSearchResponse {

    private final String title;           // 일정 제목
    private final Long managerCount;      // 담당자 수
    private final Long commentCount;      // 댓글 개수

    /**
     * @QueryProjection: QueryDSL이 DTO를 직접 생성할 수 있도록 지원
     * Q클래스에 생성자가 포함되어 타입 안전하게 Projection 가능
     */
    @QueryProjection
    public TodoSearchResponse(String title, Long managerCount, Long commentCount) {
        this.title = title;
        this.managerCount = managerCount;
        this.commentCount = commentCount;
    }
}
