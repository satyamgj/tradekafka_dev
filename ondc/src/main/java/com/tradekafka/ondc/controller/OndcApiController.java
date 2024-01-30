package com.tradekafka.ondc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.finmamba.models.bus.HttpResponseModel;
import com.finmamba.models.ondc.ConfirmRequest;
import com.finmamba.models.ondc.QuoteRequest;
import com.tradekafka.ondc.services.OndcServices;


@RestController
@CrossOrigin(origins = "*")

public class OndcApiController {

	@Autowired
	OndcServices ondcServices;
	
	@PostMapping("/quote")
	public ResponseEntity<Object> quote(@RequestBody QuoteRequest quoteRequest) {
		HttpResponseModel response  = ondcServices.quoteRequest(quoteRequest);
		return new ResponseEntity<>(
				response,
						HttpStatus.OK );
	}
	
	@PostMapping("/confirm")
	public ResponseEntity<Object> confirm(@RequestBody ConfirmRequest confirmRequest){
		HttpResponseModel response  = ondcServices.confirmRequest(confirmRequest);
		return new ResponseEntity<>(
				response,
						HttpStatus.OK );
	}
	
	@GetMapping("myQuotes/{userId}")
	public ResponseEntity<Object> fetchMyQuotes(@PathVariable("userId")Long userId){
		HttpResponseModel response  = ondcServices.fetchMyRequests(userId);
		return new ResponseEntity<>(
				response,
						HttpStatus.OK );
	}
	
	@GetMapping("matchedQuotes/{quoteId}")
	public ResponseEntity<Object> fetchMatchedQuotes(@PathVariable("quoteId")Long quoteId){
		HttpResponseModel response  = ondcServices.fetchMatchedRequests(quoteId);
		return new ResponseEntity<>(
				response,
						HttpStatus.OK );
	}
	
	@GetMapping("confirmedQuotes/{userId}")
	public ResponseEntity<Object> fetchMyConfirmedQuotes(@PathVariable("userId")Long userId){
		HttpResponseModel response  = ondcServices.fetchConfirmedQuotes(userId);
		return new ResponseEntity<>(
				response,
						HttpStatus.OK );
	}
	

	@PostMapping("addToSellCatalogue")
	public ResponseEntity<Object> addToSellCatalogue(@RequestBody com.finmamba.models.ondc.SellerCatalogue sellerCatalogue){
		HttpResponseModel response  = ondcServices.addSellerCatalogue(sellerCatalogue);
		return new ResponseEntity<>(
				response,
						HttpStatus.OK );
	}
	
	@GetMapping("mySellerCatalogue/{userId}")
	public ResponseEntity<Object> fetchmySellerCatalogue(@PathVariable("userId")Long userId){
		HttpResponseModel response  = ondcServices.fetchSellerCatalogue(userId);
		return new ResponseEntity<>(
				response,
						HttpStatus.OK );
	}
	
	@GetMapping("requestedQuotes/{userId}")
	public ResponseEntity<Object> fetchMyRequestedQuotes(@PathVariable("userId")Long userId){
		HttpResponseModel response  = ondcServices.fetchRequestedQuotes(userId);
		return new ResponseEntity<>(
				response,
						HttpStatus.OK );
	}
	
	@GetMapping("responded/{userId}/Toquote/{quoteId}")
	public ResponseEntity<Object> alreadyRespondedToQuote(@PathVariable("userId")Long userId, @PathVariable("quoteId")Long quoteId){
		HttpResponseModel response  = ondcServices.alreadyRespondedToQuote(userId,quoteId);
		return new ResponseEntity<>(
				response,
						HttpStatus.OK );
	}
}
