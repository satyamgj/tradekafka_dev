package com.tradekafka.ondc.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.finmamba.entities.staticdata.Direction;
import com.tradekafka.ondc.repository.DirectionRepository;
import com.tradekafka.ondc.services.DirectionStaticData;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DirectionInitializer {
	@Autowired
	DirectionRepository directionRepository;
	
	 @Bean
	    public ApplicationRunner initDirectionData(DirectionStaticData directionStaticData) {
	        return new ApplicationRunner() {
	            @Override
	            public void run(ApplicationArguments args) throws Exception {
	                // Initialize and add static data during application startup
	            	log.info("loading direction");
	            	List<Direction> directionList = directionRepository.findAll();
	            	for(Direction direction:directionList) {
	            		directionStaticData.addDirection(direction);
	            	}
	                // Add more data as needed
	            }
	        };
	    }
}
