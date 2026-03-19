package org.skkuding.dingdongserver.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.skkuding.dingdongserver.auth.domain.AuthenticatedSession;
import org.skkuding.dingdongserver.user.dto.UserResponse;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenResponse {
    private String accessToken;
    private long expiresIn;
    private String refreshToken;
    private long refreshTokenExpiresIn;
    private String tokenType;
    private UserResponse user;

    public static AuthTokenResponse from(AuthenticatedSession session) {
        return new AuthTokenResponse(
                session.accessToken(),
                session.accessTokenExpiresIn(),
                session.refreshToken(),
                session.refreshTokenExpiresIn(),
                "Bearer",
                UserResponse.from(session.user())
        );
    }
}
