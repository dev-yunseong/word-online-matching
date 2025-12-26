package com.wordonline.matching.deck.dto;

import java.util.List;

public record CardListItem(
        long id,
        String name,
        String type,
        int count,
        boolean unlocked,
        String unlockText,
        String progressText
) {}