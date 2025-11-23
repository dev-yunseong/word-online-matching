package com.wordonline.matching.deck.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.wordonline.matching.deck.domain.DeckCard;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeckCardRepository extends R2dbcRepository<DeckCard, Long> {

    Flux<DeckCard> findAllByDeckId(Long deckId);
    Mono<Void> deleteByDeckId(Long deckId);
}
