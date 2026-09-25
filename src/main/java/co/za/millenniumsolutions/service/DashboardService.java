package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.model.Escalation;
import co.za.millenniumsolutions.model.Notification;
import co.za.millenniumsolutions.repository.EscalationRepository;
import co.za.millenniumsolutions.repository.NotificationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    private final JdbcTemplate jdbc;
    private final ProgressService progress;
    private final NotificationRepository notifications;
    private final EscalationRepository escalations;

    public DashboardService(JdbcTemplate jdbc, ProgressService progress, NotificationRepository notifications,
                            EscalationRepository escalations) {
        this.jdbc=jdbc; this.progress=progress; this.notifications=notifications; this.escalations=escalations;
    }
    public StudentDashboard student(String userId, String periodId) {
        return new StudentDashboard(progress.calculate(userId, periodId),
                jdbc.queryForList("SELECT id,status,clock_in_at,duration_minutes,reconciliation_reference FROM attendance_session WHERE user_id=? AND work_period_id=? ORDER BY clock_in_at DESC", userId, periodId),
                jdbc.queryForList("SELECT w.id,w.work_date,w.duration_minutes,w.status,COALESCE(s.status,'NOT_SUBMITTED') submission_status FROM work_entry w LEFT JOIN submission s ON s.work_entry_id=w.id WHERE w.user_id=? AND w.work_period_id=? ORDER BY w.work_date DESC",userId,periodId),
                notifications.findByRecipient(userId));
    }
    public SupervisorQueue supervisor(String supervisorId) {
        return new SupervisorQueue(
                jdbc.queryForList("""
                        SELECT s.id,s.work_entry_id,s.status,w.user_id,w.work_date,w.duration_minutes
                        FROM submission s JOIN work_entry w ON w.id=s.work_entry_id
                        JOIN app_user u ON u.id=w.user_id
                        WHERE s.status IN ('SUBMITTED','UNDER_REVIEW','RESUBMITTED')
                          AND (u.supervisor_id=? OR u.mentor_id=?)
                        ORDER BY w.work_date
                        """, supervisorId, supervisorId),
                jdbc.queryForList("""
                        SELECT a.id,a.user_id,a.campus_id,a.clock_in_at,a.status,a.reconciliation_reference
                        FROM attendance_session a JOIN app_user u ON u.id=a.user_id
                        WHERE (a.status IN ('ACTIVE','CORRECTED') OR a.reconciliation_reference LIKE '%evidence=0%')
                          AND (u.supervisor_id=? OR u.mentor_id=?)
                        ORDER BY a.clock_in_at DESC
                        """, supervisorId, supervisorId),
                escalations.findOpenAssignedTo(supervisorId));
    }
    public record StudentDashboard(WeeklyProgress progress, List<Map<String,Object>> attendance,
                                   List<Map<String,Object>> workEntries, List<Notification> notifications) {}
    public record SupervisorQueue(List<Map<String,Object>> pendingReviews, List<Map<String,Object>> attendanceExceptions,
                                  List<Escalation> escalations) {}
}
