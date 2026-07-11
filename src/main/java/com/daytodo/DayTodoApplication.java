package com.daytodo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DayTodoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DayTodoApplication.class, args);
	}

}
