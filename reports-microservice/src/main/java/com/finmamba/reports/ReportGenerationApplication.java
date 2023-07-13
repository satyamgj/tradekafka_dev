package com.finmamba.reports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import com.finmamba.entities.staticdata.interfaces.ImportEntities;
import com.finmamba.kafka.starter.EnableKafkaProducer;
import com.finmamba.kafka.starter.EnableKafkaRest;

@SpringBootApplication
@EnableKafkaProducer
@EnableKafkaRest
@ImportEntities
//@EnableKafkaConsumer
public class ReportGenerationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReportGenerationApplication .class, args);
	}

}