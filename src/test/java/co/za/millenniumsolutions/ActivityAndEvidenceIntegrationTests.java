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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ActivityAndEvidenceIntegrationTests {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired InstitutionRepository institutions;
    @Autowired CampusRepository campuses;
    @Autowired UserRepository users;
    @Autowired WorkPeriodRepository periods;
    @Autowired WorkEntryRepository entries;
    @Autowired ActivityTypeRepository activities;

    @BeforeEach
    void setUp() {
        jdbc.execute("DELETE FROM evidence_version");
        jdbc.execute("DELETE FROM evidence");
        jdbc.execute("DELETE FROM private_object_reference");
        jdbc.execute("DELETE FROM work_entry_timing");
        jdbc.execute("DELETE FROM work_entry");
        jdbc.execute("DELETE FROM app_user");
        jdbc.execute("DELETE FROM activity_type");
        jdbc.execute("DELETE FROM work_period");
        jdbc.execute("DELETE FROM campus");
        jdbc.execute("DELETE FROM institution");

        institutions.save(new Institution("i-activity", "Activity Institution"));
        campuses.save(new Campus("c-activity", "i-activity", "Activity Campus"));
        users.save(new User("u-activity", "activity.user", "Activity User",
                "STUDENT", null, "c-activity", true));
        periods.save(new WorkPeriod("p-activity", "Activity Period",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), new BigDecimal("18")));
    }

    @Test
    void supportsActivityCrudAndPreservesReferencedActivityOnDelete() throws Exception {
        String response = mockMvc.perform(post("/api/activity-types")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Student Support"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();
        String activityId = response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        entries.save(new WorkEntry("w-activity", "u-activity", "p-activity", activityId,
                LocalDate.of(2026, 9, 10), 60, "DRAFT"));

        mockMvc.perform(delete("/api/activity-types/" + activityId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/activity-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/activity-types?includeInactive=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(activityId))
                .andExpect(jsonPath("$[0].active").value(false));
        org.assertj.core.api.Assertions.assertThat(activities.findById(activityId)).isPresent();
    }

    @Test
    void storesEvidenceMetadataAndCreatesVersionsWithoutFileContent() throws Exception {
        activities.save(new ActivityType("a-evidence", "Evidence Activity", true));
        entries.save(new WorkEntry("w-evidence", "u-activity", "p-activity", "a-evidence",
                LocalDate.of(2026, 9, 10), 60, "DRAFT"));
        String checksum = "a".repeat(64);
        String request = """
                {
                  "objectKey": " evidence/work-entry-1.pdf ",
                  "mediaType": "APPLICATION/PDF",
                  "sizeBytes": 256,
                  "checksum": "%s",
                  "purpose": " Daily evidence ",
                  "createdBy": "u-activity"
                }
                """.formatted(checksum);

        mockMvc.perform(post("/api/work-entries/w-evidence/evidence")
                        .contentType(APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.objectKey").value("evidence/work-entry-1.pdf"))
                .andExpect(jsonPath("$.mediaType").value("application/pdf"))
                .andExpect(jsonPath("$.checksum").value(checksum))
                .andExpect(jsonPath("$.purpose").value("Daily evidence"));

        mockMvc.perform(post("/api/work-entries/w-evidence/evidence")
                        .contentType(APPLICATION_JSON)
                        .content(request.replace("work-entry-1.pdf", "work-entry-1-v2.pdf")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(2));

        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM evidence_version", Integer.class)).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM private_object_reference", Integer.class)).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'evidence_content'",
                Integer.class)).isZero();
    }

    @Test
    void rejectsInvalidEvidenceMetadata() throws Exception {
        activities.save(new ActivityType("a-invalid-evidence", "Invalid Evidence Activity", true));
        entries.save(new WorkEntry("w-invalid-evidence", "u-activity", "p-activity",
                "a-invalid-evidence", LocalDate.of(2026, 9, 10), 60, "DRAFT"));

        mockMvc.perform(post("/api/work-entries/w-invalid-evidence/evidence")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "objectKey": "../outside.pdf",
                                  "mediaType": "text/plain",
                                  "sizeBytes": 0,
                                  "checksum": "not-a-sha",
                                  "purpose": "Evidence"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_EVIDENCE_METADATA"));
    }
}
