package co.za.millenniumsolutions;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@IsolatedSqliteTest
@SpringBootTest
class MainApplicationTests {

    @Test
    void applicationContextStarts() {
        assertThat(true).isTrue();
    }
}
