package com.finmamba.staticdatadal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.finmamba.kafka.starter.KafkaEventModel;

public interface LoadStaticDataService {
	/**
	 * To load static data Interface to load all the static data registration.
	 */
	public void eventTopicListener(KafkaEventModel event) throws JsonProcessingException; // To listen to kafka Topic

	public void getAssetClasses();

	public void getInstitutionalEntitiesAsBanks();

	public void getCurrencies();

	public void getCurrencyPairDetails();
	
	public void getCustomerSegments();

	public void getDirections();

	public void getEntityDetails();

	public void getProducts();

	public void getProductValidityMap();

	public void getRequestTypes();

	public void getTenors();

	public void getTimerUnits();

	public void getUserRoles();

	public void refreshcache();
}
