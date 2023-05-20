package com.finmamba.staticdataload.service;



import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.netty.handler.timeout.TimeoutException;

public interface LoadStaticDataService {

	public HashMap<String, Object> fetchStaticData()
			throws InterruptedException, ExecutionException, JsonProcessingException, TimeoutException; // To fetch
																											// Static
	public String reloadCache();																										// data from
																											// REDIS/DB
}
