package ch.bfh.ddwm.biservice.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Profile("dev")
public class DevTodayDateProvider implements TodayDateProvider {

    private final LocalDate configuredDate;

    public DevTodayDateProvider(@Value("${bi.reference-date}") LocalDate configuredDate) {
        this.configuredDate = configuredDate;
    }

    @Override
    public LocalDate today() {
        return configuredDate;
    }
}
