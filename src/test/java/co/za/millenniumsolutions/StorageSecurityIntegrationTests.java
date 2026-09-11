package co.za.millenniumsolutions;

import co.za.millenniumsolutions.model.PrivateObjectReference;
import co.za.millenniumsolutions.model.User;
import co.za.millenniumsolutions.repository.PrivateObjectReferenceRepository;
import co.za.millenniumsolutions.repository.UserRepository;
import co.za.millenniumsolutions.storage.LocalStorageAdapter;
import co.za.millenniumsolutions.storage.StorageObjectRequest;
import co.za.millenniumsolutions.storage.StorageObjectType;
import co.za.millenniumsolutions.storage.StoragePolicy;
import co.za.millenniumsolutions.storage.StorageUpload;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StorageSecurityIntegrationTests {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired PrivateObjectReferenceRepository objects;

    @Test
    void localAdapterGeneratesSeparateRandomPrivateNamespaces() {
        LocalStorageAdapter storage = new LocalStorageAdapter();
        String checksum = "b".repeat(64);

        StorageUpload evidence = storage.createUpload(new StorageObjectRequest(
                StorageObjectType.EVIDENCE, "application/pdf", 100, checksum));
        StorageUpload selfie = storage.createUpload(new StorageObjectRequest(
                StorageObjectType.ATTENDANCE_SELFIE, "image/jpeg", 100, checksum));

        assertThat(evidence.objectKey()).matches("evidence/[0-9a-f-]+");
        assertThat(selfie.objectKey()).matches("attendance-selfies/[0-9a-f-]+");
        assertThat(evidence.objectKey()).isNotEqualTo(selfie.objectKey());
        assertThat(evidence.uploadUrl()).startsWith("local-storage://");
        assertThat(evidence.presigned()).isFalse();
    }

    @Test
    void enforcesSeparateEvidenceAndSelfiePolicies() {
        String checksum = "c".repeat(64);
        StoragePolicy.validate(new StorageObjectRequest(
                StorageObjectType.ATTENDANCE_SELFIE, "image/png", 2 * 1024 * 1024, checksum));

        assertThatThrownBy(() -> StoragePolicy.validate(new StorageObjectRequest(
                StorageObjectType.ATTENDANCE_SELFIE, "application/pdf", 100, checksum)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StoragePolicy.validate(new StorageObjectRequest(
                StorageObjectType.ATTENDANCE_SELFIE, "image/jpeg", 2 * 1024 * 1024 + 1, checksum)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void privateObjectDownloadRequiresOwnerOrPrivilegedRole() throws Exception {
        jdbc.execute("DELETE FROM verification");
        jdbc.execute("DELETE FROM evidence_version");
        jdbc.execute("DELETE FROM evidence");
        jdbc.execute("DELETE FROM submission");
        jdbc.execute("DELETE FROM attendance_capture");
        jdbc.execute("DELETE FROM attendance_session");
        jdbc.execute("DELETE FROM work_entry_timing");
        jdbc.execute("DELETE FROM work_entry");
        jdbc.execute("DELETE FROM audit_log");
        jdbc.execute("DELETE FROM private_object_reference");
        jdbc.execute("DELETE FROM app_user");
        users.save(new User("u-owner", "owner", "Owner", "STUDENT", null, null, true));
        users.save(new User("u-other", "other", "Other", "STUDENT", null, null, true));
        users.save(new User("u-supervisor", "supervisor", "Supervisor", "SUPERVISOR", null, null, true));
        objects.save(new PrivateObjectReference("object-security", "evidence/private-key",
                "application/pdf", 10, "d".repeat(64), "EVIDENCE", "u-owner", Instant.now()));

        mockMvc.perform(get("/api/private-objects/object-security/download")
                        .param("actorId", "u-other"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/private-objects/object-security/download")
                        .param("actorId", "u-owner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("local-storage://evidence/private-key"));

        mockMvc.perform(get("/api/private-objects/object-security/download")
                        .param("actorId", "u-supervisor"))
                .andExpect(status().isOk());
    }
}
