package co.za.millenniumsolutions;

import co.za.millenniumsolutions.model.ActivityType;
import co.za.millenniumsolutions.model.Campus;
import co.za.millenniumsolutions.model.Institution;
import co.za.millenniumsolutions.model.User;
import co.za.millenniumsolutions.model.WorkEntry;
import co.za.millenniumsolutions.model.WorkPeriod;
import co.za.millenniumsolutions.repository.ActivityTypeRepository;
import co.za.millenniumsolutions.repository.CampusRepository;
import co.za.millenniumsolutions.repository.InstitutionRepository;
import co.za.millenniumsolutions.repository.UserRepository;
import co.za.millenniumsolutions.repository.WorkEntryRepository;
import co.za.millenniumsolutions.repository.WorkPeriodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IsolatedSqliteTest
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "user-student-demo", roles = "STUDENT")
class SubmissionStateMachineIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired InstitutionRepository institutions;
    @Autowired CampusRepository campuses;
    @Autowired UserRepository users;
    @Autowired WorkPeriodRepository periods;
    @Autowired ActivityTypeRepository activities;
    @Autowired WorkEntryRepository entries;

    @BeforeEach
    void setUp() {
        jdbc.execute("DELETE FROM audit_log");
        jdbc.execute("DELETE FROM submission");
        jdbc.execute("DELETE FROM work_entry_timing");
        jdbc.execute("DELETE FROM work_entry");
        jdbc.execute("DELETE FROM notification");
        jdbc.execute("DELETE FROM app_user");
        jdbc.execute("DELETE FROM activity_type");
        jdbc.execute("DELETE FROM work_period");
        jdbc.execute("DELETE FROM campus");
        jdbc.execute("DELETE FROM institution");

        institutions.save(new Institution("i-submission", "Submission Institution"));
        campuses.save(new Campus("c-submission", "i-submission", "Submission Campus"));
        users.save(new User("u-submission", "submission.user", "Submission User",
                "STUDENT", null, "c-submission", true));
        periods.save(new WorkPeriod("p-submission", "Submission Period",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), new BigDecimal("18")));
        activities.save(new ActivityType("a-submission", "Submission Activity", true));
        entries.save(new WorkEntry("w-submission", "u-submission", "p-submission",
                "a-submission", LocalDate.of(2026, 9, 10), 60, "DRAFT"));
    }

    @Test
    void enforcesLifecycleAndSupportsResubmissionAfterRequestedChanges() throws Exception {
        String body = mockMvc.perform(post("/api/work-entries/w-submission/submissions")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        String id = body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        transition(id, "SUBMITTED", "u-submission", "SUBMITTED");
        transition(id, "UNDER_REVIEW", "u-supervisor", "UNDER_REVIEW");
        transition(id, "CHANGES_REQUESTED", "u-supervisor", "CHANGES_REQUESTED");
        transition(id, "RESUBMITTED", "u-submission", "RESUBMITTED");
        transition(id, "UNDER_REVIEW", "u-supervisor", "UNDER_REVIEW");
        transition(id, "APPROVED", "u-supervisor", "APPROVED");
    }

    @Test
    void rejectsInvalidTransitionsExplicitly() throws Exception {
        String body = mockMvc.perform(post("/api/work-entries/w-submission/submissions"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/submissions/" + id + "/transitions")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"status":"APPROVED","actorId":"u-supervisor"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_SUBMISSION_TRANSITION"));
    }

    @Test
    void approvedSubmissionCannotBeChangedSilently() throws Exception {
        String body = mockMvc.perform(post("/api/work-entries/w-submission/submissions"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
        transition(id, "SUBMITTED", "u-submission", "SUBMITTED");
        transition(id, "UNDER_REVIEW", "u-supervisor", "UNDER_REVIEW");
        transition(id, "APPROVED", "u-supervisor", "APPROVED");

        mockMvc.perform(post("/api/submissions/" + id + "/transitions")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"status":"CHANGES_REQUESTED","actorId":"u-supervisor"}
                                """))
                .andExpect(status().isConflict());
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT status FROM submission WHERE id = ?", String.class, id)).isEqualTo("APPROVED");
    }

    private void transition(String id, String target, String actorId, String expected) throws Exception {
        mockMvc.perform(post("/api/submissions/" + id + "/transitions")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"status":"%s","actorId":"%s"}
                                """.formatted(target, actorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(expected));
    }
}
