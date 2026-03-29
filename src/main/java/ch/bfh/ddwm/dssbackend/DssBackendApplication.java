package ch.bfh.ddwm.dssbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport
public class DssBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DssBackendApplication.class, args);
    }

}
