package com.wordonline.matching.deck.dto;

import com.wordonline.matching.deck.domain.Card;

public record CardDto(
        long id,
        CardType name,
        CardType.Type type
) {
    public CardDto(long id, CardType cardType) {
        this(id, cardType, cardType.getType());
    }

    public CardDto(Card card) {
        this(card.getId(), card.getName(), card.getCardType());
    }
}

