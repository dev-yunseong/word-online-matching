package com.wordonline.matching.session.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wordonline.matching.matching.dto.MatchedInfoDto;
import com.wordonline.matching.session.domain.SessionRecoveryInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SessionRecoveryStore {

    private final Map<Long, SessionRecoveryInfo> sessionMap = new ConcurrentHashMap<>();

    public SessionRecoveryInfo getSessionInfo(Long userId) {
        return sessionMap.get(userId);
    }

    public void storeMatchInfo(MatchedInfoDto matchedInfoDto) {
        SessionRecoveryInfo sessionRecoveryInfo = new SessionRecoveryInfo(matchedInfoDto);
        storeMatchInfo(matchedInfoDto.getLeftUser().id(), sessionRecoveryInfo);
        storeMatchInfo(matchedInfoDto.getRightUser().id(), sessionRecoveryInfo);
    }

    private void storeMatchInfo(Long userId, SessionRecoveryInfo sessionRecoveryInfo) {
        if (userId < 0) {
            return;
        }
        sessionMap.put(userId, sessionRecoveryInfo);
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void cleanupExpiredSessions() {
        log.info("[SessionRecoveryStore] Cleaning expired sessions");
        sessionMap.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
