package com.wordonline.matching.deck.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table("user_cards")
public class UserCard {

    @Id
    private Long id;
    private Long userId;
    private Long cardId;
    private Integer count;

    public UserCard(Long userId, Long cardId, Integer count) {
        this(null, userId, cardId, count);
    }
}
