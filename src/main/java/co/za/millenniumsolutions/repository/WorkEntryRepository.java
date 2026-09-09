package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.WorkEntry;
import co.za.millenniumsolutions.model.WorkEntrySummary;
import co.za.millenniumsolutions.model.WorkPeriod;
import co.za.millenniumsolutions.model.WorkEntryTiming;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class WorkEntryRepository extends RepositorySupport {
    public WorkEntryRepository(JdbcTemplate j) {
        super(j);
    }

    public WorkEntry save(WorkEntry x) {
        update("INSERT INTO work_entry(id,user_id,work_period_id,activity_type_id,work_date,duration_minutes,status) " +
                        "VALUES (?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET status=excluded.status," +
                        "user_id=excluded.user_id,work_period_id=excluded.work_period_id," +
                        "activity_type_id=excluded.activity_type_id,work_date=excluded.work_date," +
                        "duration_minutes=excluded.duration_minutes",
                x.id(), x.userId(), x.workPeriodId(), x.activityTypeId(), x.workDate().toString(),
                x.durationMinutes(), x.status());
        if (x.startTime() != null && x.endTime() != null) {
            update("INSERT INTO work_entry_timing(work_entry_id,start_time,end_time,break_minutes) " +
                            "VALUES (?,?,?,?) ON CONFLICT(work_entry_id) DO UPDATE SET " +
                            "start_time=excluded.start_time,end_time=excluded.end_time,break_minutes=excluded.break_minutes",
                    x.id(), x.startTime().toString(), x.endTime().toString(), x.breakMinutes());
        }
        return x;
    }

    public Optional<WorkEntry> findById(String id) {
        return jdbc.query("""
                        SELECT e.id,e.user_id,e.work_period_id,e.activity_type_id,e.work_date,
                               e.duration_minutes,e.status,t.start_time,t.end_time,t.break_minutes
                        FROM work_entry e
                        LEFT JOIN work_entry_timing t ON t.work_entry_id = e.id
                        WHERE e.id = ?
                        """,
                        (rs, rowNum) -> new WorkEntry(
                                rs.getString("id"),
                                rs.getString("user_id"),
                                rs.getString("work_period_id"),
                                rs.getString("activity_type_id"),
                                LocalDate.parse(rs.getString("work_date")),
                                rs.getInt("duration_minutes"),
                                rs.getString("status"),
                                rs.getString("start_time") == null ? null : LocalTime.parse(rs.getString("start_time")),
                                rs.getString("end_time") == null ? null : LocalTime.parse(rs.getString("end_time")),
                                rs.getInt("break_minutes")),
                        id)
                .stream()
                .findFirst();
    }

    public List<WorkEntryTiming> findOverlapping(String userId, LocalDate workDate,
                                                  LocalTime startTime, LocalTime endTime,
                                                  String excludedId) {
        return jdbc.query("""
                        SELECT t.start_time,t.end_time,t.break_minutes
                        FROM work_entry e
                        JOIN work_entry_timing t ON t.work_entry_id = e.id
                        WHERE e.user_id = ? AND e.work_date = ? AND e.id <> ?
                          AND t.start_time < ? AND t.end_time > ?
                        """,
                        (rs, rowNum) -> new WorkEntryTiming(
                                LocalTime.parse(rs.getString("start_time")),
                                LocalTime.parse(rs.getString("end_time")),
                                rs.getInt("break_minutes")),
                        userId, workDate.toString(), excludedId, endTime.toString(), startTime.toString());
    }

    public WorkEntrySummary summarize(String userId, WorkPeriod period) {
        return jdbc.queryForObject(
                "SELECT COALESCE(SUM(duration_minutes), 0) AS logged_minutes, " +
                        "COALESCE(SUM(CASE WHEN status IN ('VERIFIED', 'APPROVED') " +
                        "THEN duration_minutes ELSE 0 END), 0) AS verified_minutes " +
                        "FROM work_entry WHERE user_id = ? AND work_period_id = ? " +
                        "AND work_date BETWEEN ? AND ?",
                (rs, rowNum) -> new WorkEntrySummary(
                        rs.getLong("logged_minutes"),
                        rs.getLong("verified_minutes")),
                userId, period.id(), period.startDate().toString(), period.endDate().toString());
    }
}
