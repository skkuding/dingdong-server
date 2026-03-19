package org.skkuding.dingdongserver.auth.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OidcDiscoveryDocument(
        @JsonProperty("jwks_uri")
        String jwksUri
) {
}
