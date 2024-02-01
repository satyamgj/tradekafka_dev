package com.finmamba.simulator.configuration;

import java.sql.Timestamp;
import java.util.Date;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class ConfigurationBeans {

	@Bean
	public Gson getGsonInstance() {
		return new GsonBuilder()
				.registerTypeAdapter(Date.class, new TimestampDeserializer())
				.registerTypeAdapter(Date.class, new TimestampSerializer())
				.registerTypeAdapter(Timestamp.class, new TimestampSerializer())
				.create();
		
	}
	
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
	
	@Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster =
          new SimpleApplicationEventMulticaster();
        
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return eventMulticaster;
    }

}
