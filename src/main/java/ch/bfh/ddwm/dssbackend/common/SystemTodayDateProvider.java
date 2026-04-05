package ch.bfh.ddwm.dssbackend.common;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Profile("!dev")
public class SystemTodayDateProvider implements TodayDateProvider {

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }
}
