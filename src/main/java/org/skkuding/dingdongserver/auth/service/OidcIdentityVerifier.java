package org.skkuding.dingdongserver.auth.service;

import lombok.RequiredArgsConstructor;
import org.skkuding.dingdongserver.auth.config.AuthProperties;
import org.skkuding.dingdongserver.auth.domain.AuthProvider;
import org.skkuding.dingdongserver.auth.domain.OidcDiscoveryDocument;
import org.skkuding.dingdongserver.auth.domain.OidcIdentity;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OidcIdentityVerifier {

    private final AuthProperties authProperties;
    private final RestClient.Builder restClientBuilder;
    private final Map<AuthProvider, JwtDecoder> decoders = new ConcurrentHashMap<>();

    public OidcIdentity verify(AuthProvider provider, String idToken, String expectedNonce) {
        if (provider == null || provider == AuthProvider.LOCAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 OIDC provider 입니다.");
        }

        try {
            Jwt jwt = decoders.computeIfAbsent(provider, this::createDecoder).decode(idToken);
            validateNonce(expectedNonce, jwt);

            if (!StringUtils.hasText(jwt.getSubject())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "subject가 없는 ID 토큰입니다.");
            }

            return new OidcIdentity(
                    provider,
                    jwt.getSubject(),
                    jwt.getClaimAsString("email"),
                    resolveNickname(jwt)
            );
        } catch (JwtException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 ID 토큰입니다.", exception);
        }
    }

    private JwtDecoder createDecoder(AuthProvider provider) {
        AuthProperties.Provider config = resolveProviderConfig(provider);

        if (!StringUtils.hasText(config.getDiscoveryUri())) {
            throw new IllegalStateException(provider + " discovery URI가 설정되지 않았습니다.");
        }

        if (config.getClientIds().isEmpty()) {
            throw new IllegalStateException(provider + " client ID가 설정되지 않았습니다.");
        }

        OidcDiscoveryDocument discoveryDocument = restClientBuilder.build()
                .get()
                .uri(config.getDiscoveryUri())
                .retrieve()
                .body(OidcDiscoveryDocument.class);

        if (discoveryDocument == null || !StringUtils.hasText(discoveryDocument.jwksUri())) {
            throw new IllegalStateException(provider + " jwks_uri를 찾지 못했습니다.");
        }

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(discoveryDocument.jwksUri()).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                issuerValidator(config.getAllowedIssuers()),
                audienceValidator(config.getClientIds())
        ));
        return decoder;
    }

    private OAuth2TokenValidator<Jwt> issuerValidator(List<String> allowedIssuers) {
        return token -> {
            String issuer = token.getIssuer() != null ? token.getIssuer().toString() : null;

            if (issuer != null && allowedIssuers.contains(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "유효하지 않은 issuer 입니다.", null));
        };
    }

    private OAuth2TokenValidator<Jwt> audienceValidator(List<String> clientIds) {
        return token -> {
            for (String audience : token.getAudience()) {
                if (clientIds.contains(audience)) {
                    return OAuth2TokenValidatorResult.success();
                }
            }

            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "유효하지 않은 audience 입니다.", null));
        };
    }

    private void validateNonce(String expectedNonce, Jwt jwt) {
        if (!StringUtils.hasText(expectedNonce)) {
            return;
        }

        String tokenNonce = jwt.getClaimAsString("nonce");

        if (!expectedNonce.equals(tokenNonce)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "nonce 검증에 실패했습니다.");
        }
    }

    private String resolveNickname(Jwt jwt) {
        String name = jwt.getClaimAsString("name");

        if (StringUtils.hasText(name)) {
            return name;
        }

        String nickname = jwt.getClaimAsString("nickname");
        return StringUtils.hasText(nickname) ? nickname : null;
    }

    private AuthProperties.Provider resolveProviderConfig(AuthProvider provider) {
        return switch (provider) {
            case GOOGLE -> authProperties.getOidc().getGoogle();
            case KAKAO -> authProperties.getOidc().getKakao();
            case APPLE -> authProperties.getOidc().getApple();
            case LOCAL -> throw new IllegalArgumentException("LOCAL provider는 OIDC를 사용하지 않습니다.");
        };
    }
}
