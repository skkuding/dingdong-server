package org.skkuding.dingdongserver.auth.domain;

public record OidcIdentity(
        AuthProvider provider,
        String subject,
        String email,
        String nickname
) {
}
