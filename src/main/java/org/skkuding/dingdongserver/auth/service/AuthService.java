package org.skkuding.dingdongserver.auth.service;

import lombok.RequiredArgsConstructor;
import org.skkuding.dingdongserver.auth.domain.AuthenticatedSession;
import org.skkuding.dingdongserver.auth.domain.OidcIdentity;
import org.skkuding.dingdongserver.auth.domain.RefreshTokenSession;
import org.skkuding.dingdongserver.auth.dto.LogoutRequest;
import org.skkuding.dingdongserver.auth.dto.OidcLoginRequest;
import org.skkuding.dingdongserver.auth.dto.RefreshTokenRequest;
import org.skkuding.dingdongserver.user.domain.User;
import org.skkuding.dingdongserver.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OidcIdentityVerifier oidcIdentityVerifier;
    private final UserRepository userRepository;
    private final AppJwtService appJwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthenticatedSession login(OidcLoginRequest request) {
        OidcIdentity identity = oidcIdentityVerifier.verify(request.getProvider(), request.getIdToken(), request.getNonce());
        LocalDateTime now = LocalDateTime.now();

        User user = userRepository.findByProviderAndProviderSubject(identity.provider(), identity.subject())
                .map(existingUser -> {
                    existingUser.login(identity.email(), resolveNickname(request, identity), now);
                    return existingUser;
                })
                .orElseGet(() -> User.create(
                        identity.provider(),
                        identity.subject(),
                        identity.email(),
                        resolveNickname(request, identity),
                        now
                ));

        User savedUser = userRepository.save(user);
        return createAuthenticatedSession(savedUser, now);
    }

    @Transactional
    public AuthenticatedSession refresh(RefreshTokenRequest request) {
        LocalDateTime now = LocalDateTime.now();
        RefreshTokenSession refreshTokenSession = refreshTokenService.rotate(request.getRefreshToken(), now);
        return createAuthenticatedSession(refreshTokenSession.user(), refreshTokenSession, now);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.getRefreshToken(), LocalDateTime.now());
    }

    private String resolveNickname(OidcLoginRequest request, OidcIdentity identity) {
        if (StringUtils.hasText(request.getNickname())) {
            return request.getNickname().trim();
        }

        return identity.nickname();
    }

    private AuthenticatedSession createAuthenticatedSession(User user, LocalDateTime now) {
        RefreshTokenSession refreshTokenSession = refreshTokenService.issue(user, now);
        return createAuthenticatedSession(user, refreshTokenSession, now);
    }

    private AuthenticatedSession createAuthenticatedSession(User user, RefreshTokenSession refreshTokenSession, LocalDateTime now) {
        String accessToken = appJwtService.createAccessToken(user, now);

        return new AuthenticatedSession(
                accessToken,
                appJwtService.getAccessTokenTtl().toSeconds(),
                refreshTokenSession.refreshToken(),
                refreshTokenSession.refreshTokenExpiresIn(),
                user
        );
    }
}
