package com.saurabh.code.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TickStatsApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(TickStatsApplication.class, args);
	}
}
