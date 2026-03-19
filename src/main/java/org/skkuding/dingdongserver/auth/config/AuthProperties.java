package org.skkuding.dingdongserver.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private final Jwt jwt = new Jwt();
    private final Oidc oidc = new Oidc();

    public Jwt getJwt() {
        return jwt;
    }

    public Oidc getOidc() {
        return oidc;
    }

    public static class Jwt {
        private String secret;
        private String issuer = "https://api.ding-dong.tv";
        private Duration accessTokenTtl = Duration.ofMinutes(15);
        private Duration refreshTokenTtl = Duration.ofDays(30);

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public Duration getAccessTokenTtl() {
            return accessTokenTtl;
        }

        public void setAccessTokenTtl(Duration accessTokenTtl) {
            this.accessTokenTtl = accessTokenTtl;
        }

        public Duration getRefreshTokenTtl() {
            return refreshTokenTtl;
        }

        public void setRefreshTokenTtl(Duration refreshTokenTtl) {
            this.refreshTokenTtl = refreshTokenTtl;
        }
    }

    public static class Oidc {
        private final Provider google = new Provider();
        private final Provider kakao = new Provider();
        private final Provider apple = new Provider();

        public Provider getGoogle() {
            return google;
        }

        public Provider getKakao() {
            return kakao;
        }

        public Provider getApple() {
            return apple;
        }
    }

    public static class Provider {
        private String discoveryUri;
        private List<String> allowedIssuers = new ArrayList<>();
        private List<String> clientIds = new ArrayList<>();

        public String getDiscoveryUri() {
            return discoveryUri;
        }

        public void setDiscoveryUri(String discoveryUri) {
            this.discoveryUri = discoveryUri;
        }

        public List<String> getAllowedIssuers() {
            return allowedIssuers;
        }

        public void setAllowedIssuers(List<String> allowedIssuers) {
            this.allowedIssuers = sanitize(allowedIssuers);
        }

        public List<String> getClientIds() {
            return clientIds;
        }

        public void setClientIds(List<String> clientIds) {
            this.clientIds = sanitize(clientIds);
        }

        private static List<String> sanitize(List<String> values) {
            List<String> sanitized = new ArrayList<>();

            if (values == null) {
                return sanitized;
            }

            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    sanitized.add(value.trim());
                }
            }

            return sanitized;
        }
    }
}
