package com.finmamba.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.finmamba.entities.staticdata.interfaces.ImportEntities;
import com.finmamba.kafka.starter.EnableKafkaProducer;
import com.finmamba.kafka.starter.EnableKafkaRest;

@SpringBootApplication
@EnableKafkaProducer
@EnableKafkaRest
@ImportEntities
//@EnableKafkaConsumer
public class SimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimulatorApplication.class, args);
	}

}
