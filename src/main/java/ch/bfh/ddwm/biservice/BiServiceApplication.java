package ch.bfh.ddwm.biservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport
public class BiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiServiceApplication.class, args);
    }

}
