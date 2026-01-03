package com.example.bustracker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class busTrackerApplication {
    public static void main(String[] args) {
        try {
            System.out.println("Attempting to load webpage now");
            SpringApplication.run(busTrackerApplication.class, args);
        } catch (Exception e) {
            System.out.println("Webpage failed (Springboot error)");
            e.printStackTrace();
        }
    }

    @Bean
    public CommandLineRunner printStartupMessage() {
        return args -> {
            System.out.println("Web page successful!");
        };
    }
}

