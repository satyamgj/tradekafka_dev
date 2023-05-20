package com.finmamba.staticdataload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import com.finmamba.kafka.starter.EnableKafkaRest;



@SpringBootApplication
@EnableAutoConfiguration(
		exclude = {DataSourceAutoConfiguration.class
				, DataSourceTransactionManagerAutoConfiguration.class
				, HibernateJpaAutoConfiguration.class})
@EnableKafkaRest
public class StaticDataLoadApplication {

	public static void main(String[] args) {
		SpringApplication.run(StaticDataLoadApplication.class, args);
	}

}
