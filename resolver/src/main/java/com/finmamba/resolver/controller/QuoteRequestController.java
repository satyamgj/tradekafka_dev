package com.finmamba.resolver.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.finmamba.entities.transaction.CounterpartyTransactionDetails;
import com.finmamba.entities.transaction.TransactionDetail;
import com.finmamba.models.bus.HttpResponseModel;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.transaction.ConfirmRequest;
import com.finmamba.models.transaction.QuoteRequestModel;
import com.finmamba.models.transaction.TransactionRequest;
import com.finmamba.resolver.repositories.CounterPartyTransactionRepository;
import com.finmamba.resolver.repositories.TransactionRepository;
import com.finmamba.resolver.services.TransactionService;
import com.finmamba.resolver.services.TransactionServiceImpl;
import com.finmamba.resolver.util.ApplicationConstants;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/transaction")
@CrossOrigin(origins = "*")
@Slf4j
public class QuoteRequestController {

	@Autowired
	TransactionServiceImpl transactionService;
	
	@Autowired
	TransactionRepository transactionRepository;
	
	@Autowired
	CounterPartyTransactionRepository counterPartyTransactionRepository;

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
	public ResponseEntity<Object> processBrokerQuoteRequest(@RequestBody TransactionRequest requestBrokerDispatchRequest) {
		
		boolean successFlag = false;
		try {
			successFlag = transactionService.processBrokerRequest(requestBrokerDispatchRequest);
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
				//.timestamp(LocalDateTime.now())
				.build(),
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
	public ResponseEntity<Object> processCounterpartyResponse(@RequestBody TransactionRequest requestQuote,
			@PathVariable(name = "responseType", required = true) int responseType) {
		boolean successFlag = false;
		
		try {
			
			log.info("Transaction Id "  + requestQuote.getTransactionId());
			
			//Check whether transaction is active
			TransactionDetail isConfirmedTransaction = transactionRepository.findByIdAndIsConfirmed(
					Long.valueOf(requestQuote.getTransactionId())
					, true);
			
			if(isConfirmedTransaction != null) {
				return new ResponseEntity<>(
						HttpResponseModel.builder().data(ApplicationConstants.REQUEST_SAVED)
								.message("Transaction has already been completed")
								.status(HttpStatus.OK.value())
								//.timestamp(LocalDateTime.now())
								.build(),
								HttpStatus.OK );
			}
			
			else {
				successFlag = transactionService.processCounterpartyResponse(requestQuote, responseType);
				
				// @formatter:off
				return new ResponseEntity<>(
						HttpResponseModel.builder().data(ApplicationConstants.REQUEST_SAVED)
								.message(successFlag ? "Response saved!" : "Response not saved. Check logs")
								.status(HttpStatus.OK.value())
								//.timestamp(LocalDateTime.now())
								.build(),
						successFlag ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
				// @formatter:on
			}
			
			
		} catch (Exception e) {
			//e.printStackTrace();
			return new ResponseEntity<>(
					HttpResponseModel.builder()
							.message("processCounterpartyResponse: Controller " + e.getMessage() )
							.status(500)
							//.timestamp(LocalDateTime.now())
							.build(),
					successFlag ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
		}

		
	}

	/**
	 * 
	 * @param confirmRequest
	 * @param ConfirmRequest class object
	 */

	@PostMapping("/confirmRequest")
	public ResponseEntity<Object> confirmRequest(@RequestBody ConfirmRequest confirmRequest){
		
		boolean successFlag = false;
		try {
			
			CounterpartyTransactionDetails buySideCPTradeDetails = counterPartyTransactionRepository.findById(confirmRequest.getBuySideCounterPartyTransactionId()).get();
			CounterpartyTransactionDetails sellSideCPTradeDetails = counterPartyTransactionRepository.findById(confirmRequest.getSellSideCounterPartyTransactionId()).get();
			
			TransactionDetail buySideCounterPartyTransaction = transactionRepository.findById(buySideCPTradeDetails.getTransactionId().getId()).get();
			
			if(buySideCounterPartyTransaction.getIsConfirmed()) {
				return new ResponseEntity<>(
						HttpResponseModel.builder()
						.message("Transaction already completed which was placed by " + buySideCounterPartyTransaction.getSource().getLongName())
						.status(HttpStatus.OK.value())
						//.timestamp(LocalDateTime.now())
						.build(),
						HttpStatus.OK);
			}
			
			TransactionDetail sellSideCounterPartyTransaction = transactionRepository.findById(sellSideCPTradeDetails.getTransactionId().getId()).get();
			
			if(sellSideCounterPartyTransaction.getIsConfirmed()) {
				return new ResponseEntity<>(
						HttpResponseModel.builder()
						.message("Transaction already completed which was placed by " + sellSideCounterPartyTransaction.getSource().getLongName())
						.status(HttpStatus.OK.value())
						//.timestamp(LocalDateTime.now())
						.build(),
						HttpStatus.OK);
			}
			
			successFlag = transactionService.confirmRequest(confirmRequest);
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
				//.timestamp(LocalDateTime.now())
				.build(),
				successFlag ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
		
	}
}
