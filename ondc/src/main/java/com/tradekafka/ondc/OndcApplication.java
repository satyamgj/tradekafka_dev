package com.tradekafka.ondc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import com.finmamba.entities.staticdata.interfaces.ImportEntities;
import com.finmamba.kafka.starter.EnableKafkaProducer;

@SpringBootApplication
@EnableKafkaProducer
//@EnableKafkaRest
@ImportEntities
//@EnableKafkaConsumer
@EnableAsync
@EnableCaching
public class OndcApplication {

	public static void main(String[] args) {
		SpringApplication.run(OndcApplication.class, args);
	}

}
