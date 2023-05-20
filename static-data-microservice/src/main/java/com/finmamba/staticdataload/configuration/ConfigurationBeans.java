package com.finmamba.staticdataload.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class ConfigurationBeans {
	
	@Bean
	public Gson getGsonInstance() {
		return new Gson();
	}
}
