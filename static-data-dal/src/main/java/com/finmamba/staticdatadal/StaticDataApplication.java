package com.finmamba.staticdatadal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.finmamba.entities.staticdata.interfaces.ImportEntities;
import com.finmamba.kafka.starter.EnableKafkaConsumer;
import com.finmamba.kafka.starter.EnableKafkaProducer;
import com.finmamba.staticdatadal.service.LoadStaticDataService;


@SpringBootApplication
@EnableKafkaProducer
@EnableKafkaConsumer
@ComponentScan(basePackages = {"com.finmamba.staticdatadal"})
@ImportEntities
public class StaticDataApplication {

	@Autowired
	static LoadStaticDataService loadStaticDataService;
	
	public static void main(String[] args) {
		SpringApplication.run(StaticDataApplication.class, args);
	
	}

}
