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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
class WorkEntryAuthorizationIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired InstitutionRepository institutions;
    @Autowired CampusRepository campuses;
    @Autowired UserRepository users;
    @Autowired WorkPeriodRepository periods;
    @Autowired ActivityTypeRepository activities;
    @Autowired WorkEntryRepository entries;

    @BeforeEach
    void reset() {
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

        institutions.save(new Institution("authorization-i", "Authorization Institution"));
        campuses.save(new Campus("authorization-c", "authorization-i", "Authorization Campus"));
        users.save(new User("auth-student", "auth.student", "Authorizing Student",
                "STUDENT", null, "authorization-i", "authorization-c", null, null, true));
        users.save(new User("other-student", "other.student", "Other Student",
                "STUDENT", null, "authorization-i", "authorization-c", null, null, true));
        periods.save(new WorkPeriod("authorization-p", "Authorization Period",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), new BigDecimal("18")));
        activities.save(new ActivityType("authorization-a", "Authorization Activity", true));
    }

    @Test
    @WithMockUser(username = "auth-student",
            authorities = {"ROLE_STUDENT", "PERM_WORK_ENTRY_MANAGE"})
    void studentCannotUpdateAnotherStudentsDraftWorkEntry() throws Exception {
        WorkEntry target = entries.save(new WorkEntry("other-entry", "other-student", "authorization-p",
                "authorization-a", LocalDate.of(2026, 9, 10), 60, "DRAFT"));

        mvc.perform(put("/api/work-entries/" + target.id())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"activityTypeId":"authorization-a","workDate":"2026-09-10",
                                 "startTime":"10:00","endTime":"12:00","breakMinutes":0}
                                """))
                .andExpect(status().isForbidden());
    }
}
