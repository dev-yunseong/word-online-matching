package com.wordonline.matching.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wordonline.matching.auth.dto.UserResponseDto;
import com.wordonline.matching.auth.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequestMapping("/api/users")
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/mine")
    public Mono<UserResponseDto> getUser(@AuthenticationPrincipal Jwt principalDetails) {
        return userService.getUser(principalDetails.getClaim("memberId"));
    }

    @DeleteMapping("/mine")
    public Mono<ResponseEntity<String>> deleteUser(@AuthenticationPrincipal Jwt principalDetails) {
        return userService.deleteUser(principalDetails.getClaim("memberId"))
                .then(Mono.just(ResponseEntity.ok("successfully delete")))
                .onErrorResume(ex -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    @GetMapping("/mine/status")
    public Mono<Map<String, String>> getMyStatus(
            @AuthenticationPrincipal Jwt principalDetails
    ) {
        return userService.getStatus(principalDetails.getClaim("memberID"))
                .map(status -> Map.of("status", status.name()));
    }
}
