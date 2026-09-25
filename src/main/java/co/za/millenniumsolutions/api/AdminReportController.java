package co.za.millenniumsolutions.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasAuthority('PERM_REPORT_READ') || hasRole('ADMIN') || hasRole('SUPER_ADMIN')")
public class AdminReportController {
    private final JdbcTemplate jdbc;
    public AdminReportController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping(value = "/work-summary.csv", produces = "text/csv")
    public ResponseEntity<String> workSummary(@RequestParam(required = false) String userId,
                                              @RequestParam(required = false) String workPeriodId,
                                              @RequestParam(required = false) String status) {
        StringBuilder sql = new StringBuilder("""
                SELECT w.id,w.user_id,w.work_period_id,w.work_date,w.duration_minutes,w.status,
                       COALESCE(s.status,'NOT_SUBMITTED') submission_status,
                       CASE WHEN s.status='APPROVED' THEN 'VERIFIED' ELSE 'PENDING' END verification_status
                FROM work_entry w LEFT JOIN submission s ON s.work_entry_id=w.id WHERE 1=1
                """);
        List<Object> args = new java.util.ArrayList<>();
        if (userId != null && !userId.isBlank()) { sql.append(" AND w.user_id=?"); args.add(userId); }
        if (workPeriodId != null && !workPeriodId.isBlank()) { sql.append(" AND w.work_period_id=?"); args.add(workPeriodId); }
        if (status != null && !status.isBlank()) { sql.append(" AND w.status=?"); args.add(status); }
        sql.append(" ORDER BY w.work_date,w.id");
        List<Map<String,Object>> rows = jdbc.queryForList(sql.toString(), args.toArray());
        StringBuilder csv = new StringBuilder("work_entry_id,user_id,work_period_id,work_date,duration_minutes,work_status,submission_status,verification_status\n");
        for (Map<String,Object> row : rows) {
            csv.append(csv(row.get("id"))).append(',').append(csv(row.get("user_id"))).append(',')
                    .append(csv(row.get("work_period_id"))).append(',').append(csv(row.get("work_date"))).append(',')
                    .append(csv(row.get("duration_minutes"))).append(',').append(csv(row.get("status"))).append(',')
                    .append(csv(row.get("submission_status"))).append(',').append(csv(row.get("verification_status"))).append('\n');
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=work-summary.csv")
                .contentType(MediaType.parseMediaType("text/csv")).body(csv.toString());
    }
    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
