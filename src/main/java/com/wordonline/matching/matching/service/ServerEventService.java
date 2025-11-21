package com.wordonline.matching.matching.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

@Service
public class ServerEventService {

    private final Map<Long, Many<Object>> userSinks = new ConcurrentHashMap<>();

    public Flux<Object> subscribe(Long userId) {
        if (userSinks.containsKey(userId)) {
            return userSinks.get(userId)
                    .asFlux();
        }

        Many<Object> many = Sinks.many().unicast().onBackpressureBuffer();
        userSinks.put(userId, many);
        return many.asFlux();
    }

    public Mono<Void> unsubscribe(long userId) {
        Many<Object> many = userSinks.remove(userId);
        if (many != null) {
            many.tryEmitComplete();
        }
        return Mono.empty();
    }

    public Mono<Boolean> send(long userId, Object data) {
        if (userId < 0) {
            return Mono.just(true);
        }
        if (userSinks.containsKey(userId)) {
            return Mono.just(userSinks.get(userId)
                    .tryEmitNext(data)
                    .isSuccess());
        }

        return Mono.just(false);
    }
}
