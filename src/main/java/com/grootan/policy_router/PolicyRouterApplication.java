package com.grootan.policy_router;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.domain.EntityScan;


@SpringBootApplication
@EnableRetry
@EnableScheduling
@ComponentScan(basePackages = {"com.grootan.policy_router", "com.grootan.policyrouter"})
@EnableJpaRepositories(basePackages = "com.grootan.policyrouter.domain.repository")
@EntityScan(basePackages = "com.grootan.policyrouter.domain.model")
public class PolicyRouterApplication {
	public static void main(String[] args) {
		SpringApplication.run(PolicyRouterApplication.class, args);
	}
}