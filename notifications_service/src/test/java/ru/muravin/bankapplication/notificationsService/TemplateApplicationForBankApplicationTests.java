package ru.muravin.bankapplication.notificationsService;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

@SpringBootTest
@Disabled
@Import(TestcontainersConfiguration.class)
class TemplateApplicationForBankApplicationTests {

    @Test
    void contextLoads() {
    }

}
