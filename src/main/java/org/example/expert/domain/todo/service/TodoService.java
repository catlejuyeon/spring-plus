package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail(), user.getNickname())
        );
    }

    /**
     * weather + 날짜 범위 모두 있는 경우
     * weather만 있는 경우
     * 날짜 범위만 있는 경우
     * 조건이 없는 경우 (기존 동작)
     */
    public Page<TodoResponse> getTodos(int page, int size, String weather, String startDateStr, String endDateStr) {
        Pageable pageable = PageRequest.of(page - 1, size);

        // 날짜 문자열을 LocalDateTime으로 변환
        LocalDateTime startDate = startDateStr != null ? LocalDateTime.parse(startDateStr) : null;
        LocalDateTime endDate = endDateStr != null ? LocalDateTime.parse(endDateStr) : null;

        Page<Todo> todos;

        // 조건에 따라 적절한 쿼리 메소드 호출
        if (weather != null && startDate != null && endDate != null) {
            // weather + 날짜 범위
            todos = todoRepository.findByWeatherAndDateRange(weather, startDate, endDate, pageable);
        } else if (weather != null) {
            // weather만
            todos = todoRepository.findByWeather(weather, pageable);
        } else if (startDate != null && endDate != null) {
            // 날짜 범위만
            todos = todoRepository.findByModifiedAtBetween(startDate, endDate, pageable);
        } else {
            // 조건 없음
            todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);
        }

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail(), todo.getUser().getNickname()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail(), user.getNickname()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }
}
