package org.skkuding.dingdongserver.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.skkuding.dingdongserver.auth.domain.AuthenticatedSession;
import org.skkuding.dingdongserver.auth.dto.AuthTokenResponse;
import org.skkuding.dingdongserver.auth.dto.LogoutRequest;
import org.skkuding.dingdongserver.auth.dto.OidcLoginRequest;
import org.skkuding.dingdongserver.auth.dto.RefreshTokenRequest;
import org.skkuding.dingdongserver.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/oidc/login")
    public AuthTokenResponse login(@Valid @RequestBody OidcLoginRequest request) {
        AuthenticatedSession session = authService.login(request);
        return AuthTokenResponse.from(session);
    }

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthenticatedSession session = authService.refresh(request);
        return AuthTokenResponse.from(session);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
    }
}
