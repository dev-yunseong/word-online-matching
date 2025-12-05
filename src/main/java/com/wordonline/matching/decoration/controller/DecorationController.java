package com.wordonline.matching.decoration.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordonline.matching.decoration.dto.DecorationRequest;
import com.wordonline.matching.decoration.dto.DecorationsResponse;
import com.wordonline.matching.decoration.service.DecorationService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
public class DecorationController {

    private final DecorationService decorationService;

    @GetMapping("/mine/decorations")
    public Mono<DecorationsResponse> getMyDecoration(
            @AuthenticationPrincipal Jwt principalDetails,
            @RequestParam(required = false, defaultValue = "false") boolean equippedOnly
    ) {
        return getDecoration(principalDetails.getClaim("memberId"), equippedOnly);
    }

    @PostMapping("/mine/decorations")
    public Mono<Void> setDecoration(
            @AuthenticationPrincipal Jwt principalDetails,
            @RequestBody DecorationRequest decorationRequest
    ) {
        return decorationService.setDecoration(principalDetails.getClaim("memberId"), decorationRequest);
    }

    @GetMapping("/{memberId}/decorations")
    public Mono<DecorationsResponse> getDecoration(
            @PathVariable long memberId,
            @RequestParam(required = false) boolean equippedOnly
    ) {
        return decorationService.getDecorationsByUserId(memberId, equippedOnly)
                .collectList()
                .map(DecorationsResponse::new);
    }
}
