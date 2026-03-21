package org.skkuding.dingdongserver.auth.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.skkuding.dingdongserver.auth.config.AuthProperties;
import org.skkuding.dingdongserver.auth.config.JwtSecretKeyFactory;
import org.skkuding.dingdongserver.auth.domain.RefreshJwt;
import org.skkuding.dingdongserver.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class RefreshJwtService {

    private final AuthProperties authProperties;
    private final JwtEncoder refreshJwtEncoder;
    private final JwtDecoder refreshJwtDecoder;

    public RefreshJwtService(AuthProperties authProperties) {
        this.authProperties = authProperties;
        var refreshKey = JwtSecretKeyFactory.hmacSha256(
                authProperties.getJwt().getRefreshSecret(),
                "AUTH_REFRESH_JWT_SECRET"
        );

        this.refreshJwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(refreshKey));

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(refreshKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(authProperties.getJwt().getIssuer()),
                jwt -> "refresh".equals(jwt.getClaimAsString("token_type"))
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "refresh token type 이 아닙니다.", null)),
                jwt -> StringUtils.hasText(jwt.getId())
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "refresh token jti 가 없습니다.", null)),
                jwt -> StringUtils.hasText(jwt.getSubject())
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "refresh token subject 가 없습니다.", null))
        ));
        this.refreshJwtDecoder = decoder;
    }

    public RefreshJwt createRefreshToken(User user, LocalDateTime issuedAt) {
        Instant now = issuedAt.atZone(ZoneId.systemDefault()).toInstant();
        Instant expiresAt = now.plus(authProperties.getJwt().getRefreshTokenTtl());
        String tokenId = UUID.randomUUID().toString();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(authProperties.getJwt().getIssuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(String.valueOf(user.getId()))
                .id(tokenId)
                .claim("token_type", "refresh")
                .build();

        String tokenValue = refreshJwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claimsSet
        )).getTokenValue();

        return new RefreshJwt(
                tokenValue,
                tokenId,
                authProperties.getJwt().getRefreshTokenTtl().toSeconds()
        );
    }

    public Jwt decode(String refreshToken) {
        try {
            return refreshJwtDecoder.decode(refreshToken);
        } catch (JwtException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token 입니다.", exception);
        }
    }
}
