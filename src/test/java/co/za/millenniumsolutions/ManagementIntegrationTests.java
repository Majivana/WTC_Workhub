package co.za.millenniumsolutions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IsolatedSqliteTest
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", authorities = {
        "PERM_USER_MANAGE", "PERM_ORGANIZATION_READ", "PERM_ORGANIZATION_MANAGE",
        "PERM_ASSIGNMENT_MANAGE"
})
class ManagementIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        jdbc.execute("PRAGMA foreign_keys = OFF");
        jdbc.execute("DELETE FROM auth_session");
        jdbc.execute("DELETE FROM app_user WHERE id LIKE 'management-%'");
        jdbc.execute("DELETE FROM app_user WHERE username LIKE 'management.%' OR username = 'bad.assignment'");
        jdbc.execute("DELETE FROM campus WHERE id LIKE 'management-%'");
        jdbc.execute("DELETE FROM institution WHERE id LIKE 'management-%'");
        jdbc.execute("PRAGMA foreign_keys = ON");
    }

    @Test
    void adminCanCreateAssignAndDeactivateUserWithoutDeletingHistory() throws Exception {
        jdbc.update("INSERT OR IGNORE INTO institution(id,name) VALUES (?,?)", "management-inst", "Management Institution");
        jdbc.update("INSERT OR IGNORE INTO campus(id,institution_id,name) VALUES (?,?,?)",
                "management-campus", "management-inst", "Management Campus");
        jdbc.update("INSERT OR IGNORE INTO work_role(id,code,name) VALUES (?,?,?)",
                "management-role", "MANAGEMENT_ROLE", "Management Role");
        jdbc.update("INSERT OR IGNORE INTO app_user(id,username,display_name,system_role,active,password_hash) " +
                        "VALUES (?,?,?,?,1,?)", "management-supervisor", "management.supervisor",
                "Management Supervisor", "SUPERVISOR", "{noop}unused");

        String body = """
                {"username":"management.student","displayName":"Management Student","systemRole":"STUDENT",
                 "password":"safe-password","institutionId":"management-inst","campusId":"management-campus",
                 "workRoleId":"management-role","mentorId":"management-supervisor",
                 "supervisorId":"management-supervisor"}
                """;
        String response = mvc.perform(post("/api/admin/users").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(response).get("id").asText();

        mvc.perform(post("/api/admin/users/" + id + "/deactivate"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/admin/users?includeInactive=true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id=='" + id + "')].active").value(false));
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM app_user WHERE id=?", Integer.class, id)).isEqualTo(1);
    }

    @Test
    void nonAdminCannotManageUsers() throws Exception {
        mvc.perform(get("/api/admin/users").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void mismatchedCampusInstitutionIsRejected() throws Exception {
        jdbc.update("INSERT OR IGNORE INTO institution(id,name) VALUES (?,?)", "management-inst-a", "Institution A");
        jdbc.update("INSERT OR IGNORE INTO institution(id,name) VALUES (?,?)", "management-inst-b", "Institution B");
        jdbc.update("INSERT OR IGNORE INTO campus(id,institution_id,name) VALUES (?,?,?)",
                "management-campus-b", "management-inst-b", "Campus B");
        mvc.perform(post("/api/admin/users").contentType(APPLICATION_JSON).content("""
                {"username":"bad.assignment","displayName":"Bad Assignment","systemRole":"STUDENT",
                 "password":"safe-password","institutionId":"management-inst-a","campusId":"management-campus-b"}
                """)).andExpect(status().isBadRequest());
    }
}
