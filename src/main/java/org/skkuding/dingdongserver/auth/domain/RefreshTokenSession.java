package org.skkuding.dingdongserver.auth.domain;

import org.skkuding.dingdongserver.user.domain.User;

public record RefreshTokenSession(
        User user,
        String refreshToken,
        long refreshTokenExpiresIn
) {
}
