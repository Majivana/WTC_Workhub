package co.za.millenniumsolutions;

import co.za.millenniumsolutions.model.*;
import co.za.millenniumsolutions.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@WithMockUser(username = "golden.student", roles = "STUDENT")
class ApiGoldenPathIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired InstitutionRepository institutions;
    @Autowired CampusRepository campuses;
    @Autowired UserRepository users;
    @Autowired WorkPeriodRepository periods;
    @Autowired ActivityTypeRepository activities;

    @BeforeEach
    void reset() {
        jdbc.execute("DELETE FROM work_entry_timing");
        jdbc.execute("DELETE FROM work_entry");
        jdbc.execute("DELETE FROM app_user");
        jdbc.execute("DELETE FROM activity_type");
        jdbc.execute("DELETE FROM work_period");
        jdbc.execute("DELETE FROM campus");
        jdbc.execute("DELETE FROM institution");
        institutions.save(new Institution("api-i", "API Institution"));
        campuses.save(new Campus("api-c", "api-i", "API Campus"));
        users.save(new User("api-u", "golden.student", "Golden Student", "STUDENT", null, "api-c", true));
        periods.save(new WorkPeriod("api-p", "API Period", LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30), new BigDecimal("18")));
        activities.save(new ActivityType("api-a", "API Activity", true));
    }

    @Test
    void goldenPathCreatesWorkEntryAndReturnsProgressShape() throws Exception {
        String response = mvc.perform(post("/api/users/api-u/work-periods/api-p/work-entries")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"activityTypeId":"api-a","workDate":"2026-09-10",
                                 "startTime":"09:00","endTime":"11:00","breakMinutes":15}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.userId").value("api-u"))
                .andExpect(jsonPath("$.durationMinutes").value(105))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(get("/api/users/api-u/work-periods/api-p/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetHours").value(18))
                .andExpect(jsonPath("$.loggedHours").value(1.75))
                .andExpect(jsonPath("$.status").value("BELOW_TARGET"));
    }

    @Test
    void malformedWorkEntryReturnsStableErrorShape() throws Exception {
        mvc.perform(post("/api/users/api-u/work-periods/api-p/work-entries")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_WORK_ENTRY"))
                .andExpect(jsonPath("$.message").isString());
    }
}
