package org.skkuding.dingdongserver.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.skkuding.dingdongserver.auth.domain.AuthProvider;

@Getter
@Setter
@NoArgsConstructor
public class OidcLoginRequest {

    @NotNull
    private AuthProvider provider;

    @NotBlank
    private String idToken;

    private String nickname;

    private String nonce;
}
