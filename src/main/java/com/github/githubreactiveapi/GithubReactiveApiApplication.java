package com.github.githubreactiveapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class GithubReactiveApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(GithubReactiveApiApplication.class, args);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().baseUrl("https://api.github.com").build();
    }
}