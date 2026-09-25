package co.za.millenniumsolutions;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Keeps Spring integration tests away from the developer's local database and resets the
 * disposable integration database before each test method.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ActiveProfiles("integration")
@Sql(scripts = "/test-database-reset.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public @interface IsolatedSqliteTest {
}
