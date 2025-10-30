package org.example.toy_zhiri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Главный класс Spring Boot приложения ToyZhiri.
 */
@SpringBootApplication
@EnableScheduling
public class ToyZhiriApplication {
    // Точка входа в приложение.
    public static void main(String[] args) {
        SpringApplication.run(ToyZhiriApplication.class, args);
    }

}
