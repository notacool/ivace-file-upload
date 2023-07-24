package com.fileupload.web.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableJpaRepositories("com.fileupload.web.app.repository")
// @EnableSwagger2
public class AlfrescoFileUploadApplication {
	public static void main(String[] args) {
		SpringApplication.run(AlfrescoFileUploadApplication.class, args);
	}
}
