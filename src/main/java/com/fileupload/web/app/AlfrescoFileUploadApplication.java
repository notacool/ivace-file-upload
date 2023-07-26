package com.fileupload.web.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableJpaRepositories("com.fileupload.web.app.repository")
// @EnableSwagger2
public class AlfrescoFileUploadApplication {

	static Logger logger = LoggerFactory.getLogger(AlfrescoFileUploadApplication.class);

	public static void main(String[] args) {
		logger.info("Estas usando la version 1.4.0");
		SpringApplication.run(AlfrescoFileUploadApplication.class, args);
	}
	
}
