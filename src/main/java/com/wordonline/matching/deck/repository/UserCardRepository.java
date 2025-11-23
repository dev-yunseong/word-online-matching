package com.wordonline.matching.deck.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.wordonline.matching.deck.domain.UserCard;

import reactor.core.publisher.Flux;

public interface UserCardRepository extends R2dbcRepository<UserCard, Long> {

    Flux<UserCard> findAllByUserId(Long userId);
}
