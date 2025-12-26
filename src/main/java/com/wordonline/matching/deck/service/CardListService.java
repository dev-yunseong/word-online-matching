package com.wordonline.matching.deck.service;

import com.wordonline.matching.deck.dto.CardListItem;
import com.wordonline.matching.deck.dto.CardListResponse;
import com.wordonline.matching.deck.repository.CardListQueryRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CardListService {

    private final CardListQueryRepository repo;

    public CardListService(CardListQueryRepository repo) {
        this.repo = repo;
    }

    public Mono<CardListResponse> getMyCards(long userId) {
        return repo.findMyCardList(userId)
                .map(r -> new CardListItem(
                        r.getId(),
                        r.getName(),
                        r.getType(),
                        r.getCount() == null ? 0 : r.getCount(),
                        Boolean.TRUE.equals(r.getUnlocked()),
                        r.getUnlockText(),
                        r.getProgressText()
                ))
                .collectList()
                .map(CardListResponse::new);
    }
}