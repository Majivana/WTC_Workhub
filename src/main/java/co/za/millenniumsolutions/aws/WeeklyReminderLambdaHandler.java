package co.za.millenniumsolutions.aws;

import co.za.millenniumsolutions.Main;
import co.za.millenniumsolutions.service.WeeklyReminderService;
import co.za.millenniumsolutions.service.WeeklyReminderService.ReminderRunResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** EventBridge scheduled-event adapter. Exceptions are rethrown so Lambda retry/DLQ policy can act. */
public final class WeeklyReminderLambdaHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private static final Logger log = LoggerFactory.getLogger(WeeklyReminderLambdaHandler.class);
    private static final AtomicReference<ConfigurableApplicationContext> CONTEXT = new AtomicReference<>();

    private final WeeklyReminderService reminders;

    /** AWS Lambda managed runtime constructor. */
    public WeeklyReminderLambdaHandler() {
        this(serviceFromSpring());
    }

    /** Testable constructor that avoids starting a Spring context. */
    public WeeklyReminderLambdaHandler(WeeklyReminderService reminders) {
        this.reminders = reminders;
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        String eventId = event == null ? null : stringValue(event.get("id"));
        try {
            LocalDate evaluationDate = eventDate(event);
            ReminderRunResult result = reminders.run(evaluationDate);
            log.info("Weekly reminder run completed: eventId={}, evaluationDate={}, studentsEvaluated={}, studentsBehind={}, notificationsCreated={}",
                    eventId, evaluationDate, result.studentsEvaluated(), result.studentsBehind(),
                    result.notificationsCreated());
            return Map.of("status", "completed", "evaluationDate", evaluationDate.toString(),
                    "studentsEvaluated", result.studentsEvaluated(),
                    "studentsBehind", result.studentsBehind(),
                    "notificationsCreated", result.notificationsCreated());
        } catch (RuntimeException exception) {
            log.error("Weekly reminder run failed: eventId={}, exceptionType={}", eventId,
                    exception.getClass().getSimpleName());
            throw exception;
        }
    }

    private static LocalDate eventDate(Map<String, Object> event) {
        if (event == null) return LocalDate.now(ZoneOffset.UTC);
        Object timestamp = event.getOrDefault("time", event.get("scheduledTime"));
        if (timestamp == null) return LocalDate.now(ZoneOffset.UTC);
        return Instant.parse(stringValue(timestamp)).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static WeeklyReminderService serviceFromSpring() {
        ConfigurableApplicationContext context = CONTEXT.updateAndGet(existing -> existing == null
                ? new SpringApplicationBuilder(Main.class).web(WebApplicationType.NONE).run()
                : existing);
        return context.getBean(WeeklyReminderService.class);
    }
}
