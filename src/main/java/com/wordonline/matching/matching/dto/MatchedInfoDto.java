package com.wordonline.matching.matching.dto;

import com.wordonline.matching.auth.dto.UserDetailResponseDto;
import com.wordonline.matching.session.domain.SessionRecoveryInfo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchedInfoDto{
    private final String type = "matchedInfoDto";
    private String message;
    private String server;
    private UserDetailResponseDto leftUser;
    private UserDetailResponseDto rightUser;
    private String sessionId;

    public MatchedInfoDto(SessionRecoveryInfo sessionInfo, UserDetailResponseDto leftUser, UserDetailResponseDto rightUser) {
        this(
                "Successfully Match Info Recovered",
                sessionInfo.serverUrl(),
                leftUser,
                rightUser,
                sessionInfo.sessionId()
        );
    }
}
