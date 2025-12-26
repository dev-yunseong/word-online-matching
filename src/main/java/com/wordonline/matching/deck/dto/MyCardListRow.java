package com.wordonline.matching.deck.dto;

public interface MyCardListRow {
    Long getId();
    String getName();
    String getType();
    Integer getCount();
    Boolean getUnlocked();
    String getUnlockText();
    String getProgressText();
}