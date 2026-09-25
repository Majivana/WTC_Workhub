package co.za.millenniumsolutions;

import co.za.millenniumsolutions.model.*;
import co.za.millenniumsolutions.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "user-supervisor-demo", roles = "SUPERVISOR")
class VerificationIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired InstitutionRepository institutions;
    @Autowired CampusRepository campuses;
    @Autowired UserRepository users;
    @Autowired WorkPeriodRepository periods;
    @Autowired ActivityTypeRepository activities;
    @Autowired WorkEntryRepository entries;
    @Autowired EvidenceRepository evidence;
    @Autowired EvidenceVersionRepository versions;
    @Autowired PrivateObjectReferenceRepository objects;

    @BeforeEach
    void setUp() {
        jdbc.execute("DELETE FROM audit_log");
        jdbc.execute("DELETE FROM verification");
        jdbc.execute("DELETE FROM submission");
        jdbc.execute("DELETE FROM evidence_version");
        jdbc.execute("DELETE FROM evidence");
        jdbc.execute("DELETE FROM private_object_reference");
        jdbc.execute("DELETE FROM work_entry");
        jdbc.execute("DELETE FROM notification");
        jdbc.execute("DELETE FROM app_user");
        jdbc.execute("DELETE FROM activity_type");
        jdbc.execute("DELETE FROM work_period");
        jdbc.execute("DELETE FROM campus");
        jdbc.execute("DELETE FROM institution");

        institutions.save(new Institution("i-verification", "Verification Institution"));
        campuses.save(new Campus("c-verification", "i-verification", "Verification Campus"));
        users.save(new User("u-worker", "worker", "Worker", "STUDENT",
                null, "c-verification", true));
        users.save(new User("u-supervisor", "supervisor", "Supervisor", "SUPERVISOR",
                null, "c-verification", true));
        periods.save(new WorkPeriod("p-verification", "Verification Period",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), new BigDecimal("18")));
        activities.save(new ActivityType("a-verification", "Verification Activity", true));
        entries.save(new WorkEntry("w-verification", "u-worker", "p-verification",
                "a-verification", LocalDate.of(2026, 9, 10), 60, "DRAFT"));
        objects.save(new PrivateObjectReference("o-verification", "evidence/test-object",
                "application/pdf", 100, "a".repeat(64), "DAILY_EVIDENCE", "u-worker", Instant.now()));
        evidence.save(new Evidence("e-verification", "w-verification", "DRAFT"));
        versions.save(new EvidenceVersion("ev-verification", "e-verification", 1,
                "o-verification", "a".repeat(64), "Initial", Instant.now()));
    }

    @Test
    void recordsImmutableVerificationAndLinksReviewedEvidenceVersion() throws Exception {
        String submissionId = createSubmission();
        transition(submissionId, "SUBMITTED", "u-worker");
        transition(submissionId, "UNDER_REVIEW", "u-supervisor");

        mockMvc.perform(post("/api/submissions/" + submissionId + "/verifications")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"verifierId":"u-supervisor","evidenceVersionId":"ev-verification","action":"APPROVE","comment":"Evidence verified"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.submissionId").value(submissionId))
                .andExpect(jsonPath("$.evidenceVersionId").value("ev-verification"))
                .andExpect(jsonPath("$.action").value("APPROVE"))
                .andExpect(jsonPath("$.comment").value("Evidence verified"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/submissions/" + submissionId + "/verifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].verifierId").value("u-supervisor"))
                .andExpect(jsonPath("$[0].comment").value("Evidence verified"));

        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT status FROM submission WHERE id = ?", String.class, submissionId))
                .isEqualTo("APPROVED");
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_log WHERE entity_type = 'VERIFICATION'", Integer.class))
                .isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM verification WHERE submission_id = ?", Integer.class, submissionId))
                .isEqualTo(1);
    }

    @Test
    void rejectsWorkerAndSecondDecision() throws Exception {
        String submissionId = createSubmission();
        transition(submissionId, "SUBMITTED", "u-worker");
        transition(submissionId, "UNDER_REVIEW", "u-supervisor");

        mockMvc.perform(post("/api/submissions/" + submissionId + "/verifications")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"verifierId":"u-worker","evidenceVersionId":"ev-verification","action":"APPROVE","comment":"Worker cannot verify"}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/submissions/" + submissionId + "/verifications")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"verifierId":"u-supervisor","evidenceVersionId":"ev-verification","action":"REQUEST_CHANGES","comment":"Please add supporting detail"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/submissions/" + submissionId + "/verifications")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"verifierId":"u-supervisor","evidenceVersionId":"ev-verification","action":"APPROVE","comment":"Second decision is not allowed"}
                                """))
                .andExpect(status().isConflict());
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM verification WHERE submission_id = ?", Integer.class, submissionId))
                .isEqualTo(1);
    }

    private String createSubmission() throws Exception {
        String body = mockMvc.perform(post("/api/work-entries/w-verification/submissions"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
    }

    private void transition(String id, String target, String actor) throws Exception {
        mockMvc.perform(post("/api/submissions/" + id + "/transitions")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"status":"%s","actorId":"%s"}
                                """.formatted(target, actor)))
                .andExpect(status().isOk());
    }
}
