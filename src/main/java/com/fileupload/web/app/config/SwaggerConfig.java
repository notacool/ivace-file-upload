package com.fileupload.web.app.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
//@EnableSwagger2
public class SwaggerConfig {

	@Value("${version}")
	private String ver;

	 @Bean
	 public Docket api() {
		   return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
		     .select()
		        .apis(RequestHandlerSelectors.basePackage("com.fileupload.web.app.controller"))
		     .build();
		}

	    private ApiInfo apiInfo() {
	        return new ApiInfoBuilder().title("REST API")
	                .description("API REST Backend Gestor Documental IVACE").termsOfServiceUrl("")
	                .version(ver)
	                .build();
	    }

}