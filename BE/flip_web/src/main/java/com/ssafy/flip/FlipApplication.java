package com.ssafy.flip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlipApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlipApplication.class, args);
	}

}
