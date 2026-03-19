package org.skkuding.dingdongserver.auth.service;

import lombok.RequiredArgsConstructor;
import org.skkuding.dingdongserver.auth.config.AuthProperties;
import org.skkuding.dingdongserver.auth.domain.RefreshToken;
import org.skkuding.dingdongserver.auth.domain.RefreshTokenSession;
import org.skkuding.dingdongserver.auth.repository.RefreshTokenRepository;
import org.skkuding.dingdongserver.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_SIZE = 64;

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthProperties authProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public RefreshTokenSession issue(User user, LocalDateTime now) {
        String rawRefreshToken = generateRefreshToken();
        String refreshTokenHash = hash(rawRefreshToken);

        refreshTokenRepository.save(RefreshToken.issue(
                user,
                refreshTokenHash,
                now.plus(authProperties.getJwt().getRefreshTokenTtl()),
                now
        ));

        return new RefreshTokenSession(
                user,
                rawRefreshToken,
                authProperties.getJwt().getRefreshTokenTtl().toSeconds()
        );
    }

    @Transactional
    public RefreshTokenSession rotate(String rawRefreshToken, LocalDateTime now) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token 입니다."));

        if (!storedToken.isAvailableAt(now)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "만료되었거나 폐기된 refresh token 입니다.");
        }

        String nextRawRefreshToken = generateRefreshToken();
        String nextRefreshTokenHash = hash(nextRawRefreshToken);

        storedToken.rotate(nextRefreshTokenHash, now);
        refreshTokenRepository.save(storedToken);
        refreshTokenRepository.save(RefreshToken.issue(
                storedToken.getUser(),
                nextRefreshTokenHash,
                now.plus(authProperties.getJwt().getRefreshTokenTtl()),
                now
        ));

        return new RefreshTokenSession(
                storedToken.getUser(),
                nextRawRefreshToken,
                authProperties.getJwt().getRefreshTokenTtl().toSeconds()
        );
    }

    @Transactional
    public void revoke(String rawRefreshToken, LocalDateTime now) {
        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .ifPresent(refreshToken -> {
                    refreshToken.revoke(now);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    private String generateRefreshToken() {
        byte[] randomBytes = new byte[REFRESH_TOKEN_SIZE];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hash(String rawRefreshToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 hash를 사용할 수 없습니다.", exception);
        }
    }
}
