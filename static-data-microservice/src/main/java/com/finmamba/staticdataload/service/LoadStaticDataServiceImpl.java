package com.finmamba.staticdataload.service;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.finmamba.kafka.starter.autoconfigure.rest.KafkaRestService;
import com.finmamba.kafka.starter.util.ApplicationProperties;
import com.finmamba.models.bus.EventType;
import com.finmamba.models.bus.SerialDataBusModel;
import com.finmamba.staticdataload.configuration.ApplicationConstants;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.netty.handler.timeout.TimeoutException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoadStaticDataServiceImpl implements LoadStaticDataService {

	@Autowired
	KafkaRestService kafkaRestService;
	@Autowired
	ApplicationProperties applicationProperties;

	private SerialDataBusModel prepareRequest(Object payload, EventType eventType) {
		//@// @formatter:off
		return SerialDataBusModel.builder()
				.payload(payload)
				.messageTimestamp(LocalDateTime.now().toString())
				.eventType(eventType)
				.build();
		// @formatter:on
	}

	@Override
	public HashMap<String, Object> fetchStaticData()
			throws InterruptedException, ExecutionException, JsonProcessingException, TimeoutException {
		log.info("Fetching Static data for Matter operations" + ApplicationConstants.METHOD_EXECUTION_START);
		String responseJson = kafkaRestService.send(prepareRequest("1", EventType.FIND_ALL),
				applicationProperties.getProducerTopicName());
		try {
			//List<Object>response = new ArrayList<Object>();
			Gson gson = new Gson();
			JsonObject convertedObject = gson.fromJson(responseJson, JsonObject.class);
			JsonObject payloadObject = convertedObject.getAsJsonObject(ApplicationConstants.PAYLOAD_TAG);
			Type typeTask= new TypeToken<HashMap<String, Object>>() {
				private static final long serialVersionUID = 1L;
			}.getType();
			HashMap<String, Object> staticDataList = gson.fromJson(payloadObject, typeTask);
			log.debug("Fetchinng of static data for matter - END");
			return staticDataList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.debug("Fetching of static data list failed. Returning an empty list");
		return null;
	}

	@Override
	public String reloadCache() {
		log.info("Fetching Static data for Matter operations" + ApplicationConstants.METHOD_EXECUTION_START);
		return kafkaRestService.send(prepareRequest("1", EventType.FIND_BY_ID),
				applicationProperties.getProducerTopicName());
		
	}
	

	
}
