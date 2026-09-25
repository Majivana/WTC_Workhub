package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.api.AttendanceCaptureRequest;
import co.za.millenniumsolutions.model.*;
import co.za.millenniumsolutions.repository.*;
import co.za.millenniumsolutions.storage.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class AttendanceService {
    private final UserRepository users;
    private final CampusGeofenceRepository geofences;
    private final AttendanceSessionRepository sessions;
    private final AttendanceCaptureRepository captures;
    private final PrivateObjectReferenceRepository objects;
    private final StoragePort storage;
    private final AttendanceAuditService audits;
    private final JdbcTemplate jdbc;

    public AttendanceService(UserRepository users, CampusGeofenceRepository geofences,
                             AttendanceSessionRepository sessions, AttendanceCaptureRepository captures,
                             PrivateObjectReferenceRepository objects, StoragePort storage,
                             AttendanceAuditService audits, JdbcTemplate jdbc) {
        this.users=users; this.geofences=geofences; this.sessions=sessions; this.captures=captures;
        this.objects=objects; this.storage=storage; this.audits=audits; this.jdbc=jdbc;
    }

    public AttendanceSession clockIn(AttendanceCaptureRequest request) {
        User user = user(request.userId());
        if (request.campusId() == null || !request.campusId().equals(user.campusId())) {
            fail(user.id(), "CAMPUS_NOT_ASSIGNED");
            throw new SecurityException("User is not assigned to the requested campus");
        }
        if (sessions.findActiveByUserId(user.id()).isPresent()) {
            fail(user.id(), "DUPLICATE_ACTIVE_SESSION");
            throw new IllegalStateException("User already has an active attendance session");
        }
        CampusGeofence fence = geofence(request.campusId());
        validateLocation(request, fence, user);
        Instant now = Instant.now();
        String sessionId = UUID.randomUUID().toString();
        AttendanceSession session = sessions.save(new AttendanceSession(sessionId, user.id(), request.campusId(),
                request.workPeriodId(), now, null, null, "ACTIVE", null));
        captures.save(capture(sessionId, "CLOCK_IN", request, fence, user, now));
        audit(user.id(), "CLOCK_IN", sessionId, "campusId=" + request.campusId());
        return session;
    }

    public AttendanceSession clockOut(String sessionId, AttendanceCaptureRequest request) {
        AttendanceSession session = sessions.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Unknown attendance session: " + sessionId));
        if (!"ACTIVE".equals(session.status())) throw new IllegalStateException("Attendance session is not active");
        if (!session.userId().equals(request.userId())) throw new SecurityException("Session belongs to another user");
        User user = user(request.userId());
        CampusGeofence fence = geofence(session.campusId());
        validateLocation(request, fence, user);
        Instant now = Instant.now();
        if (!now.isAfter(session.clockInAt())) throw new IllegalStateException("Clock-out must follow clock-in");
        int minutes = (int) Duration.between(session.clockInAt(), now).toMinutes();
        String reconciliation = reconcile(session.userId(), session.workPeriodId(), minutes);
        AttendanceSession closed = sessions.save(new AttendanceSession(session.id(), session.userId(),
                session.campusId(), session.workPeriodId(), session.clockInAt(), now, minutes, "COMPLETED",
                reconciliation));
        captures.save(capture(session.id(), "CLOCK_OUT", request, fence, user, now));
        audit(user.id(), "CLOCK_OUT", session.id(), "durationMinutes=" + minutes + ",reconciliation=" + reconciliation);
        return closed;
    }

    public AttendanceSession correct(String sessionId, String actorId, Instant newClockIn, Instant newClockOut,
                                     String reason) {
        User actor = user(actorId);
        if (!"SUPERVISOR".equalsIgnoreCase(actor.systemRole()) && !"ADMIN".equalsIgnoreCase(actor.systemRole()))
            throw new SecurityException("Only supervisors or administrators may correct attendance");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Correction reason is required");
        AttendanceSession old = sessions.findById(sessionId).orElseThrow(() -> new NoSuchElementException("Unknown attendance session: " + sessionId));
        if (newClockIn == null || newClockOut == null || !newClockOut.isAfter(newClockIn))
            throw new IllegalArgumentException("Corrected timestamps are invalid");
        int minutes = (int) Duration.between(newClockIn, newClockOut).toMinutes();
        AttendanceSession updated = sessions.save(new AttendanceSession(old.id(), old.userId(), old.campusId(),
                old.workPeriodId(), newClockIn, newClockOut, minutes, "CORRECTED", old.reconciliationReference()));
        jdbc.update("INSERT INTO attendance_correction(id,attendance_session_id,actor_id,reason,old_clock_in_at,old_clock_out_at,new_clock_in_at,new_clock_out_at) VALUES (?,?,?,?,?,?,?,?)",
                UUID.randomUUID().toString(), sessionId, actor.id(), reason.trim(), old.clockInAt().toString(),
                old.clockOutAt()==null?null:old.clockOutAt().toString(), newClockIn.toString(), newClockOut.toString());
        audit(actor.id(), "ATTENDANCE_CORRECTED", sessionId, reason.trim());
        return updated;
    }

    private AttendanceCapture capture(String sessionId, String type, AttendanceCaptureRequest r,
                                      CampusGeofence fence, User user, Instant now) {
        StoragePolicy.validate(new StorageObjectRequest(StorageObjectType.ATTENDANCE_SELFIE,
                r.mediaType(), r.sizeBytes(), r.checksum()));
        StorageUpload upload = storage.createUpload(new StorageObjectRequest(StorageObjectType.ATTENDANCE_SELFIE,
                r.mediaType().trim().toLowerCase(), r.sizeBytes(), r.checksum().trim().toLowerCase()));
        String objectId = UUID.randomUUID().toString();
        objects.save(new PrivateObjectReference(objectId, upload.objectKey(), r.mediaType().trim().toLowerCase(),
                r.sizeBytes(), r.checksum().trim().toLowerCase(), "ATTENDANCE_SELFIE", user.id(), now));
        if (r.imageBase64() != null && !r.imageBase64().isBlank()) {
            byte[] image;
            try {
                image = Base64.getDecoder().decode(r.imageBase64());
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Selfie image data is invalid");
            }
            if (image.length != r.sizeBytes() || !sha256(image).equalsIgnoreCase(r.checksum())) {
                throw new IllegalArgumentException("Selfie image does not match its size and checksum metadata");
            }
            storage.storeObject(upload.objectKey(), image, r.mediaType().trim().toLowerCase());
        }
        return new AttendanceCapture(UUID.randomUUID().toString(), sessionId, type, now, objectId, true,
                "WITHIN_GEOFENCE:" + distanceMetres(r.latitude(), r.longitude(), fence));
    }

    private void validateLocation(AttendanceCaptureRequest r, CampusGeofence fence, User user) {
        if (r.latitude()==null || r.longitude()==null) { fail(user.id(), "LOCATION_MISSING"); throw new IllegalArgumentException("Location is required"); }
        StoragePolicy.validate(new StorageObjectRequest(StorageObjectType.ATTENDANCE_SELFIE, r.mediaType(), r.sizeBytes(), r.checksum()));
        double distance = distanceMetres(r.latitude(), r.longitude(), fence);
        if (distance > fence.radiusMetres()) { fail(user.id(), "OUTSIDE_GEOFENCE"); throw new SecurityException("Location is outside the approved campus geofence"); }
    }
    private double distanceMetres(BigDecimal lat, BigDecimal lon, CampusGeofence f) {
        double earth=6371000, p1=Math.toRadians(lat.doubleValue()), p2=Math.toRadians(f.latitude().doubleValue());
        double dp=Math.toRadians(f.latitude().doubleValue()-lat.doubleValue()), dl=Math.toRadians(f.longitude().doubleValue()-lon.doubleValue());
        double a=Math.sin(dp/2)*Math.sin(dp/2)+Math.cos(p1)*Math.cos(p2)*Math.sin(dl/2)*Math.sin(dl/2);
        return earth*2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
    }
    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
    private String reconcile(String userId, String periodId, int minutes) {
        Integer entries=jdbc.queryForObject("SELECT COUNT(*) FROM work_entry WHERE user_id=? AND work_period_id=? AND duration_minutes>0",Integer.class,userId,periodId);
        Integer evidence=jdbc.queryForObject("SELECT COUNT(*) FROM evidence e JOIN work_entry w ON w.id=e.work_entry_id WHERE w.user_id=? AND w.work_period_id=?",Integer.class,userId,periodId);
        Integer verified=jdbc.queryForObject("SELECT COUNT(*) FROM submission s JOIN work_entry w ON w.id=s.work_entry_id WHERE w.user_id=? AND w.work_period_id=? AND s.status='APPROVED'",Integer.class,userId,periodId);
        return "entries="+entries+",evidence="+evidence+",verified="+verified+",attendanceMinutes="+minutes;
    }
    private User user(String id){return users.findById(id).filter(User::active).orElseThrow(()->new NoSuchElementException("Unknown or inactive user: "+id));}
    private CampusGeofence geofence(String id){return geofences.findActiveByCampusId(id).orElseThrow(()->new IllegalStateException("No active campus geofence configured")); }
    private void fail(String actor,String action){audit(actor, action, UUID.randomUUID().toString(), "failed=true");}
    private void audit(String actor,String action,String entity,String details){audits.record(actor, action, entity, details);}
}
