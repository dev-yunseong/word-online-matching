package com.wordonline.matching.deck.validation;

import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Service;

import com.wordonline.matching.deck.dto.CardDto;
import com.wordonline.matching.deck.service.DeckDataService;
import com.wordonline.matching.deck.service.DeckService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DeckValidator {

    private final DeckDataService deckDataService;
    public static final int LEAST_NUM_OF_MAGIC_CARD = 2;
    public static final int LEAST_NUM_OF_TYPE_CARD = 2;

    public Mono<Boolean> isValid(List<Long> cardIds) {
        if (cardIds == null || cardIds.isEmpty()) {
            return Mono.just(false);
        }

        return deckDataService.getCardDtoMap()
                .map(cards -> {
                    int numOfType = 0, numOfMagic = 0;

                    for (long id : cardIds) {
                        switch (cards.get(id).type()) {
                            case Type -> {
                                numOfType++;
                            }
                            case Magic -> {
                                numOfMagic++;
                            }
                        }
                        if (numOfType >= LEAST_NUM_OF_TYPE_CARD && numOfMagic >= LEAST_NUM_OF_MAGIC_CARD)
                            return true;
                    }

                    return false;
                });
    }
}
