package com.daytodo.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.core.io.FileSystemResource;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentExampleTest {

    @Test
    void courseReminderCronIsLoadedAsAPropertiesValue() throws Exception {
        var propertySource = new PropertiesPropertySourceLoader()
                .load("env", new FileSystemResource(".env.example"))
                .get(0);

        assertThat(propertySource.getProperty("COURSE_REMINDER_CRON"))
                .isEqualTo("0 0 9 * * *");
    }
}
