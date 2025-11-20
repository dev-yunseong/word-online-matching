package com.wordonline.matching.auth.service;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wordonline.matching.auth.domain.User;
import com.wordonline.matching.auth.domain.UserStatus;
import com.wordonline.matching.auth.dto.UserDetailResponseDto;
import com.wordonline.matching.auth.dto.UserResponseDto;
import com.wordonline.matching.auth.repository.UserRepository;
import com.wordonline.matching.deck.service.DeckService;
import com.wordonline.matching.matching.client.AccountClient;
import com.wordonline.matching.matching.dto.AccountMemberResponseDto;
import com.wordonline.matching.service.LocalizationService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DeckService deckService;
    private final AccountClient accountClient;
    private final LocalizationService localizationService;

    public Mono<User> initialUser(long memberId) {
        User user = new User(memberId);
        return userRepository.save(user)
                .onErrorResume(e ->
                    Mono.deferContextual(ctx -> {
                        LocaleContext localeContext = ctx.get(LocaleContext.class);
                        String message = localizationService.getMessage(localeContext, "error.register.failed");
                        return Mono.error(
                                new AuthorizationDeniedException(message)
                        );
                    })
                ).flatMap(saveUser ->
                    deckService.initializeCard(saveUser.getId())
                            .map(deckId -> Tuples.of(saveUser, deckId))
                ).flatMap(tuple -> {
                    tuple.getT1().setSelectedDeckId(tuple.getT2());
                    return userRepository.save(tuple.getT1());
                });
    }

    public Mono<UserResponseDto> getUser(long memberId) {
        return findUserDomain(memberId)
                .onErrorResume(e -> initialUser(memberId))
                .map(UserResponseDto::new);
    }

    public Mono<UserDetailResponseDto> getUserDetail(long memberId) {
        return accountClient.getMember(memberId)
                .map(accountMemberResponseDto ->
                        new UserDetailResponseDto(memberId, accountMemberResponseDto));
    }

    public Mono<Void> deleteUser(long userId) {
        return userRepository.deleteById(userId);
    }

    private Mono<User> findUserDomain(long userId) {
        return userRepository.findById(userId)
                .onErrorResume(e -> throwAuthorizationDeniedException(userId));
    }

    private Mono<User> throwAuthorizationDeniedException(long userId) {
        return Mono.deferContextual(ctx -> {
            LocaleContext localeContext = ctx.get(LocaleContext.class);
            return Mono.error(
                    new AuthorizationDeniedException(
                            localizationService.getMessage(
                                    localeContext,
                                    "error.user.not.found",
                                    new Object[]{userId})));
        });
    }

    public Mono<Void> markMatching(long userId) {
        return findUserDomain(userId)
                .flatMap(user -> {
                    user.markOnline();
                    return userRepository.save(user);
                }).then();
    }

    public Mono<Void> markPlaying(long userId) {
        return findUserDomain(userId)
                .flatMap(user -> {
                    user.markPlaying();
                    return userRepository.save(user);
                }).then();
    }

    public Mono<Void> markOnline(long userId) {
        return findUserDomain(userId)
                .flatMap(user -> {
                    user.markOnline();
                    return userRepository.save(user);
                }).then();
    }

    public Mono<UserStatus> getStatus(long userId) {
        return findUserDomain(userId)
                .map(User::getStatus);
    }
}
