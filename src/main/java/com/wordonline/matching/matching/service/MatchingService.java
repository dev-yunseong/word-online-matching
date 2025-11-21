package com.wordonline.matching.matching.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.wordonline.matching.deck.service.DeckService;
import com.wordonline.matching.auth.service.UserService;
import com.wordonline.matching.matching.dto.SessionDto;
import com.wordonline.matching.matching.dto.SimpleMessageDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingService {

    private final ServerEventService serverEventService;
    private final ReentrantLock lock = new ReentrantLock();
    private final Queue<Long> matchingQueue = new ConcurrentLinkedQueue<>();
    private final static AtomicInteger sessionIdCounter = new AtomicInteger(1);

    private final DeckService deckService;
    private final UserService userService;
    private final GameSessionService gameSessionService;

    public int getQueueLength() {
        return matchingQueue.size();
    }

    public boolean isInQueue(long userId) {
        return matchingQueue.contains(userId);
    }

    public Flux<Object> requestMatching(long userId) {
        Flux<Object> userFlux = serverEventService.subscribe(userId);

        enqueue(userId)
                .flatMap(isSuccess -> {
                    if (isSuccess) {
                        return serverEventService.send(userId, new SimpleMessageDto("Successfully Enqueued"));
                    } else {
                        return serverEventService.send(userId, new SimpleMessageDto("Failed to enqueue user"))
                                .then(serverEventService.unsubscribe(userId));
                    }
                }).subscribe();

        return userFlux;
    }

    private Mono<Boolean> enqueue(long userId) {
        if (isInQueue(userId)) {
            return Mono.just(true);
        }

        return userService.markMatching(userId)
                .then(deckService.hasSelectedDeck(userId))
                .flatMap(has -> {
                    if (!has) {
                        return Mono.error(new IllegalStateException("Deck has no selected"));
                    }
                    matchingQueue.add(userId);
                    return Mono.just(true);
                })
                .onErrorResume(e -> {
                            log.warn("매칭 불가 상태: {} -> {}", userId, e.getMessage());
                            return userService.markOnline(userId)
                                    .thenReturn(false);
                        }
                );
    }

    public Mono<Void> removeFromQueue(long id) {
        matchingQueue.remove(id);
        return serverEventService.unsubscribe(id);
    }

    public Mono<Boolean> tryMatching() {
        return Mono.fromCallable(this::tryMatchUsers)
                .subscribeOn(Schedulers.single())
                .flatMap(booleanMono -> booleanMono);
    }

    private Mono<Boolean> tryMatchUsers() {
        if (matchingQueue.size() < 2) {
            return Mono.just(false);
        }

        long sessionId = sessionIdCounter.incrementAndGet();

        Long uid1 = matchingQueue.poll();
        Long uid2 = matchingQueue.poll();

        if (uid1 == null || uid2 == null) {
            return Mono.just(false);
        }

        return createSession(sessionId, uid1, uid2);
    }

    public Mono<Boolean> matchPractice(long userId) {
        return removeFromQueue(userId)
                .then(createSession(
                        sessionIdCounter.incrementAndGet(),
                        userId,
                        -1)
                );
    }

    private Mono<Boolean> createSession(long sessionId, long uid1, long uid2) {
        SessionDto sessionDto = new SessionDto("session-" + sessionId, uid1, uid2);

        return gameSessionService.createSession(sessionDto)
                .flatMap(matchedInfoDto -> Mono.zip(
                                userService.markOnline(uid1),
                                userService.markOnline(uid2))
                        .thenReturn(matchedInfoDto))
                .flatMap(matchedInfoDto -> Mono.zip(
                        serverEventService.send(uid1, matchedInfoDto),
                        serverEventService.send(uid2, matchedInfoDto))
                        .thenReturn(true))
                .onErrorResume(e ->
                        Mono.zip(
                                userService.markOnline(uid1),
                                userService.markOnline(uid2)
                        ).thenReturn(false)
                );
    }

    public String getHealthLog() {
        return "Matching Queue: " + matchingQueue;
    }
}
