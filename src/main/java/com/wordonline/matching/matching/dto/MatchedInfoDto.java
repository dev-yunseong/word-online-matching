package com.wordonline.matching.matching.dto;

import com.wordonline.matching.auth.dto.UserDetailResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchedInfoDto{
    private String message;
    private String server;
    private UserDetailResponseDto leftUser;
    private UserDetailResponseDto rightUser;
    private String sessionId;
}
