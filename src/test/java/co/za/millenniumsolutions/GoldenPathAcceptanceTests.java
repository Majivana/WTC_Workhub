package co.za.millenniumsolutions;

import co.za.millenniumsolutions.model.*;
import co.za.millenniumsolutions.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@IsolatedSqliteTest
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
class GoldenPathAcceptanceTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper objectMapper;
    @Autowired InstitutionRepository institutions;
    @Autowired CampusRepository campuses;
    @Autowired UserRepository users;
    @Autowired WorkPeriodRepository periods;
    @Autowired ActivityTypeRepository activities;
    @Autowired EvidenceVersionRepository evidenceVersions;
    @Autowired NotificationRepository notifications;

    @BeforeEach
    void reset() {
        clearData();

        institutions.save(new Institution("acceptance-i", "Acceptance Institution"));
        campuses.save(new Campus("acceptance-c", "acceptance-i", "Acceptance Campus"));
        users.save(new User("acceptance-supervisor", "golden.supervisor", "Golden Supervisor",
                "SUPERVISOR", null, "acceptance-i", "acceptance-c", null, null, true));
        users.save(new User("acceptance-student", "golden.student", "Golden Student",
                "STUDENT", null, "acceptance-i", "acceptance-c", null,
                "acceptance-supervisor", true));
        periods.save(new WorkPeriod("acceptance-p", "Acceptance Period",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), new BigDecimal("18")));
        activities.save(new ActivityType("acceptance-a", "Acceptance Activity", true));
    }

    @AfterEach
    void cleanup() {
        clearData();
    }

    private void clearData() {
        jdbc.execute("DELETE FROM attendance_correction");
        jdbc.execute("DELETE FROM attendance_capture");
        jdbc.execute("DELETE FROM attendance_session");
        jdbc.execute("DELETE FROM auth_session");
        jdbc.execute("DELETE FROM escalation");
        jdbc.execute("DELETE FROM verification");
        jdbc.execute("DELETE FROM notification");
        jdbc.execute("DELETE FROM audit_log");
        jdbc.execute("DELETE FROM evidence_version");
        jdbc.execute("DELETE FROM evidence");
        jdbc.execute("DELETE FROM private_object_reference");
        jdbc.execute("DELETE FROM submission");
        jdbc.execute("DELETE FROM work_entry_timing");
        jdbc.execute("DELETE FROM work_entry");
        jdbc.execute("DELETE FROM app_user");
        jdbc.execute("DELETE FROM activity_type");
        jdbc.execute("DELETE FROM work_period");
        jdbc.execute("DELETE FROM campus_geofence");
        jdbc.execute("DELETE FROM campus");
        jdbc.execute("DELETE FROM work_role");
        jdbc.execute("DELETE FROM institution");
    }

    @Test
    void studentSubmitsEvidenceSupervisorRequestsChangesThenApproves() throws Exception {
        MvcResult workEntryResult = mvc.perform(post("/api/users/acceptance-student/work-periods/acceptance-p/work-entries")
                        .with(user("acceptance-student").roles("STUDENT"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"activityTypeId":"acceptance-a","workDate":"2026-09-10",
                                 "startTime":"09:00","endTime":"11:00","breakMinutes":15}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.durationMinutes").value(105))
                .andReturn();
        String workEntryId = json(workEntryResult).get("id").asText();

        MvcResult evidenceResult = mvc.perform(post("/api/work-entries/" + workEntryId + "/evidence")
                        .with(user("acceptance-student").roles("STUDENT"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"objectKey":"ignored-by-storage","mediaType":"application/pdf","sizeBytes":1024,
                                 "checksum":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                                 "purpose":"Daily work evidence","createdBy":"acceptance-student",
                                 "changeNotes":"Initial evidence"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.checksum").value(
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
                .andExpect(jsonPath("$.changeNotes").value("Initial evidence"))
                .andReturn();
        JsonNode evidence = json(evidenceResult);
        String evidenceVersionId = evidenceVersions.findLatestId(evidence.get("evidenceId").asText()).orElseThrow();

        MvcResult submissionResult = mvc.perform(post("/api/work-entries/" + workEntryId + "/submissions")
                        .with(user("acceptance-student").roles("STUDENT")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();
        String submissionId = json(submissionResult).get("id").asText();

        transition(submissionId, "SUBMITTED", "acceptance-student",
                user("acceptance-student").roles("STUDENT"));
        transition(submissionId, "UNDER_REVIEW", "acceptance-student",
                user("acceptance-student").roles("STUDENT"));

        mvc.perform(post("/api/submissions/" + submissionId + "/verifications")
                        .with(user("acceptance-supervisor").roles("SUPERVISOR"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"verifierId":"acceptance-supervisor","evidenceVersionId":"%s",
                                 "action":"REQUEST_CHANGES","comment":"Please add a clearer work summary."}
                                """.formatted(evidenceVersionId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.action").value("REQUEST_CHANGES"))
                .andExpect(jsonPath("$.evidenceVersionId").value(evidenceVersionId));

        transition(submissionId, "RESUBMITTED", "acceptance-student",
                user("acceptance-student").roles("STUDENT"));
        transition(submissionId, "UNDER_REVIEW", "acceptance-student",
                user("acceptance-student").roles("STUDENT"));

        mvc.perform(post("/api/submissions/" + submissionId + "/verifications")
                        .with(user("acceptance-supervisor").roles("SUPERVISOR"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"verifierId":"acceptance-supervisor","evidenceVersionId":"%s",
                                 "action":"APPROVE","comment":"Evidence and work entry approved."}
                                """.formatted(evidenceVersionId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.action").value("APPROVE"));

        mvc.perform(get("/api/submissions/" + submissionId)
                        .with(user("acceptance-student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mvc.perform(get("/api/submissions/" + submissionId + "/verifications")
                        .with(user("acceptance-student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].evidenceVersionId").value(evidenceVersionId))
                .andExpect(jsonPath("$[1].evidenceVersionId").value(evidenceVersionId));

        mvc.perform(get("/api/users/acceptance-student/notifications")
                        .with(user("acceptance-student").roles("STUDENT")))
                .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].type").value("SUBMISSION_STATUS"));

        mvc.perform(get("/api/users/acceptance-student/work-periods/acceptance-p/progress")
                        .with(user("acceptance-student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loggedHours").value(1.75))
                .andExpect(jsonPath("$.verifiedHours").value(1.75))
                .andExpect(jsonPath("$.status").value("BELOW_TARGET"));

        assertThat(notifications.findByRecipient("acceptance-supervisor"))
                .extracting(Notification::type)
                .contains("SUBMISSION_STATUS");
    }

    private void transition(String submissionId, String status, String actorId,
                            RequestPostProcessor user) throws Exception {
        mvc.perform(post("/api/submissions/" + submissionId + "/transitions")
                        .with(user)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"status":"%s","actorId":"%s"}
                                """.formatted(status, actorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(status));
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
