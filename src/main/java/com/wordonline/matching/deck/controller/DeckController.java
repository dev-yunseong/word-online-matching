package com.wordonline.matching.deck.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wordonline.matching.deck.dto.CardPoolDto;
import com.wordonline.matching.deck.dto.DeckRequestDto;
import com.wordonline.matching.deck.dto.DeckResponseDto;
import com.wordonline.matching.deck.service.DeckService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/users/mine")
@RestController
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @GetMapping("/cards")
    public Mono<CardPoolDto> getCardPool(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return deckService.getCardPool(jwt.getClaim("memberId"));
    }

    @GetMapping("/decks")
    public Flux<DeckResponseDto> getDecks(
            @AuthenticationPrincipal Jwt principalDetails
    ) {
        return deckService.getDecks(principalDetails.getClaim("memberId"));
    }

    @PostMapping("/decks")
    public Mono<DeckResponseDto> saveDeck(
            @Validated @RequestBody DeckRequestDto deckRequestDto,
            @AuthenticationPrincipal Jwt principalDetails
            ) {
        return deckService.saveDeck(
                    principalDetails.getClaim("memberId"),
                    deckRequestDto);
    }

    @PutMapping("/decks/{deckId}")
    public Mono<DeckResponseDto> updateDeck(
            @PathVariable Long deckId,
            @Validated @RequestBody DeckRequestDto deckRequestDto,
            @AuthenticationPrincipal Jwt principalDetails
    ) {
        return deckService.updateDeck(
                principalDetails.getClaim("memberId"),
                deckId,
                deckRequestDto);
    }

    @PostMapping("/decks/{deckId}")
    public Mono<String> selectDeck(
            @PathVariable Long deckId,
            @AuthenticationPrincipal Jwt principalDetails
    ) {
        return deckService.selectDeck(
                principalDetails.getClaim("memberId"),
                deckId
        ).then(Mono.just("Successfully Saved"));
    }
}
