package org.skkuding.dingdongserver.user.domain;

import org.skkuding.dingdongserver.auth.domain.AuthProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_users_provider_subject",
                columnNames = {"provider", "provider_subject"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;

    private String email;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    private User(AuthProvider provider, String providerSubject, String email, String nickname, LocalDateTime now) {
        this.provider = provider;
        this.providerSubject = providerSubject;
        this.email = email;
        this.nickname = normalizeNickname(nickname, email, provider, providerSubject);
        this.createdAt = now;
        this.lastLoginAt = now;
    }

    public static User create(AuthProvider provider, String providerSubject, String email, String nickname, LocalDateTime now) {
        return new User(provider, providerSubject, email, nickname, now);
    }

    public void login(String email, String nickname, LocalDateTime now) {
        if (email != null && !email.isBlank()) {
            this.email = email;
        }

        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        } else if (this.nickname == null || this.nickname.isBlank()) {
            this.nickname = normalizeNickname(null, this.email, this.provider, this.providerSubject);
        }

        this.lastLoginAt = now;
    }

    private static String normalizeNickname(String nickname, String email, AuthProvider provider, String providerSubject) {
        if (nickname != null && !nickname.isBlank()) {
            return nickname.trim();
        }

        if (email != null && !email.isBlank()) {
            int atIndex = email.indexOf('@');
            return atIndex > 0 ? email.substring(0, atIndex) : email;
        }

        String providerValue = provider.name().toLowerCase(Locale.ROOT);
        String suffix = providerSubject.length() > 8
                ? providerSubject.substring(providerSubject.length() - 8)
                : providerSubject;

        return providerValue + "-" + suffix;
    }
}
