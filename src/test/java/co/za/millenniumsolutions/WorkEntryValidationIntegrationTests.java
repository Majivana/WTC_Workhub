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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WorkEntryValidationIntegrationTests {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired InstitutionRepository institutions;
    @Autowired CampusRepository campuses;
    @Autowired UserRepository users;
    @Autowired ActivityTypeRepository activities;
    @Autowired WorkPeriodRepository periods;

    @BeforeEach
    void setUp() {
        jdbc.execute("DELETE FROM work_entry_timing");
        jdbc.execute("DELETE FROM work_entry");
        jdbc.execute("DELETE FROM app_user");
        jdbc.execute("DELETE FROM activity_type");
        jdbc.execute("DELETE FROM work_period");
        jdbc.execute("DELETE FROM campus");
        jdbc.execute("DELETE FROM institution");

        institutions.save(new Institution("i-entry", "Entry Institution"));
        campuses.save(new Campus("c-entry", "i-entry", "Entry Campus"));
        users.save(new User("u-entry", "entry.user", "Entry User", "STUDENT", null, "c-entry", true));
        activities.save(new ActivityType("a-entry", "Entry Activity", true));
        periods.save(new WorkPeriod("p-entry", "Entry Period",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                new BigDecimal("18")));
    }

    @Test
    void calculatesDurationAfterBreakAndReturnsCreatedDraft() throws Exception {
        mockMvc.perform(post("/api/users/u-entry/work-periods/p-entry/work-entries")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "activityTypeId": "a-entry",
                                  "workDate": "2026-09-10",
                                  "startTime": "09:00",
                                  "endTime": "17:00",
                                  "breakMinutes": 60
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.durationMinutes").value(420))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.startTime").value("09:00:00"))
                .andExpect(jsonPath("$.endTime").value("17:00:00"));
    }

    @Test
    void rejectsInvalidRangeBreakOutsidePeriodAndOverlap() throws Exception {
        String invalidRange = """
                {"activityTypeId":"a-entry","workDate":"2026-09-10","startTime":"17:00","endTime":"09:00","breakMinutes":0}
                """;
        mockMvc.perform(post("/api/users/u-entry/work-periods/p-entry/work-entries")
                        .contentType(APPLICATION_JSON).content(invalidRange))
                .andExpect(status().isBadRequest());

        String invalidBreak = """
                {"activityTypeId":"a-entry","workDate":"2026-09-10","startTime":"09:00","endTime":"10:00","breakMinutes":60}
                """;
        mockMvc.perform(post("/api/users/u-entry/work-periods/p-entry/work-entries")
                        .contentType(APPLICATION_JSON).content(invalidBreak))
                .andExpect(status().isBadRequest());

        String outsidePeriod = """
                {"activityTypeId":"a-entry","workDate":"2026-10-01","startTime":"09:00","endTime":"10:00","breakMinutes":0}
                """;
        mockMvc.perform(post("/api/users/u-entry/work-periods/p-entry/work-entries")
                        .contentType(APPLICATION_JSON).content(outsidePeriod))
                .andExpect(status().isBadRequest());

        String valid = """
                {"activityTypeId":"a-entry","workDate":"2026-09-10","startTime":"09:00","endTime":"12:00","breakMinutes":30}
                """;
        mockMvc.perform(post("/api/users/u-entry/work-periods/p-entry/work-entries")
                        .contentType(APPLICATION_JSON).content(valid))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users/u-entry/work-periods/p-entry/work-entries")
                        .contentType(APPLICATION_JSON).content(valid))
                .andExpect(status().isBadRequest());

        String overlap = """
                {"activityTypeId":"a-entry","workDate":"2026-09-10","startTime":"11:00","endTime":"13:00","breakMinutes":0}
                """;
        mockMvc.perform(post("/api/users/u-entry/work-periods/p-entry/work-entries")
                        .contentType(APPLICATION_JSON).content(overlap))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingFieldsAndAllowsDraftEdits() throws Exception {
        mockMvc.perform(post("/api/users/u-entry/work-periods/p-entry/work-entries")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"workDate":"2026-09-10","startTime":"09:00","endTime":"10:00","breakMinutes":0}
                                """))
                .andExpect(status().isBadRequest());

        String response = mockMvc.perform(post("/api/users/u-entry/work-periods/p-entry/work-entries")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"activityTypeId":"a-entry","workDate":"2026-09-10","startTime":"09:00","endTime":"10:00","breakMinutes":0}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(put("/api/work-entries/" + id)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"activityTypeId":"a-entry","workDate":"2026-09-10","startTime":"09:00","endTime":"12:00","breakMinutes":30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(150));
    }
}
