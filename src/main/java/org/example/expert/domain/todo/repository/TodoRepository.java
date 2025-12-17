package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    /**
     * left join fetch t.user사용하여 n+1문제 방지(findByWeatherAndDateRange까지)
     * 수정일 내림차순으로 정렬
     * LIKE 연산자로 부분 일치 검색, 대소문자 구분 없음
     * @param weather
     * @param pageable
     */
    //weather 조건만 검색
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user u " +
            "WHERE LOWER(t.weather) LIKE LOWER(CONCAT('%', :weather, '%')) " +
            "ORDER BY t.modifiedAt DESC")
    Page<Todo> findByWeather(@Param("weather") String weather, Pageable pageable);

    // 수정일 기간만 검색
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user u " +
            "WHERE t.modifiedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.modifiedAt DESC")
    Page<Todo> findByModifiedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    //weather + 수정일 기간 검색
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user u " +
            "WHERE LOWER(t.weather) LIKE LOWER(CONCAT('%', :weather, '%')) " +
            "AND t.modifiedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.modifiedAt DESC")
    Page<Todo> findByWeatherAndDateRange(
            @Param("weather") String weather,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
