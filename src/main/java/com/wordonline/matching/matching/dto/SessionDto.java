package com.wordonline.matching.matching.dto;

public record SessionDto(
        String sessionId,
        Long uid1,
        Long uid2
) {

}
