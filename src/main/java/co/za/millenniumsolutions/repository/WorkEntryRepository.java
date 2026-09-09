package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.WorkEntry;
import co.za.millenniumsolutions.model.WorkEntrySummary;
import co.za.millenniumsolutions.model.WorkPeriod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class WorkEntryRepository extends RepositorySupport {
    public WorkEntryRepository(JdbcTemplate j) {
        super(j);
    }

    public WorkEntry save(WorkEntry x) {
        update("INSERT INTO work_entry(id,user_id,work_period_id,activity_type_id,work_date,duration_minutes,status) " +
                        "VALUES (?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET status=excluded.status," +
                        "duration_minutes=excluded.duration_minutes",
                x.id(), x.userId(), x.workPeriodId(), x.activityTypeId(), x.workDate().toString(),
                x.durationMinutes(), x.status());
        return x;
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
