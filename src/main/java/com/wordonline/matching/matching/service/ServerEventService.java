package com.wordonline.matching.matching.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

@Slf4j
@Service
public class ServerEventService {

    private final Map<Long, Many<Object>> userSinks = new ConcurrentHashMap<>();

    public Flux<Object> subscribe(Long userId) {
        Many<Object> many = Sinks.many().unicast().onBackpressureBuffer();
        userSinks.put(userId, many);
        log.info("User sink created");
        return userSinks.get(userId)
                .asFlux();
    }

    public Mono<Void> unsubscribe(long userId) {
        Many<Object> many = userSinks.remove(userId);
        log.info("User {} sink removed", userId);
        if (many != null) {
            many.tryEmitComplete();
        }
        return Mono.just(0).then();
    }

    public Mono<Boolean> send(long userId, Object data) {
        log.info("[Practice] Sending user id: {}", userId);
        if (userId < 0) {
            return Mono.just(true);
        }
        if (userSinks.containsKey(userId)) {
            return Mono.just(userSinks.get(userId)
                    .tryEmitNext(data)
                    .isSuccess());
        }

        log.info("[Practice] user {}'s sinks not found", userId);
        return Mono.just(false);
    }
}
