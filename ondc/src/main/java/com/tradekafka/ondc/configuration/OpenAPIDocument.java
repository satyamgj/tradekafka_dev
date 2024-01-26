package com.tradekafka.ondc.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenAPIDocument {
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("FXTrader API Document")
						.version("v1")
						.description("API Description")
						//.termsOfService("http://swagger.io/terms/")
						//.license(new License().name("finmamba")
						//.url("http://springdoc.org"))
						)
						;
	}
}
