package co.za.millenniumsolutions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IsolatedSqliteTest
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void cleanSessions() {
        jdbc.execute("DELETE FROM auth_session");
    }

    @Test
    void anonymousBusinessAccessIsDenied() throws Exception {
        mvc.perform(get("/api/users/missing/notifications")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsBearerTokenAndTokenAuthorizesRequests() throws Exception {
        jdbc.update("""
                INSERT INTO app_user(id, username, display_name, system_role, active, password_hash)
                VALUES ('auth-test-user', 'auth.test', 'Auth Test', 'STUDENT', 1, ?)
                ON CONFLICT(id) DO UPDATE SET password_hash=excluded.password_hash, active=1
                """, encoder.encode("correct-password"));

        String response = mvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                        .content("""
                                {"username":"auth.test","password":"correct-password"}
                                """))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(response).get("token").asText();

        mvc.perform(get("/api/users/missing/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidPasswordIsRejected() throws Exception {
        jdbc.update("""
                INSERT INTO app_user(id, username, display_name, system_role, active, password_hash)
                VALUES ('auth-bad-user', 'auth.bad', 'Auth Bad', 'STUDENT', 1, ?)
                ON CONFLICT(id) DO UPDATE SET password_hash=excluded.password_hash, active=1
                """, encoder.encode("correct-password"));
        mvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                        .content("""
                                {"username":"auth.bad","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
