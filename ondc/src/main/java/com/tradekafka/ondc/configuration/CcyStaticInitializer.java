package com.tradekafka.ondc.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.finmamba.entities.staticdata.Currency;
import com.tradekafka.ondc.repository.CurrencyRepository;
import com.tradekafka.ondc.services.CurrencyStaticMap;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class CcyStaticInitializer {
	
	@Autowired
	CurrencyRepository currencyRepository;
	
	 @Bean
	    public ApplicationRunner initCcy(CurrencyStaticMap currencyStaticMap) {
	        return new ApplicationRunner() {
	            @Override
	            public void run(ApplicationArguments args) throws Exception {
	                // Initialize and add static data during application startup
	            	log.info("loading ccy");

	            	List<Currency> ccyList = currencyRepository.findAll();
	            	for(Currency ccy:ccyList) {
	            		currencyStaticMap.addCurrency(ccy);
	            	}
	                // Add more data as needed
	            }
	        };
	    }
}
