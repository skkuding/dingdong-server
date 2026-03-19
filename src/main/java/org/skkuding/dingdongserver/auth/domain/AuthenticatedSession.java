package org.skkuding.dingdongserver.auth.domain;

import org.skkuding.dingdongserver.user.domain.User;

public record AuthenticatedSession(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn,
        User user
) {
}
