package com.finmamba.resolver.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finmamba.models.bus.HttpResponseModel;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.transaction.FXRequestModel;
import com.finmamba.models.transaction.QuoteRequestModel;
import com.finmamba.resolver.exception.EntityNotFound;
import com.finmamba.resolver.services.TransactionService;
import com.finmamba.resolver.util.ApplicationConstants;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/transaction")
@CrossOrigin(origins = "*")
@Slf4j
public class QuoteRequestController {

	@Autowired
	TransactionService transactionService;

	/*
	 * Quote Request 
	 * 
	 * Below controller will be used to request quote by all the banks.
	 * @Input : QuoteRequestModel
	 * @output : 
	 * 
	 */
	
	@PostMapping("/processQuoteRequest")
	public ResponseEntity<List<QuoteRequestDto>> processQuoteRequest(@RequestBody QuoteRequestModel quoteRequestModel) {
		try {
			List<QuoteRequestDto> returnList = transactionService.processQuoteRequest(quoteRequestModel);
			returnList.get(0).setHttpStatus(HttpStatus.OK.value());
			return new ResponseEntity<>(returnList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * 2 -> 3
	 */
	@PostMapping("/processBrokerQuoteRequest")
	public ResponseEntity<Object> processBrokerQuoteRequest(@RequestBody DaddyDto requestQuote,
			@RequestParam(name = "callerRealmName", required = true) String callerRealmName) {
		
		boolean successFlag = false;
		try {
			successFlag = transactionService.processBrokerRequest(requestQuote, callerRealmName);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(ApplicationConstants.REQUEST_NOT_SAVED, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// @formatter:off
		return new ResponseEntity<>(
				HttpResponseModel.builder()
				.data(ApplicationConstants.REQUEST_SAVED)
				.message(successFlag ? "Broker request saved" : "Broker request not saved. Check logs")
				.status(HttpStatus.OK.value())
				.timestamp(LocalDateTime.now()).build(),
				successFlag ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
		// @formatter:on
	}

	/*
	 * 3 -> 2
	 */
	/**
	 * 
	 * @param requestQuote
	 * @param responseType - DEAL, NEGOTIATION
	 * @return
	 */
	@PostMapping("/processCounterpartyResponse/{responseType}")
	public ResponseEntity<Object> processCounterpartyResponse(@RequestBody DaddyDto requestQuote,
			@PathVariable(name = "responseType", required = true) int responseType,
			@RequestParam(name = "callerRealmName", required = true) String callerRealmName,
			@RequestParam(name = "negotiationParty",required = true) String sendToRealmName) {
		boolean successFlag = false;
		requestQuote.setRequestActive(true);

		// check who is responding and raise event according
		// request source == this destination then the negotiationp

		try {
			successFlag = transactionService.processCounterpartyResponse(requestQuote, responseType, callerRealmName,sendToRealmName);
		} catch (EntityNotFound e) {
			e.printStackTrace();
		}

		// @formatter:off
		return new ResponseEntity<>(
				HttpResponseModel.builder().data(ApplicationConstants.REQUEST_SAVED)
						.message(successFlag ? "Response saved!" : "Response not saved. Check logs")
						.status(HttpStatus.OK.value())
						.timestamp(LocalDateTime.now()).build(),
				successFlag ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
		// @formatter:on
	}
	

	/*
	 * 2 -> 1
	 */
	@PostMapping("/processBrokerResponse/{responseType}")
	public ResponseEntity<Object> processBrokerResponse(@RequestBody DaddyDto requestQuote,
			@PathVariable(name = "responseType", required = true) int responseType,
			@RequestParam(name = "callerRealmName", required = true) String callerRealmName,
			@RequestParam(name = "negotiationParty",required = true) String sendToRealmName) {
		boolean successFlag = false;
		try {
			successFlag = transactionService.processBrokerResponse(requestQuote, callerRealmName, responseType, sendToRealmName);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(ApplicationConstants.REQUEST_NOT_SAVED, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		// @formatter:off
		return new ResponseEntity<>(
				HttpResponseModel.builder().data(ApplicationConstants.REQUEST_SAVED)
						.message(successFlag ? "Response saved!" : "Response not saved. Check logs")
						.status(HttpStatus.OK.value())
						.timestamp(LocalDateTime.now()).build(),
				successFlag ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
		// @formatter:on
	}

	/*
	 * 2 -> 1
	 */
	/*
	@PostMapping("/processBrokerResponse")
	public ResponseEntity<Object> processBrokerResponse(@RequestBody DaddyDto requestQuote,
			@RequestParam(name = "callerRealmName", required = true) String callerRealmName) {
		boolean successFlag = false;
		try {
			successFlag = transactionService.processBrokerResponse(requestQuote, callerRealmName);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(ApplicationConstants.REQUEST_NOT_SAVED, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		// @formatter:off
		return new ResponseEntity<>(
				HttpResponseModel.builder().data(ApplicationConstants.REQUEST_SAVED)
						.message(successFlag ? "Response saved!" : "Response not saved. Check logs")
						.status(HttpStatus.OK.value())
						.timestamp(LocalDateTime.now()).build(),
				successFlag ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
		// @formatter:on
	}*/

	/*
	 * 1 -> 2 for negotiation response
	 */
	/*@PostMapping("/processNegotiationResponse/{responseType}")
	public ResponseEntity<Object> processQuoteNegotiationResponse(@RequestBody DaddyDto requestQuote,
			@PathVariable(name = "responseType", required = true) int responseType) {
		boolean successFlag = false;

		try {
			successFlag = transactionService.processNegotiationResponse(requestQuote, responseType);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(ApplicationConstants.REQUEST_NOT_SAVED, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		// @formatter:off
		return new ResponseEntity<>(
				HttpResponseModel.builder().data(ApplicationConstants.REQUEST_SAVED)
						.message(successFlag ? "Response saved!" : "Response not saved. Check logs")
						.timestamp(LocalDateTime.now()).build(),
				successFlag ? HttpStatus.CREATED : HttpStatus.INTERNAL_SERVER_ERROR);
		// @formatter:on
	}*/

	/*@PostMapping("/processNegotiationResponse/{responseType}")
	public ResponseEntity<Object> processQuoteNegotiationBrokerResponse(@RequestBody DaddyDto requestQuote,
			@PathVariable(name = "responseType", required = true) int responseType) {
		boolean successFlag = false;
		try {
			successFlag = transactionService.processNegotiationBrokerResponse(requestQuote, responseType);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(ApplicationConstants.REQUEST_NOT_SAVED, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		// @formatter:off
		return new ResponseEntity<>(
				HttpResponseModel.builder().data(ApplicationConstants.REQUEST_SAVED)
						.message(successFlag ? "Response saved!" : "Response not saved. Check logs")
						.timestamp(LocalDateTime.now()).build(),
				successFlag ? HttpStatus.CREATED : HttpStatus.INTERNAL_SERVER_ERROR);
		// @formatter:on
	}*/

	/*
	 * Make the transaction inactive Possible cases when a transaction will be made
	 * inactive 1. TTL has expired 2. Transaction is complete and the trade is
	 * complete
	 *
	 */
	@PostMapping("/expireOrder/{transactionId}")
	public ResponseEntity<HttpResponseModel> expireTransaction(
			@PathVariable(name = "transactionId") String transactionId) {
		try {
			transactionService.expireOrder(transactionId, "TIMEOUT");
		} catch (Exception e) {
			log.debug(Arrays.toString(e.getStackTrace()));
			// @// @formatter:off
			return new ResponseEntity<>(
					HttpResponseModel.builder().data(ApplicationConstants.REQUEST_NOT_SAVED).message(e.getMessage())
					.status(HttpStatus.OK.value())
							.stackTrace(e.getStackTrace()).timestamp(LocalDateTime.now()).build(),
					HttpStatus.INTERNAL_SERVER_ERROR);
			// @formatter:on
		}
		// @formatter:off
		return new ResponseEntity<>(HttpResponseModel.builder().data(ApplicationConstants.REQUEST_SAVED)
				.message("Request expired success").timestamp(LocalDateTime.now()).build(), HttpStatus.OK);
		// @formatter:on
	}

}
