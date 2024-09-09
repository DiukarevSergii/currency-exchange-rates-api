package com.task.spribe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpribeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpribeApplication.class, args);
	}

}
