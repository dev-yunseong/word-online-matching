package com.wordonline.matching.deck.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wordonline.matching.auth.domain.User;
import com.wordonline.matching.auth.repository.UserRepository;
import com.wordonline.matching.deck.domain.Card;
import com.wordonline.matching.deck.domain.Deck;
import com.wordonline.matching.deck.domain.DeckCard;
import com.wordonline.matching.deck.domain.UserCard;
import com.wordonline.matching.deck.dto.CardDto;
import com.wordonline.matching.deck.dto.CardPoolDto;
import com.wordonline.matching.deck.dto.DeckCardDto;
import com.wordonline.matching.deck.dto.DeckRequestDto;
import com.wordonline.matching.deck.dto.DeckResponseDto;
import com.wordonline.matching.deck.repository.CardRepository;
import com.wordonline.matching.deck.repository.DeckCardRepository;
import com.wordonline.matching.deck.repository.DeckRepository;
import com.wordonline.matching.deck.repository.UserCardRepository;
import com.wordonline.matching.service.LocalizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeckService {

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final LocalizationService localizationService;
    private final CardRepository cardRepository;
    private final UserCardRepository userCardRepository;
    private final DeckCardRepository deckCardRepository;

    public Mono<Long> initializeCard(long userId) {
        return giveAllCard(userId).then(
            Mono.deferContextual(ctx -> {
                LocaleContext localeContext = ctx.get(LocaleContext.class);
                String defaultName = localizationService.getMessage(localeContext, "string.default.deck");
                return Mono.just(new Deck(userId, defaultName));
            })).flatMap(deckRepository::save)
                .flatMap(deck -> Flux.range(1, 10)
                            .flatMap(i -> {
                                DeckCard deckCard = new DeckCard(deck.getId(), (long) i, 1);
                                return deckCardRepository.save(deckCard);
                            }).then(Mono.just(deck)))
                .flatMap(deck -> userRepository.updateSelectedDeck(userId, deck.getId())
                        .then(Mono.just(deck.getId())));
    }

    private Mono<Void> giveAllCard(long userId) {
        return Flux.fromStream(getAllCard().stream())
                .flatMap(card -> {
                    UserCard userCard = new UserCard(userId, card.getId(), 3);
                    return userCardRepository.save(userCard);
                })
                .then();
    }

    @Transactional(readOnly = true)
    public Mono<Boolean> hasSelectedDeck(long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getSelectedDeckId() != null);
    }

    @Cacheable("cards")
    @Transactional(readOnly = true)
    public List<Card> getAllCard() {
        return cardRepository.findAll()
                .collectList()
                .block();
    }

    @Cacheable("cardsMap")
    @Transactional(readOnly = true)
    public Map<Long, CardDto> getCardDtoMap() {
        return getAllCard()
                .stream()
                .collect(Collectors.toMap(
                        Card::getId,
                        card -> getCardDto(card.getId())
                ));
    }

    @Cacheable("cardDto")
    @Transactional(readOnly = true)
    public CardDto getCardDto(long cardId) {
        return cardRepository.findById(cardId)
                .map(CardDto::new)
                .block();
    }

    @Transactional(readOnly = true)
    public Flux<DeckResponseDto> getDecks(long userId){
        return deckRepository.findAllByUserId(userId)
            .flatMap(deck -> deckCardRepository.findAllByDeckId(deck.getId())
                    .flatMap(deckCard -> Flux.range(0, deckCard.getCount()).map(i -> deckCard.getCardId()))
                    .collectList()
                    .map(cardIds ->
                        new DeckResponseDto(
                                deck.getId(),
                                deck.getName(),
                                cardIds.stream()
                                        .map(this::getCardDto)
                                        .toList())));
    }

    public Mono<DeckResponseDto> saveDeck(long userId, DeckRequestDto deckRequestDto) {
        Deck deck = new Deck(userId, deckRequestDto.name());
        return deckRepository.save(deck)
                .flatMap(savedDeck -> saveCardsToDeck(userId, deckRequestDto))
                .then(getDeck(deck.getId()));
    }

    public Mono<DeckResponseDto> updateDeck(long userId, long deckId, DeckRequestDto deckRequestDto) {
        return findDeck(deckId, userId)
                .flatMap(deck ->
                        // 기존 카드 삭제
                        deckCardRepository.deleteByDeckId(deckId)
                                .then(deckRepository.updateDeckName(deckId, deckRequestDto.name()))
                                .then(saveCardsToDeck(deckId, deckRequestDto))
                                .then(Mono.just(deckId))
                )
                .flatMap(this::getDeck);
    }

    private Mono<Void> saveCardsToDeck(long deckId, DeckRequestDto deckRequestDto) {
        return Flux.fromStream(deckRequestDto.cardIds().stream()
                        .collect(Collectors.groupingBy(
                                Long::longValue,
                                Collectors.counting()
                        )).entrySet().stream())
                .flatMap(entry -> {
                    DeckCard deckCard = new DeckCard(deckId, entry.getKey(), entry.getValue().intValue());
                    return deckCardRepository.save(deckCard);
                })
                .then();
    }

    private Mono<Deck> findDeck(long deckId, long userId) {
        return deckRepository.findById(deckId)
                .switchIfEmpty(throwException(IllegalArgumentException.class, "error.deck.not.found"))
                .filter(deck -> deck.getUserId() == userId)
                .switchIfEmpty(throwException(IllegalArgumentException.class, "error.not.authorized"));
    }

    private <T> Mono<T> throwException(Class<? extends Exception> exceptionClass, String message) {
        return Mono.deferContextual(ctx -> {
            LocaleContext localeContext = ctx.get(LocaleContext.class);
            String defaultName = localizationService.getMessage(localeContext, message);
            try {
                Constructor<?> constructor = exceptionClass.getConstructor(String.class);
                return Mono.error((Exception) constructor.newInstance(defaultName));
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                return Mono.error(new RuntimeException(defaultName));
            }
        });
    }

    public Mono<Void> selectDeck(long userId, long deckId) {
        return findDeck(deckId, userId)
                .flatMap(deck -> userRepository.updateSelectedDeck(userId, deck.getId()));
    }


    @Transactional(readOnly = true)
    public Mono<DeckResponseDto> getDeck(long deckId) {
        return deckRepository.findById(deckId)
                .flatMapMany(deck ->
                    deckCardRepository.findAllByDeckId(deckId)
                            .map(deckCard -> new DeckCardDto(deck, getCardDto(deckCard.getCardId()), deckCard.getCount())))
                .collectList()
                .map(DeckResponseDto::new);
    }

    @Transactional(readOnly = true)
    public Mono<CardPoolDto> getCardPool(long userId) {
        return userCardRepository.findAllByUserId(userId)
                .collectList()
                .map(userCards -> userCards.stream()
                    .flatMap(userCard ->
                            Stream.generate(() -> getCardDto(userCard.getCardId()))
                                    .limit(userCard.getCount())
                    ).toList())
                .map(CardPoolDto::new);
    }
}