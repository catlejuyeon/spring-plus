package org.example.expert.domain.user.repository;

import org.example.expert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * 닉네임으로 유저 검색 (정확히 일치)
     * 대용량 데이터 조회를 위해 나중에 인덱스 최적화 필요
     */
    List<User> findByNickname(String nickname);
}
