package com.akoev.dme;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class AbstractIntegrationTest {

    // Singleton container pattern: started once per JVM (not per test class) so every
    // test class shares the same instance and Spring's context cache stays warm.
    // The official MySQL image restarts its server process once during first-time
    // initialization; waiting for a single "ready for connections" log line can
    // report the container as ready before that restart, causing flaky connection
    // refusals. Waiting for the message twice avoids that race. Cleanup is handled
    // by the Testcontainers Ryuk reaper at JVM shutdown.
    @ServiceConnection
    static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.4")
            .waitingFor(Wait.forLogMessage(".*ready for connections.*\\n", 2));

    static {
        MYSQL_CONTAINER.start();
    }

}
