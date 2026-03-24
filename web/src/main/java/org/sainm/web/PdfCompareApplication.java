package org.sainm.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PdfCompareApplication {
    public static void main(String[] args) {
        SpringApplication.run(PdfCompareApplication.class, args);
    }
}
