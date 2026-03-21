package org.skkuding.dingdongserver.auth.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public final class JwtSecretKeyFactory {

    private JwtSecretKeyFactory() {
    }

    public static SecretKey hmacSha256(String base64Secret, String envName) {
        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(base64Secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(envName + " 은 유효한 Base64 문자열이어야 합니다.", exception);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(envName + " 은 최소 32바이트 이상이어야 합니다.");
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
}
