package com.wordonline.matching.matching.service;

import java.util.Random;

import org.springframework.stereotype.Component;

import com.wordonline.matching.matching.dto.AccountMemberResponseDto;

import reactor.core.publisher.Mono;

@Component
public class BotMemberMaker {

    private static final Random random = new Random();

    public long getRandomBotMemberId() {
        return -1L * (random.nextInt(2) + 1);
    }

    public Mono<AccountMemberResponseDto> getBot(long botId) {
        String name = switch ((int) botId) {
            case -1 -> "master of everything";
            case -2 -> "master of lightning water";
            case -3 -> "master of fire rock";
            case -4 -> "master of water nature";
            default -> "bot";
        };
        return Mono.just(new AccountMemberResponseDto("bot@team6515.com", name));
    }
}
