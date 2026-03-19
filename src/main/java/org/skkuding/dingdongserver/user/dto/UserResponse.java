package org.skkuding.dingdongserver.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.skkuding.dingdongserver.user.domain.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String provider;
    private String email;
    private String nickname;
    private LocalDateTime lastLoginAt;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getProvider().name(),
                user.getEmail(),
                user.getNickname(),
                user.getLastLoginAt()
        );
    }
}
