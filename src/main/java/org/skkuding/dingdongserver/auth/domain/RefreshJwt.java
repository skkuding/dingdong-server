package org.skkuding.dingdongserver.auth.domain;

public record RefreshJwt(
        String tokenValue,
        String tokenId,
        long expiresIn
) {
}
