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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProgressApiIntegrationTests {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired InstitutionRepository institutions;
    @Autowired CampusRepository campuses;
    @Autowired UserRepository users;
    @Autowired ActivityTypeRepository activities;
    @Autowired WorkPeriodRepository periods;
    @Autowired WorkEntryRepository entries;

    @BeforeEach
    void setUp() {
        jdbc.execute("DELETE FROM audit_log");
        jdbc.execute("DELETE FROM verification");
        jdbc.execute("DELETE FROM submission");
        jdbc.execute("DELETE FROM evidence_version");
        jdbc.execute("DELETE FROM evidence");
        jdbc.execute("DELETE FROM attendance_capture");
        jdbc.execute("DELETE FROM private_object_reference");
        jdbc.execute("DELETE FROM attendance_session");
        jdbc.execute("DELETE FROM work_entry");
        jdbc.execute("DELETE FROM app_user");
        jdbc.execute("DELETE FROM activity_type");
        jdbc.execute("DELETE FROM work_period");
        jdbc.execute("DELETE FROM campus_geofence");
        jdbc.execute("DELETE FROM campus");
        jdbc.execute("DELETE FROM work_role");
        jdbc.execute("DELETE FROM institution");

        institutions.save(new Institution("i-api", "API Institution"));
        campuses.save(new Campus("c-api", "i-api", "API Campus"));
        users.save(new User("u-api", "api.user", "API User", "STUDENT", null, "c-api", true));
        activities.save(new ActivityType("a-api", "API Activity", true));
        periods.save(new WorkPeriod("p-api", "API Period", LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30), new BigDecimal("10")));
        entries.save(new WorkEntry("w-api", "u-api", "p-api", "a-api",
                LocalDate.of(2026, 9, 10), 120, "VERIFIED"));
    }

    @Test
    void returnsProgressAsJsonObject() throws Exception {
        mockMvc.perform(get("/api/users/u-api/work-periods/p-api/progress"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.targetHours").value(10))
                .andExpect(jsonPath("$.loggedHours").value(2))
                .andExpect(jsonPath("$.verifiedHours").value(2))
                .andExpect(jsonPath("$.pendingHours").value(0))
                .andExpect(jsonPath("$.remainingHours").value(8))
                .andExpect(jsonPath("$.percentage").value(20))
                .andExpect(jsonPath("$.status").value("BELOW_TARGET"));
    }

    @Test
    void returnsNotFoundForUnknownWorkPeriod() throws Exception {
        mockMvc.perform(get("/api/users/u-api/work-periods/missing/progress"))
                .andExpect(status().isNotFound());
    }
}
