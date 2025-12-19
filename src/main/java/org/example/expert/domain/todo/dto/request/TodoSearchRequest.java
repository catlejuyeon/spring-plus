package org.example.expert.domain.todo.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 일정 검색 조건 요청 DTO
 * 모든 조건은 선택적(Optional)
 */
@Getter
@Setter
@NoArgsConstructor
public class TodoSearchRequest {

    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String managerNickname;
}
