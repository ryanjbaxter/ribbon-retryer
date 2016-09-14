package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
@EnableRetry
public class RibbonRetryerApplication {

	@Autowired
	private RetryerService retryer;

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplateBuilder().build();
	}

	public static void main(String[] args) {
		SpringApplication.run(RibbonRetryerApplication.class, args);
	}

	@RequestMapping("/")
	public String index() {
		return retryer.getContent();
	}

	@RequestMapping("/retry")
	@Retryable
	public String indexRetry() {
		return retryer.getContent();
	}
}

@Service
class RetryerService {

	private RestTemplate rest;

	public RetryerService(RestTemplate rest) {
		this.rest = rest;
	}

	public String getContent() {
		return rest.getForObject("http://service1/", String.class);
	}
}
