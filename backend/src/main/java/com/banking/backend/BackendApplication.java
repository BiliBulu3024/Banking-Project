package com.banking.backend;

import com.banking.backend.entity.User;
import com.banking.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
    @Bean
    CommandLineRunner run(UserRepository userRepository) {
        return args -> {
            // Tạo user mới
            User user = new User();
            user.setUsername("BiliBuluTest");
            user.setEmail("Bilibulu@example.com");
            user.setPassword("123456");

            userRepository.save(user);
            System.out.println("Save: " + user.getUsername());
        };
    }
}
