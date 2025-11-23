package com.wordonline.matching.deck.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.wordonline.matching.deck.dto.CardType;

import lombok.Getter;

@Getter
@Table(name = "cards")
public class Card {
    @Id
    private Long id;
    private CardType name;
    private CardType.Type cardType;
}
