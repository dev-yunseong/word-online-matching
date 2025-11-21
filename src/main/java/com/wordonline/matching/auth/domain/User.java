package com.wordonline.matching.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {
    @Id
    private Long id;
    private UserStatus status;
    @Setter
    private Long selectedDeckId;

    // 상태 전환 편의 메서드
    public void markMatching() {
        this.status = UserStatus.OnMatching;
    }

    public void markPlaying() {
        this.status = UserStatus.OnPlaying;
    }

    public void markOnline() {
        this.status = UserStatus.Online;
    }

    public User(long memberId) {
        this(memberId, UserStatus.Online, null);
    }
}
