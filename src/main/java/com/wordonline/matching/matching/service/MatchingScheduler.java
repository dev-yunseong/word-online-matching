package com.wordonline.matching.matching.service;

import java.time.Duration;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingScheduler {

    private final MatchingService matchingService;
    private Disposable scheduler;

    @PostConstruct
    public void init() {
        scheduler = Flux.interval(Duration.ofSeconds(5))
                .flatMap(t -> tryMatch())
                .subscribe();
    }

    private Mono<Void> tryMatch() {
        if (matchingService.getQueueLength() == 0) {
            return Mono.just(true).then();
        }

        return matchingService.tryMatching();
    }

    public void stop() {
        scheduler.dispose();
    }
}
