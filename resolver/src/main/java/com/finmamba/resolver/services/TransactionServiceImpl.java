package com.finmamba.resolver.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.entities.transaction.MatchingTable;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.kafka.starter.autoconfigure.rest.KafkaRestService;
import com.finmamba.kafka.starter.util.ApplicationProperties;
import com.finmamba.models.bus.EventType;
import com.finmamba.models.bus.HttpResponseModel;
import com.finmamba.models.bus.SerialDataBusModel;
import com.finmamba.models.dto.InstitutionalEntityDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.request.UserProfileModel;
import com.finmamba.models.transaction.ConfirmRequest;
import com.finmamba.models.transaction.LatestQuoteDetailRequest;
import com.finmamba.models.transaction.MarketDepthRequest;
import com.finmamba.models.transaction.QuoteRequestModel;
import com.finmamba.models.transaction.TransactionRequest;
import com.finmamba.resolver.exception.EntityNotFound;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.util.ApplicationConstants;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {
	@Autowired
	KafkaRestService kafkaRestService;

	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	TransactionServiceDal dal;

	@Autowired
	Gson gson;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	InstitutionalEntityRepository institutionalEntityRepository;

	@Autowired
	EventPublisher eventPublisher;

	@Override
	public List<QuoteRequestDto> processQuoteRequest(QuoteRequestModel quoteRequestModel) throws EntityNotFound {
		log.info("processQuoteRequest | START " + Instant.now());
		try {
			/**
			 * Save Quote Request in database
			 */
			List<QuoteRequestDto> requests = dal.saveRequest(quoteRequestModel);

		log.info("processQuoteRequest | END" + Instant.now());

			return requests;
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("processQuoteRequest | END with empty list" + Instant.now());
		return Collections.emptyList();
	}

	@Override
	public boolean processBrokerRequest(TransactionRequest requestBrokerDispatchRequest) throws EntityNotFound {
		
		log.info("processBrokerRequest | START | " + Instant.now());
		try {
			/**
			 * Save Quote Request from broker with updated spread in database I know parent
			 * id of the request
			 */
			eventPublisher.publishNewBrokerRequestEvent(requestBrokerDispatchRequest);
			
			log.info("processBrokerRequest | END with true | " + Instant.now());
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("processBrokerRequest | END with false");
		return false;
	}

	@Override
	public boolean processCounterpartyResponse(TransactionRequest request, int responseType) throws EntityNotFound {
		log.info("processCounterpartyResponse | START");
		try {
			eventPublisher.publishNewCounterpartyResponseEvent(request, responseType);
			log.info("processCounterpartyResponse | END");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("processCounterpartyResponse | END with false");
		return false;
	}


	private JsonArray getJsonArray(String jsonString) {
		JsonObject convertedObject = gson.fromJson(jsonString, JsonObject.class);
		return convertedObject.getAsJsonArray(ApplicationConstants.PAYLOAD_TAG);
	}

	
	@Override
	public HttpResponseModel findActiveRequestsForInstitution(String institutionName, int page, int pageSize) {
		log.info("Fetching active requests for route id: " + institutionName);
		try {
			return dal.findActiveRequestsForInstitution(institutionName, page, pageSize);
		} catch (EntityNotFound e) {
			// TODO Auto-generated catch block
			return HttpResponseModel.builder()
					.data(Collections.emptyList())
					.message(e.getMessage())
					.build();
		}
	}

	@Override
	public HttpResponseModel findConfirmedOrdersForInstitution(String institutionName, int page, int pageSize) {
		log.info("Fetching Confirmed requests for route id: " + institutionName);
		try {
			return dal.findConfirmedOrdersForInstitution(institutionName, page, pageSize);
		} catch (EntityNotFound e) {
			// TODO Auto-generated catch block
			return HttpResponseModel.builder().data(Collections.emptyList()).message("Exception occured in findConfirmedOrdersForInstitution: "
					+  e.getMessage()).build();
		}
	}
	
	@Override
	public HttpResponseModel findConfirmedOrdersForBroker(String institutionName, int page, int pageSize) {
		log.info("Fetching Confirmed requests for route id: " + institutionName);
		try {
			HttpResponseModel response = dal.findConfirmedOrdersForBroker(institutionName, page, pageSize);
			return response;
		} catch (EntityNotFound e) {
			// TODO Auto-generated catch block
			return HttpResponseModel.builder().data(Collections.emptyList()).message("Exception occured in findConfirmedOrdersForInstitution: "
					+  e.getMessage()).build();
		}
	}

	@Override
	public HttpResponseModel findInOutForTransaction(String transactionId,
			String institutionId) {
		log.info("Fetching in out for transaction id: " + transactionId);
		try {
			return dal.findInOutForTransaction(transactionId, institutionId);
		} catch (EntityNotFound e) {
			// TODO Auto-generated catch block
			return HttpResponseModel.builder().data(Collections.emptyList()).message("Exception occured in findInOutForTransaction: "
					+  e.getMessage()).build();

		}
	}

	private SerialDataBusModel serialDataBusModelBuilder(Object payload, EventType eventType) {
		//@// @formatter:off
		return SerialDataBusModel.builder()
				.payload(payload)
				.eventType(eventType)
				.messageTimestamp(LocalDateTime.now().toString())
				.build();
		// @formatter:on
	}


	@Override
	public InstitutionalEntityDto findInstitutionalEntityFromUserProfile(UserProfileModel userProfileModel) {
		log.info("Fetching institutional entity for user profile: " + userProfileModel);

		return modelMapper.map(
				institutionalEntityRepository.findByRealmNameAndClientRoleNameAndClientNameAndGroupName(
						userProfileModel.getRealmName(), userProfileModel.getClientRoleName(),
						userProfileModel.getClientName(), userProfileModel.getGroupName()),
				InstitutionalEntityDto.class);

	}



	@Override
	public HttpResponseModel findInRequestsForInstitution(String institutionId, int page, int pageSize) {
		try {
			return dal.findInForInstitution(institutionId, page, pageSize);
			
		} catch (EntityNotFound e) {
			e.printStackTrace();
			return HttpResponseModel.builder()
					.data(Collections.emptyList())
					.message(e.getMessage())
					.build();
		}
		
	}

	@Override
	public boolean confirmRequest(ConfirmRequest confirmRequest) {
		log.info("processCounterpartyResponse | START");
		try {
			eventPublisher.publishConfirmRequest(confirmRequest);
			log.info("processCounterpartyResponse | END");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("processCounterpartyResponse | END with false");
		return false;
	}

//	@Override
//	public HttpResponseModel findInRequestsForBroker(String institutionId, int page, int pageSize) {
//		try {
//			
//			logTimeUpdate("findInRequestsForBroker", "start");
//			return dal.findInOrdersForBroker(institutionId, page, pageSize);
//			
//		} catch (EntityNotFound e) {
//			
//			Map<String, Object> requests = new HashMap<>() ;
//			requests.put("BUY", Collections.emptyList());
//			requests.put("SELL", Collections.emptyList());
//
//			e.printStackTrace();
//			return HttpResponseModel.builder()
//					.data(requests)
//					.message(e.getMessage())
//					.build();
//		} catch (Exception e) {
//			Map<String, Object> requests = new HashMap<>() ;
//			requests.put("BUY", Collections.emptyList());
//			requests.put("SELL", Collections.emptyList());
//
//			e.printStackTrace();
//			return HttpResponseModel.builder()
//					.data(requests)
//					.message(e.getMessage())
//					.build();		}
//		
//	}


	private void logTimeUpdate(String functionName, String status) {
		log.info(functionName + " " + status + " " + Instant.now());
	}

	@Override
	public HttpResponseModel findDepthOfCurrenctByCounterParty(MarketDepthRequest marketDepthRequest) {
		
		try {
			return dal.findDepthOfCcyPairByInstituionId(marketDepthRequest);
			
		} catch (Exception e) {
			Map<String, Object> requests = new HashMap<>() ;
			requests.put("BUY", Collections.emptyList());
			requests.put("SELL", Collections.emptyList());

			e.printStackTrace();
			return HttpResponseModel.builder()
					.data(requests)
					.message(e.getMessage())
					.build();	
		}
	}

	@Override
	public HttpResponseModel findBuyInRequestsForBroker(String institutionId, int page, int pageSize) {
		try {
			return dal.findBuyInOrdersForBroker(institutionId, page, pageSize);
			
		} catch (Exception e) {
			return HttpResponseModel.builder()
					.data(Collections.emptyList())
					.message(e.getMessage())
					.build();	
		}
	}

	@Override
	public HttpResponseModel findSellInRequestsForBroker(String institutionId, int page, int pageSize) {
		try {
			HttpResponseModel response = dal.findSellInOrdersForBroker(institutionId, page, pageSize);
			return response;
			
		} catch (Exception e) {
			return HttpResponseModel.builder()
					.data(Collections.emptyList())
					.message(e.getMessage())
					.build();	
		}
	}

	@Override
	public HttpResponseModel intiateMatching(MatchingTable newOrder) {
		try {
			return dal.intiateMatchingAlgorithms(newOrder);
		} catch (Exception e) {
			return HttpResponseModel.builder()
					.message("Something went wrong while matching. Please try again! " + e.getMessage())
					.build();
		}
	}

	@Override
	public HttpResponseModel getLatestQuoteBySource(LatestQuoteDetailRequest latestQuoteDetailRequest) {
		try {
			return dal.getLatestQuoteBySource(latestQuoteDetailRequest);
		} catch (Exception e) {
			return HttpResponseModel.builder()
					.data(Collections.emptyList())
					.message("Something went wrong while fetching getLatestQuoteBySource. Please try again! " + e.getMessage())
					.build();
		}
	}

	@Override
	public HttpResponseModel findInOutForTransactionForBroker(String transactionName, String institutionId) {
		try {
			return dal.findInOutForTransactionForBroker(transactionName, institutionId);
		} catch (Exception e) {
			return HttpResponseModel.builder()
					.data(Collections.emptyList())
					.message("Something went wrong while fetching findInOutForTransactionForBroker. Please try again! " + e.getMessage())
					.build();
		}
	}

	@Override
	public HttpResponseModel getBuyOrdersViaBroker(Long brokerId) {
		try {
			return dal.getBuyOrdersFromBroker(brokerId);
		} catch (Exception e) {
			return HttpResponseModel.builder()
					.data(Collections.emptyList())
					.message("Something went wrong while fetching getBuyOrdersViaBroker. Please try again! " + e.getMessage())
					.build();
		}
	}
	
	@Override
	public HttpResponseModel getSellOrdersViaBroker(Long brokerId) {
		try {
			return dal.getBuyOrdersFromBroker(brokerId);
		} catch (Exception e) {
			return HttpResponseModel.builder()
					.data(Collections.emptyList())
					.message("Something went wrong while fetching getBuyOrdersViaBroker. Please try again! " + e.getMessage())
					.build();
		}
	}


}
