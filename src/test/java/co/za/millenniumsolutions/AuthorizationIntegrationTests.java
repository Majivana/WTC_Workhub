package co.za.millenniumsolutions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationIntegrationTests {
    @Autowired MockMvc mvc;

    @Test
    void anonymousBusinessRoutesAreDenied() throws Exception {
        mvc.perform(get("/api/admin/reports/work-summary.csv"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentsCannotUseAdminReport() throws Exception {
        mvc.perform(get("/api/admin/reports/work-summary.csv"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentsCannotApproveUnknownSubmission() throws Exception {
        mvc.perform(post("/api/submissions/not-owned/verifications")
                        .contentType("application/json")
                        .content("""
                                {"verifierId":"student","evidenceVersionId":"version",
                                 "action":"APPROVE","comment":"not allowed"}
                                """))
                .andExpect(status().isForbidden());
    }
}
