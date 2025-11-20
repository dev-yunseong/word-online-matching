package com.wordonline.matching.deck.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import com.wordonline.matching.deck.domain.Deck;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeckRepository extends R2dbcRepository<Deck, Long> {

    Flux<Deck> findAllByUserId(Long userId);

    @Query("""
UPDATE decks
SET name = :newName
WHERE id = :deckId;
""")
    Mono<Void> updateDeckName(@Param("deckId") Long deckId, @Param("newName") String newName);
}
