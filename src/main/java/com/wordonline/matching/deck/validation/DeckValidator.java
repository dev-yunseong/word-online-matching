package com.wordonline.matching.deck.validation;

import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.wordonline.matching.deck.dto.CardDto;
import com.wordonline.matching.deck.service.DeckService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeckValidator implements ConstraintValidator<DeckValid, List<Long>> {

    private final DeckService deckService;
    public static final int LEAST_NUM_OF_MAGIC_CARD = 2;
    public static final int LEAST_NUM_OF_TYPE_CARD = 2;

    @Override
    public boolean isValid(List<Long> cardIds, ConstraintValidatorContext context) {
        if (cardIds == null || cardIds.isEmpty()) {
            return false;
        }

        Map<Long, CardDto> cards = deckService.getCardDtoMap();
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
    }
}
