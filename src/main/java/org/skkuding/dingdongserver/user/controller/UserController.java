package org.skkuding.dingdongserver.user.controller;

import lombok.RequiredArgsConstructor;
import org.skkuding.dingdongserver.user.domain.User;
import org.skkuding.dingdongserver.user.dto.UserResponse;
import org.skkuding.dingdongserver.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.getUser(Long.parseLong(jwt.getSubject()));
        return UserResponse.from(user);
    }
}
