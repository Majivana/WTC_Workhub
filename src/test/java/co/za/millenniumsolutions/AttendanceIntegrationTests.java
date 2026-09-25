package co.za.millenniumsolutions;

import co.za.millenniumsolutions.model.*;
import co.za.millenniumsolutions.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IsolatedSqliteTest
@SpringBootTest @AutoConfigureMockMvc
@WithMockUser(username = "user-student-demo", roles = "STUDENT")
class AttendanceIntegrationTests {
    @Autowired MockMvc mvc; @Autowired JdbcTemplate jdbc;
    @Autowired InstitutionRepository institutions; @Autowired CampusRepository campuses;
    @Autowired CampusGeofenceRepository fences; @Autowired UserRepository users;
    @Autowired WorkPeriodRepository periods;

    @BeforeEach void setup() {
        jdbc.execute("DELETE FROM attendance_correction"); jdbc.execute("DELETE FROM audit_log");
        jdbc.execute("DELETE FROM attendance_capture"); jdbc.execute("DELETE FROM attendance_session");
        jdbc.execute("DELETE FROM private_object_reference");         jdbc.execute("DELETE FROM notification");
        jdbc.execute("DELETE FROM app_user");
        jdbc.execute("DELETE FROM work_period"); jdbc.execute("DELETE FROM campus_geofence");
        jdbc.execute("DELETE FROM campus"); jdbc.execute("DELETE FROM institution");
        institutions.save(new Institution("i-att", "Attendance Institution"));
        campuses.save(new Campus("c-att", "i-att", "Attendance Campus"));
        fences.save(new CampusGeofence("g-att", "c-att", new BigDecimal("-33.9249"),
                new BigDecimal("18.4241"), 500, true));
        users.save(new User("u-att", "attendance.user", "Attendance User", "STUDENT", null, "c-att", true));
        periods.save(new WorkPeriod("p-att", "Attendance Period", LocalDate.of(2026,9,1),
                LocalDate.of(2026,9,30), new BigDecimal("18")));
    }

    @Test void clocksInOutWithServerDurationAndPrivateSelfies() throws Exception {
        String request = request("-33.9249", "18.4241");
        String body = mvc.perform(post("/api/attendance/clock-in").contentType(APPLICATION_JSON).content(request))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();
        String id = body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
        mvc.perform(post("/api/attendance/"+id+"/clock-out").contentType(APPLICATION_JSON).content(request))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.durationMinutes").isNumber());
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM private_object_reference WHERE purpose='ATTENDANCE_SELFIE'", Integer.class)).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_log WHERE entity_type='ATTENDANCE'", Integer.class)).isEqualTo(2);
    }

    @Test void rejectsDuplicateActiveSessionAndOutsideGeofence() throws Exception {
        String request = request("-33.9249", "18.4241");
        mvc.perform(post("/api/attendance/clock-in").contentType(APPLICATION_JSON).content(request))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/attendance/clock-in").contentType(APPLICATION_JSON).content(request))
                .andExpect(status().isConflict());
        String sessionId = jdbc.queryForObject("SELECT id FROM attendance_session WHERE status='ACTIVE'", String.class);
        mvc.perform(post("/api/attendance/"+sessionId+"/clock-out").contentType(APPLICATION_JSON).content(request))
                .andExpect(status().isOk());
        String outside = request("-34.0", "18.8");
        mvc.perform(post("/api/attendance/clock-in").contentType(APPLICATION_JSON).content(outside))
                .andExpect(status().isForbidden());
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_log WHERE action IN ('DUPLICATE_ACTIVE_SESSION','OUTSIDE_GEOFENCE')", Integer.class))
                .isGreaterThanOrEqualTo(1);
    }

    private String request(String lat, String lon) {
        return """
          {"userId":"u-att","campusId":"c-att","workPeriodId":"p-att",
           "mediaType":"image/jpeg","sizeBytes":100,"checksum":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
           "latitude":"%s","longitude":"%s"}""".formatted(lat, lon);
    }
}
