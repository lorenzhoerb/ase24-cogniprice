package ase.cogniprice.integrationTest.cronJobSchedulerIntegrationTest;

import ase.cogniprice.service.scheduler.impl.SchedulerService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EnableScheduling
@TestPropertySource(properties = {
        "scheduler.scheduling.cronExpression=0/5 * * * * ?", // Every 5 seconds
        "scheduling.scheduler.crawlLimit=10"
})
@ActiveProfiles(value = {"${spring.profiles.active}", "cron"})
public class CronJobSchedulerIntegrationTest {

    @MockBean
    private SchedulerService schedulerService;

    @Test
    void testScheduledExecution() throws InterruptedException {
        // Wait to let the scheduler execute (5 seconds for our cron)
        Thread.sleep(6000);

        // Verify that the service was triggered at least once
        verify(schedulerService, atLeastOnce()).dispatchScheduledCrawlJobs(10);
    }
}
