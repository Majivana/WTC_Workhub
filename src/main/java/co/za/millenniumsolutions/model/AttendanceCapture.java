package co.za.millenniumsolutions.model;

import java.time.Instant;

import java.util.Objects;

public final class AttendanceCapture {
    private final String id;
    private final String attendanceSessionId;
    private final String captureType;
    private final Instant capturedAt;
    private final String privateObjectReferenceId;
    private final boolean locationValid;
    private final String locationResult;

    public AttendanceCapture(String id, String attendanceSessionId, String captureType, Instant capturedAt, String privateObjectReferenceId, boolean locationValid, String locationResult) {
        this.id = id;
        this.attendanceSessionId = attendanceSessionId;
        this.captureType = captureType;
        this.capturedAt = capturedAt;
        this.privateObjectReferenceId = privateObjectReferenceId;
        this.locationValid = locationValid;
        this.locationResult = locationResult;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String attendanceSessionId() { return attendanceSessionId; }
    public String getAttendanceSessionId() { return attendanceSessionId; }

    public String captureType() { return captureType; }
    public String getCaptureType() { return captureType; }

    public Instant capturedAt() { return capturedAt; }
    public Instant getCapturedAt() { return capturedAt; }

    public String privateObjectReferenceId() { return privateObjectReferenceId; }
    public String getPrivateObjectReferenceId() { return privateObjectReferenceId; }

    public boolean locationValid() { return locationValid; }
    public boolean isLocationValid() { return locationValid; }

    public String locationResult() { return locationResult; }
    public String getLocationResult() { return locationResult; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof AttendanceCapture other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(attendanceSessionId, other.attendanceSessionId) && Objects.equals(captureType, other.captureType) && Objects.equals(capturedAt, other.capturedAt) && Objects.equals(privateObjectReferenceId, other.privateObjectReferenceId) && locationValid == other.locationValid && Objects.equals(locationResult, other.locationResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attendanceSessionId, captureType, capturedAt, privateObjectReferenceId, locationValid, locationResult);
    }

    @Override
    public String toString() {
        return "AttendanceCapture[id=" + id + ", attendanceSessionId=" + attendanceSessionId + ", captureType=" + captureType + ", capturedAt=" + capturedAt + ", privateObjectReferenceId=" + privateObjectReferenceId + ", locationValid=" + locationValid + ", locationResult=" + locationResult + "]";
    }
}
