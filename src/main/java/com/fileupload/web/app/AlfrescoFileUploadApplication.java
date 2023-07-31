package com.fileupload.web.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.fileupload.web.app.controller.FileUploadController;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class }	)
@EnableJpaRepositories("com.fileupload.web.app.repository")
@EnableSwagger2
public class AlfrescoFileUploadApplication {

	static Logger logger = LoggerFactory.getLogger(FileUploadController.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(AlfrescoFileUploadApplication.class, args);
		String str = ctx.getEnvironment().getProperty("version");
		System.out.println("Deployed Version: " + str);
	}

}
