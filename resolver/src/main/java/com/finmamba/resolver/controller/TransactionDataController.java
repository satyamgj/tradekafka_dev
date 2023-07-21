package com.finmamba.resolver.controller;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finmamba.entities.transaction.MatchingTable;
import com.finmamba.models.bus.HttpResponseModel;
import com.finmamba.models.dto.CounterPartyTransactionFetchingDto;
import com.finmamba.models.dto.InstitutionalEntityDto;
import com.finmamba.models.matching.NewOrder;
import com.finmamba.models.request.UserProfileModel;
import com.finmamba.models.transaction.MarketDepthRequest;
import com.finmamba.resolver.services.TransactionService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/transaction-data")
@CrossOrigin(origins = "*")
@Slf4j
public class TransactionDataController {

	@Autowired
	TransactionService transactionService;
	
	@Autowired
	ModelMapper modelMapper;
	

	@PostMapping("/getInstitutionalEntity")
	public InstitutionalEntityDto getInstitutionalEntity(@RequestBody UserProfileModel userProfileModel) {
		try {
			return transactionService.findInstitutionalEntityFromUserProfile(userProfileModel);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get all active requests for the Institution 
	 * IMP: Source = instituion ID
	 * @param institutionId
	 * @return
	 */
	@GetMapping("/getActiveRequestsForBank/{institutionId}")
	public ResponseEntity<Object> getActiveRequestsForInstitution(
			@PathVariable(name = "institutionId", required = true) String institutionId,
			@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			@RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
			) {
		
		try {
			return new ResponseEntity<>(transactionService.findActiveRequestsForInstitution(institutionId, page, pageSize),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**	
	 * Get all IN/OUT requests for the Institution
	 * @param transactionID and institutionId
	 * @return
	 */
	@GetMapping("/getInOutConfirmedForInstitution/{transactionId}")
	public ResponseEntity<Object> getInOutForTransaction(
			@PathVariable(name = "transactionId", required = true) String transactionId,
			@RequestParam(name = "institutionId", required = true) String institutionId) {
		
		try {
			return new ResponseEntity<>(transactionService.findInOutForTransaction(transactionId, institutionId),
					HttpStatus.OK);
		} catch (Exception e) {
			e.getStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		
		}
	}


	/**
	 * Get all IN requests for the Institution
	 * @param transactionID and institutionId
	 * @return
	 */
	@GetMapping("/getInForInst/{institutionId}")
	public Object getInForInstitution(
			@PathVariable(name = "institutionId", required = true) String institutionId,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page,
			@RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
			) {

		try {
			logTimeUpdate("getInForInstitution", "START");
			
			HttpResponseModel response = transactionService.findInRequestsForInstitution(institutionId, page, pageSize);
			
			logTimeUpdate("getInForInstitution", "END");

			
			
			return response.getData();
			
		} catch (Exception e) {
			e.printStackTrace();
			log.info("Error inside getInForInstitution " + e.getMessage());
			return HttpResponseModel.builder().data(Collections.emptyList()).status(500).build();
		}
	}
	
	/**
	 * Get all BUY IN requests for the Institution
	 * @param institutionId
	 * @return
	 */
	@GetMapping("/getBuyOrdersInForBroker/{institutionId}")
	public HttpResponseModel getInForBroker(
			@PathVariable(name = "institutionId", required = true) String institutionId,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page,
			@RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
			) {

		Map<String, List<CounterPartyTransactionFetchingDto>> requests = new HashMap<>();

		
		try {
			logTimeUpdate("getBuyInForBroker", "START");
			
			HttpResponseModel response = transactionService.findBuyInRequestsForBroker(institutionId, page, pageSize);
			
			logTimeUpdate("getBuyInForBroker", "END");

			return response;
			
		} catch (Exception e) {
			e.printStackTrace();
			
			requests.put("BUY", Collections.emptyList());

			requests.put("SELL", Collections.emptyList());
			
			log.info("Error inside getInForInstitution " + e.getMessage());
			return null;
		}
	}
	

	/**
	 * Get all BUY IN requests for the Institution
	 * @param institutionId
	 * @return
	 */
	@GetMapping("/getSellOrdersInForBroker/{institutionId}")
	public HttpResponseModel getSellInForBroker(
			@PathVariable(name = "institutionId", required = true) String institutionId,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page,
			@RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
			) {

		Map<String, List<CounterPartyTransactionFetchingDto>> requests = new HashMap<>();

		
		try {
			logTimeUpdate("getInForBroker", "START");
			
			HttpResponseModel response = transactionService.findSellInRequestsForBroker(institutionId, page, pageSize);
			
			logTimeUpdate("getInForBroker", "END");

			return response;
			
		} catch (Exception e) {
			e.printStackTrace();
		
			
			log.info("Error inside getSellInForInstitution " + e.getMessage());
			return null;
		}
	}
	
	@GetMapping("/getConfirmedOrdersForBank/{institutionId}")
	public ResponseEntity<Object> getConfirmedOrdersForInstitution(
			@PathVariable(name = "institutionId", required = true) String institutionId,
			@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			@RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
			) {
		//

		try {
			return new ResponseEntity<>(transactionService.findConfirmedOrdersForInstitution(institutionId, page, pageSize),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/getConfirmedOrdersForBroker/{institutionId}")
	public ResponseEntity<Object> getConfirmedOrdersForBroker(
			@PathVariable(name = "institutionId", required = true) String institutionId,
			@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			@RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
			) {
		//

		try {
			return new ResponseEntity<>(transactionService.findConfirmedOrdersForBroker(institutionId, page, pageSize),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/getDepth")
	public ResponseEntity<Object> getMarketDepth(
			@RequestBody MarketDepthRequest marketDepthRequest
			){
		try {
			return new ResponseEntity<>(transactionService.findDepthOfCurrenctByCounterParty(marketDepthRequest),
					HttpStatus.OK);
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

	@PostMapping("/getMatchingOrders")
	public ResponseEntity<Object> initiateMatching(@RequestBody MatchingTable order){
		try {
			return new ResponseEntity<Object>(transactionService.intiateMatching(order),HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private void logTimeUpdate(String functionName, String status) {
		log.info(functionName + " " + status + " " + Instant.now());
	}

	private void logException(String functionName, String exceptionMessage) {
		log.info("Exception occurred " + functionName + " " + exceptionMessage);
	}
}
