package com.untanglechat.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ChatappApplication {

	public static void main(String[] args) {

		SpringApplication.run(ChatappApplication.class, args);

	}


}
