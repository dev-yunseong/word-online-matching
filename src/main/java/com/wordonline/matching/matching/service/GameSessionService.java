package com.wordonline.matching.matching.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.wordonline.matching.auth.service.UserService;
import com.wordonline.matching.matching.dto.MatchedInfoDto;
import com.wordonline.matching.matching.dto.SessionDto;
import com.wordonline.matching.service.LocalizationService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class GameSessionService {

    private final WebClient webClient;
    private final LocalizationService localizationService;
    private final UserService userService;
    private final String serverUrl;

    public GameSessionService(WebClient.Builder webClientBuilder, @Value("${team6515.server.game.url}") String url,
            LocalizationService localizationService, UserService userService) {
        this.localizationService = localizationService;
        this.webClient = webClientBuilder.baseUrl(url)
                .build();
        this.userService = userService;
        serverUrl = url;
    }

    public Mono<MatchedInfoDto> createSession(SessionDto sessionDto) {
        return webClient.post().uri("/api/server/game-sessions")
                .body(Mono.just(sessionDto), SessionDto.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnNext(isSuccess -> log.info("Response from game server: {}", isSuccess))
                .flatMap(isSuccess -> {
                        if (!isSuccess) {
                            return getException();
                        }
                        log.info("Session Created");
                        return Mono.zip(
                                userService.getUserDetail(sessionDto.uid1()),
                                userService.getUserDetail(sessionDto.uid2())
                        );
                })
                .map(tuple ->
                        new MatchedInfoDto(
                                "Successfully Matched",
                                serverUrl,
                                tuple.getT1(),
                                tuple.getT2(),
                                sessionDto.sessionId()
                        ));
    }

    private <T> Mono<T> getException() {
        return Mono.deferContextual(ctx -> {
            LocaleContext localeContext = ctx.get(LocaleContext.class);
            return Mono.error(
                    new IllegalArgumentException(localizationService.getMessage(localeContext, "error.member.not.found")));
        });
    }
}
