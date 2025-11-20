package com.wordonline.matching.deck.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "decks")
public class Deck {

    @Id
    private Long id;
    private Long userId;
    private String name;

    public Deck(Long userId, String name) {
        this(null, userId, name);
    }
}
