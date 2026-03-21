package org.skkuding.dingdongserver.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skkuding.dingdongserver.auth.domain.AuthProvider;
import org.skkuding.dingdongserver.auth.domain.OidcIdentity;
import org.skkuding.dingdongserver.auth.service.OidcIdentityVerifier;
import org.skkuding.dingdongserver.auth.repository.RefreshTokenRepository;
import org.skkuding.dingdongserver.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("dingdong")
            .withUsername("dingdong")
            .withPassword("1234");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private OidcIdentityVerifier oidcIdentityVerifier;

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void firstLoginCreatesUserAndIssuesAccessJwtAndRefreshJwt() throws Exception {
        given(oidcIdentityVerifier.verify(AuthProvider.GOOGLE, "google-id-token", null))
                .willReturn(new OidcIdentity(AuthProvider.GOOGLE, "google-subject-1", "user@ding-dong.tv", "dingdong"));

        JsonNode jsonNode = login(AuthProvider.GOOGLE, "google-id-token");
        String accessToken = jsonNode.get("accessToken").asText();
        String refreshToken = jsonNode.get("refreshToken").asText();

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(accessToken.split("\\.")).hasSize(3);
        assertThat(refreshToken.split("\\.")).hasSize(3);

        mockMvc.perform(get("/user/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("GOOGLE"))
                .andExpect(jsonPath("$.email").value("user@ding-dong.tv"));
    }

    @Test
    void sameIdentityLogsInAgainDoesNotCreateDuplicateUser() throws Exception {
        given(oidcIdentityVerifier.verify(AuthProvider.APPLE, "apple-id-token-1", null))
                .willReturn(new OidcIdentity(AuthProvider.APPLE, "apple-subject-1", "apple@ding-dong.tv", "first-name"));

        login(AuthProvider.APPLE, "apple-id-token-1");

        given(oidcIdentityVerifier.verify(AuthProvider.APPLE, "apple-id-token-2", null))
                .willReturn(new OidcIdentity(AuthProvider.APPLE, "apple-subject-1", "apple-updated@ding-dong.tv", "updated-name"));

        JsonNode secondLogin = login(AuthProvider.APPLE, "apple-id-token-2");

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(secondLogin.get("user").get("email").asText()).isEqualTo("apple-updated@ding-dong.tv");
        assertThat(secondLogin.get("user").get("nickname").asText()).isEqualTo("updated-name");
    }

    @Test
    void refreshRotatesRefreshJwt() throws Exception {
        given(oidcIdentityVerifier.verify(AuthProvider.KAKAO, "kakao-id-token", null))
                .willReturn(new OidcIdentity(AuthProvider.KAKAO, "kakao-subject-1", "user2@ding-dong.tv", "kakao-user"));

        JsonNode loginJson = login(AuthProvider.KAKAO, "kakao-id-token");
        String refreshToken = loginJson.get("refreshToken").asText();

        String refreshResponse = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.provider").value("KAKAO"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode refreshJson = objectMapper.readTree(refreshResponse);
        String nextRefreshToken = refreshJson.get("refreshToken").asText();

        assertThat(nextRefreshToken).isNotEqualTo(refreshToken);
        assertThat(nextRefreshToken.split("\\.")).hasSize(3);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesRefreshJwt() throws Exception {
        given(oidcIdentityVerifier.verify(AuthProvider.GOOGLE, "google-logout-token", null))
                .willReturn(new OidcIdentity(AuthProvider.GOOGLE, "google-subject-logout", "logout@ding-dong.tv", "logout-user"));

        JsonNode loginJson = login(AuthProvider.GOOGLE, "google-logout-token");
        String refreshToken = loginJson.get("refreshToken").asText();

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void multipleRefreshTokensCanCoexistForSameUser() throws Exception {
        given(oidcIdentityVerifier.verify(AuthProvider.KAKAO, "multi-device-token-1", null))
                .willReturn(new OidcIdentity(AuthProvider.KAKAO, "same-user-subject", "same@ding-dong.tv", "same-user"));

        JsonNode firstLogin = login(AuthProvider.KAKAO, "multi-device-token-1");
        String firstRefreshToken = firstLogin.get("refreshToken").asText();

        given(oidcIdentityVerifier.verify(AuthProvider.KAKAO, "multi-device-token-2", null))
                .willReturn(new OidcIdentity(AuthProvider.KAKAO, "same-user-subject", "same@ding-dong.tv", "same-user"));

        JsonNode secondLogin = login(AuthProvider.KAKAO, "multi-device-token-2");
        String secondRefreshToken = secondLogin.get("refreshToken").asText();

        assertThat(firstRefreshToken).isNotEqualTo(secondRefreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(firstRefreshToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(secondRefreshToken)))
                .andExpect(status().isOk());
    }

    @Test
    void userMeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/user/me"))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode login(AuthProvider provider, String idToken) throws Exception {
        String response = mockMvc.perform(post("/auth/oidc/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "%s",
                                  "idToken": "%s"
                                }
                                """.formatted(provider.name().toLowerCase(), idToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }
}
