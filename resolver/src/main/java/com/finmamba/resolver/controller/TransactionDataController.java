package com.finmamba.resolver.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.models.request.UserProfileModel;
import com.finmamba.resolver.repositories.CounterPartyTransactionRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.repositories.TransactionRepository;
import com.finmamba.resolver.services.TransactionService;

@RestController
@RequestMapping("/transaction-data")
@CrossOrigin(origins = "*")
public class TransactionDataController {

	@Autowired
	TransactionService transactionService;
	
	
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
	@GetMapping("/getInOutConfirmedForBank/{transactionId}")
	public ResponseEntity<Object> getInOutForTransaction(
			@PathVariable(name = "transactionId", required = true) String transactionId,
			@RequestParam(name = "institutionName", required = true) String institutionId) {
		
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
	public ResponseEntity<Object> getInForInstitution(
			@PathVariable(name = "institutionId", required = true) String institutionId,
			@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			@RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
			) {

		try {
			return new ResponseEntity<>(transactionService.findInRequestsForInstitution(institutionId, page, pageSize),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

	@GetMapping("/getInstitutionalEntity")
	public ResponseEntity<Object> getInstitutionalEntity(@RequestBody UserProfileModel userProfileModel) {
		try {
			return new ResponseEntity<>(transactionService.findInstitutionalEntityFromUserProfile(userProfileModel),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
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

}
