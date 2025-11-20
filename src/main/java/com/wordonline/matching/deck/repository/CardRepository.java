package com.wordonline.matching.deck.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.wordonline.matching.deck.domain.Card;

import reactor.core.publisher.Flux;

public interface CardRepository extends R2dbcRepository<Card, Long> {

//    Flux<Card> findAll();
}

