package com.springboot.spring_security;

import com.springboot.spring_security.models.User;
import com.springboot.spring_security.services.JWTService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@SpringBootApplication
public class SpringSecurityApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		SpringApplication.run(SpringSecurityApplication.class, args);

	}
}
