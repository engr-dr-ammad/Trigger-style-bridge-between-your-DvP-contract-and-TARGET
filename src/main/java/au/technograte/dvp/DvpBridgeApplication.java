package au.technograte.dvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DvP Bridge Application - Trigger-style bridge between DvP contracts and TARGET payment system
 * Manages tokenized bonds, settlement, and ISO 20022 payment messaging
 *
 * Build by: Engr Ammad Aslam
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "au.technograte.dvp")
public class DvpBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DvpBridgeApplication.class, args);
    }
}
