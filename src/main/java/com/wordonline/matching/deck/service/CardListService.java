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
                .map(r -> {
                    return new CardListItem(
                            r.id(),
                            r.name(),
                            r.type(),
                            r.count(),
                            r.unlocked(),
                            r.unlockText(),
                            r.progressText()
                    );
                })
                .collectList()
                .map(CardListResponse::new);
    }
}