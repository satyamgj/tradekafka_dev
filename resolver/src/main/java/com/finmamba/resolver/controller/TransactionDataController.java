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

import com.finmamba.models.request.UserProfileModel;
import com.finmamba.resolver.services.TransactionService;

@RestController
@RequestMapping("/transaction-data")
@CrossOrigin(origins = "*")
public class TransactionDataController {

	@Autowired
	TransactionService transactionService;

	/**
	 * Get all active requests for the Institution
	 * 
	 * @param institutionId
	 * @return
	 */
	@GetMapping("/getActiveRequestsForInstitution/{institutionName}")
	public ResponseEntity<Object> getActiveRequestsForInstitution(
			@PathVariable(name = "institutionName", required = true) String institutionName,
			@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			@RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
			) {
		//

		try {
			return new ResponseEntity<>(transactionService.findActiveRequestsForInstitution(institutionName, page, pageSize),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getInOutConfimedForTransaction/{transactionId}")
	public ResponseEntity<Object> getInOutForTransaction(
			@PathVariable(name = "transactionId", required = true) String transactionId,
			@RequestParam(name = "institutionName", required = true) String institutionName) {
		//

		try {
			return new ResponseEntity<>(transactionService.findInOutForTransaction(transactionId, institutionName),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getInForInstitution/{institutionName}")
	public ResponseEntity<Object> getInForInstitution(
			@PathVariable(name = "institutionName", required = true) String institutionName,
			@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			@RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
			) {

		try {
			return new ResponseEntity<>(transactionService.findInRequestsForInstitution(institutionName, page, pageSize),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// @GetMapping("/fetchListForTransaction/{transactionId}")
	public ResponseEntity<Object> fetchListForTransaction(
			@PathVariable(name = "transactionId", required = true) String transactionId,
			@RequestParam(name = "institutionId", required = true) long institutionId) {
		try {
			return new ResponseEntity<>(transactionService.findTrailForTransaction(institutionId, transactionId),
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

	@GetMapping("/getDaddyRequest/{transactionId}")
	public ResponseEntity<Object> getDaddyRequestByTransactionId(
			@PathVariable(name = "transactionId", required = true) String transactionId) {
		try {
			return new ResponseEntity<>(transactionService.findDaddyRequestByTransactionId(transactionId),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/getConfirmedOrdersForInstitution/{institutionName}")
	public ResponseEntity<Object> getConfirmedOrdersForInstitution(
			@PathVariable(name = "institutionName", required = true) String institutionName,
			@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			@RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
			) {
		//

		try {
			return new ResponseEntity<>(transactionService.findConfirmedOrdersForInstitution(institutionName, page, pageSize),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
