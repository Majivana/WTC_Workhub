package co.za.millenniumsolutions.repository;

import co.za.millenniumsolutions.model.AttendanceSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class AttendanceSessionRepository extends RepositorySupport {
    public AttendanceSessionRepository(JdbcTemplate j) { super(j); }

    public AttendanceSession save(AttendanceSession x) {
        update("INSERT INTO attendance_session(id,user_id,campus_id,work_period_id,clock_in_at,clock_out_at," +
                        "duration_minutes,status,reconciliation_reference) VALUES (?,?,?,?,?,?,?,?,?) " +
                        "ON CONFLICT(id) DO UPDATE SET clock_out_at=excluded.clock_out_at," +
                        "duration_minutes=excluded.duration_minutes,status=excluded.status," +
                        "reconciliation_reference=excluded.reconciliation_reference",
                x.id(), x.userId(), x.campusId(), x.workPeriodId(),
                x.clockInAt() == null ? null : x.clockInAt().toString(),
                x.clockOutAt() == null ? null : x.clockOutAt().toString(),
                x.durationMinutes(), x.status(), x.reconciliationReference());
        return x;
    }

    public Optional<AttendanceSession> findById(String id) {
        return jdbc.query("SELECT id,user_id,campus_id,work_period_id,clock_in_at,clock_out_at," +
                        "duration_minutes,status,reconciliation_reference FROM attendance_session WHERE id = ?",
                (rs, rowNum) -> new AttendanceSession(rs.getString("id"), rs.getString("user_id"),
                        rs.getString("campus_id"), rs.getString("work_period_id"),
                        Instant.parse(rs.getString("clock_in_at")),
                        rs.getString("clock_out_at") == null ? null : Instant.parse(rs.getString("clock_out_at")),
                        (Integer) rs.getObject("duration_minutes"), rs.getString("status"),
                        rs.getString("reconciliation_reference")), id).stream().findFirst();
    }

    public Optional<AttendanceSession> findActiveByUserId(String userId) {
        return jdbc.query("SELECT id,user_id,campus_id,work_period_id,clock_in_at,clock_out_at," +
                        "duration_minutes,status,reconciliation_reference FROM attendance_session " +
                        "WHERE user_id = ? AND status = 'ACTIVE'",
                (rs, rowNum) -> new AttendanceSession(rs.getString("id"), rs.getString("user_id"),
                        rs.getString("campus_id"), rs.getString("work_period_id"),
                        Instant.parse(rs.getString("clock_in_at")),
                        rs.getString("clock_out_at") == null ? null : Instant.parse(rs.getString("clock_out_at")),
                        (Integer) rs.getObject("duration_minutes"), rs.getString("status"),
                        rs.getString("reconciliation_reference")), userId).stream().findFirst();
    }
}
