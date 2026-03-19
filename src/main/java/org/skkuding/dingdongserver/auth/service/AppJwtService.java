package org.skkuding.dingdongserver.auth.service;

import lombok.RequiredArgsConstructor;
import org.skkuding.dingdongserver.auth.config.AuthProperties;
import org.skkuding.dingdongserver.user.domain.User;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AppJwtService {

    private final JwtEncoder jwtEncoder;
    private final AuthProperties authProperties;

    public String createAccessToken(User user, java.time.LocalDateTime issuedAt) {
        Instant now = issuedAt.atZone(java.time.ZoneId.systemDefault()).toInstant();
        Instant expiresAt = now.plus(authProperties.getJwt().getAccessTokenTtl());

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(authProperties.getJwt().getIssuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(String.valueOf(user.getId()))
                .claim("provider", user.getProvider().name())
                .claim("email", user.getEmail())
                .claim("nickname", user.getNickname())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claimsSet
        )).getTokenValue();
    }

    public Duration getAccessTokenTtl() {
        return authProperties.getJwt().getAccessTokenTtl();
    }

    public static OAuth2TokenValidator<Jwt> appTokenValidator(String issuer) {
        return new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                jwt -> StringUtils.hasText(jwt.getSubject())
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "subject가 없습니다.", null))
        );
    }
}
