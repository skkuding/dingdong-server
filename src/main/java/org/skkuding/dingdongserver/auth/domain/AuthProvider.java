package org.skkuding.dingdongserver.auth.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum AuthProvider {
    LOCAL,
    GOOGLE,
    KAKAO,
    APPLE;

    @JsonCreator
    public static AuthProvider from(String value) {
        if (value == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(provider -> provider.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 provider 입니다. value=" + value));
    }
}
