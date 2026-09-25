package co.za.millenniumsolutions;

import co.za.millenniumsolutions.aws.WeeklyReminderLambdaHandler;
import co.za.millenniumsolutions.service.WeeklyReminderService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WeeklyReminderLambdaHandlerTests {
    @Test
    void parsesEventBridgeTimeAndReturnsAggregateResult() {
        var handler = new WeeklyReminderLambdaHandler(new StubReminderService(
                new WeeklyReminderService.ReminderRunResult(12, 3, 3), null));

        Map<String, Object> result = handler.handleRequest(Map.of(
                "id", "scheduled-event-1",
                "scheduledTime", "2026-07-15T22:30:00Z"), null);

        assertEquals("completed", result.get("status"));
        assertEquals("2026-07-15", result.get("evaluationDate"));
        assertEquals(12, result.get("studentsEvaluated"));
        assertEquals(3, result.get("studentsBehind"));
        assertEquals(3, result.get("notificationsCreated"));
    }

    @Test
    void propagatesFailuresSoLambdaCanRetryTheInvocation() {
        IllegalStateException failure = new IllegalStateException("database unavailable");
        var handler = new WeeklyReminderLambdaHandler(new StubReminderService(null, failure));

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> handler.handleRequest(Map.of("id", "scheduled-event-2"), null));

        assertSame(failure, thrown);
    }

    private static final class StubReminderService extends WeeklyReminderService {
        private final ReminderRunResult result;
        private final RuntimeException failure;

        private StubReminderService(ReminderRunResult result, RuntimeException failure) {
            super(null, null, null, null);
            this.result = result;
            this.failure = failure;
        }

        @Override
        public ReminderRunResult run(LocalDate date) {
            if (failure != null) throw failure;
            return result;
        }
    }
}
