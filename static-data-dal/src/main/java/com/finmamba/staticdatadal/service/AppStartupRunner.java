package com.finmamba.staticdatadal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AppStartupRunner implements ApplicationRunner {

	@Autowired
	LoadStaticDataServiceImpl loadStaticDataService;

	@Override
	public void run(ApplicationArguments args) throws Exception {

		// Calling LoadStaticDataServiceImpl methods to insert data into REDIS cache.
		log.info("Inserting into REDIS cache...");
		loadStaticDataService.getAssetClasses();
		loadStaticDataService.getInstitutionalEntitiesAsBanks();
		loadStaticDataService.getCurrencies();
		loadStaticDataService.getCurrencyPairDetails();
		loadStaticDataService.getCustomerSegments();
		loadStaticDataService.getDirections();
		loadStaticDataService.getEntityDetails();
		loadStaticDataService.getProducts();
		loadStaticDataService.getProductValidityMap();
		loadStaticDataService.getRequestTypes();
		loadStaticDataService.getTenors();
		loadStaticDataService.getTimerUnits();
		loadStaticDataService.getUserRoles();
		loadStaticDataService.refreshcache();
	}

}
