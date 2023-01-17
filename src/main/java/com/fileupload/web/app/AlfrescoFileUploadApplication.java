package com.fileupload.web.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication()
@EnableJpaRepositories("com.fileupload.web.app.repository")
public class AlfrescoFileUploadApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlfrescoFileUploadApplication.class, args);
	}

}
