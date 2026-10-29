package au.technograte.dvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DvP Bridge Application - Trigger-style bridge between DvP contracts and TARGET payment system
 * Manages tokenized bonds, settlement, and ISO 20022 payment messaging
 *
 * Developed by: Engr Dr. Ammad Aslam
 * Email: ammad1@iscte-iul.pt
 * Development: With AI-assisted tooling (Claude AI)
 * Organization: ISCTE-IUL (Instituto Universitário de Lisboa)
 * Version: 1.0.0
 * Date: 2026-10-29
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "au.technograte.dvp")
public class DvpBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DvpBridgeApplication.class, args);
    }
}
