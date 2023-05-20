package com.finmamba.staticdataload.controller;



import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.finmamba.staticdataload.service.LoadStaticDataService;

import io.netty.handler.timeout.TimeoutException;

@RestController
@RequestMapping
@CrossOrigin
public class LoadStaticdataController {

	@Autowired 
	LoadStaticDataService loadStaticDataService;
	
	@GetMapping("/loadStaticData")
	public ResponseEntity<HashMap<String, Object>> fetchMatterStaticData() throws TimeoutException, JsonProcessingException, InterruptedException, ExecutionException{	
		return new ResponseEntity<>(loadStaticDataService.fetchStaticData(), HttpStatus.OK);
	}
	
	@GetMapping("/reloadStaticCache")
	public ResponseEntity<String> reloadCache(){
		return new ResponseEntity<>(loadStaticDataService.reloadCache(), HttpStatus.OK);
	}

}
