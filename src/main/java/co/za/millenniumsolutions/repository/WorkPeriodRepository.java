package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.WorkPeriod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public class WorkPeriodRepository extends RepositorySupport {
    public WorkPeriodRepository(JdbcTemplate j) {
        super(j);
    }

    public WorkPeriod save(WorkPeriod x) {
        update("INSERT INTO work_period(id,name,start_date,end_date,weekly_hours_target) VALUES (?,?,?,?,?) " +
                        "ON CONFLICT(id) DO UPDATE SET name=excluded.name,start_date=excluded.start_date," +
                        "end_date=excluded.end_date,weekly_hours_target=excluded.weekly_hours_target",
                x.id(), x.name(), x.startDate().toString(), x.endDate().toString(), x.weeklyHoursTarget());
        return x;
    }

    public Optional<WorkPeriod> findById(String id) {
        return jdbc.query("SELECT id,name,start_date,end_date,weekly_hours_target FROM work_period WHERE id = ?",
                        (rs, rowNum) -> new WorkPeriod(
                                rs.getString("id"),
                                rs.getString("name"),
                                LocalDate.parse(rs.getString("start_date")),
                                LocalDate.parse(rs.getString("end_date")),
                                rs.getBigDecimal("weekly_hours_target")),
                        id)
                .stream()
                .findFirst();
    }
}
