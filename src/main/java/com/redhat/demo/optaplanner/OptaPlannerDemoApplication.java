package com.redhat.demo.optaplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OptaPlannerDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(OptaPlannerDemoApplication.class, args);
	}

}
