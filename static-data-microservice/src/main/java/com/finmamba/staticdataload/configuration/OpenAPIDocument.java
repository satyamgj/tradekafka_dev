package com.finmamba.staticdataload.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

public class OpenAPIDocument {
	@Bean
	public OpenAPI customOpenAPI(@Value("${application-description}") String appDesciption,
			@Value("${application-version}") String appVersion, @Autowired List<Server> listServer) {
		return new OpenAPI().servers(listServer)
				.info(new Info().title("QiLegal API Document")
						.version(appVersion)
						.description(appDesciption)
						//.termsOfService("http://swagger.io/terms/")
						//.license(new License().name("QiLegal")
						//.url("http://springdoc.org"))
						)
						;
	}

	@Bean
	public List<Server> configureServerDescription(@Value("${server.servlet.context-path}") String serverContextPath,
			@Value("${server-description}") String serverDescription) {
		List<Server> listServer = new ArrayList<>();
		Server server = new Server();
		server.setUrl(serverContextPath);
		server.setDescription(serverDescription);
		return listServer;

	}
}
