package com.wordonline.matching.auth.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import com.wordonline.matching.auth.domain.User;
import com.wordonline.matching.auth.domain.UserStatus;

import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, Long> {

    @Query("""
UPDATE users
SET selected_deck_id = :deckId
WHERE id = :userId;
""")
    Mono<Long> updateSelectedDeck(@Param("userId") Long userId, @Param("deckId") Long deckId);

    @Query("""
UPDATE users
SET status = CAST(:status AS user_status)
WHERE id = :userId;
""")
    Mono<Long> updateStatus(@Param("userId") Long userId, @Param("status") UserStatus status);

    @Query("""
INSERT INTO users(id, status) VALUES
                      (:userId, 'Online');
""")
    Mono<Long> insertUser(@Param("userId") Long userId);
}
