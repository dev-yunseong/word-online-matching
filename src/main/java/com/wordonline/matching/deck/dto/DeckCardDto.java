package com.wordonline.matching.deck.dto;

import com.wordonline.matching.deck.domain.Deck;

public record DeckCardDto(
        long deckId,
        long cardId,
        int count,
        String deckName,
        CardType cardName,
        CardType.Type type
) {
    public DeckCardDto(Deck deck, CardDto cardDto, int count) {
        this(deck.getId(), cardDto.id(), count, deck.getName(), cardDto.name(), cardDto.type());
    }
}
