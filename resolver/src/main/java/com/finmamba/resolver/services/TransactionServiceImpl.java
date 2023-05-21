package com.finmamba.resolver.services;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.entities.transaction.Daddy;
import com.finmamba.entities.transaction.RequestQuote;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.kafka.starter.autoconfigure.rest.KafkaRestService;
import com.finmamba.kafka.starter.util.ApplicationProperties;
import com.finmamba.models.bus.EventType;
import com.finmamba.models.bus.SerialDataBusModel;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.InstitutionalEntityDto;
import com.finmamba.models.dto.KanjiWorldDto;
import com.finmamba.models.dto.KotakWorldDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.models.request.UserProfileModel;
import com.finmamba.models.transaction.FXRequestModel;
import com.finmamba.models.transaction.QuoteRequestModel;
import com.finmamba.resolver.exception.EntityNotFound;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.util.ApplicationConstants;
import com.google.common.reflect.TypeToken;
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
	public boolean processBrokerRequest(DaddyDto request, String callerRealmName) throws EntityNotFound {
		
		log.info("processBrokerRequest | START | " + Instant.now());
		try {
			/**
			 * Save Quote Request from broker with updated spread in database I know parent
			 * id of the request
			 */
			eventPublisher.publishNewBrokerRequestEvent(request, callerRealmName);
			log.info("processBrokerRequest | END with true | " + Instant.now());
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("processBrokerRequest | END with false");
		return false;
	}

	@Override
	public boolean processCounterpartyResponse(DaddyDto request, int responseType, String callerRealmName,
			String sendToRealmName) throws EntityNotFound {
		log.info("processCounterpartyResponse | START");
		try {

			// decide who is the source
			Daddy daddyRequest = dal.findDaddyRequestByTransactionId(request.getTransactionId());
			long requestInitiatorId = daddyRequest.getSource().getId().longValue();
			long callerId = request.getSource().getId().longValue();

			// This condition is checked inside the event listener
			if (requestInitiatorId == callerId) {
				// This means that the caller is responding to a negotiation request

			} else {
				// This means that the caller is responding to a quote request
			}

			eventPublisher.publishNewCounterpartyResponseEvent(request, responseType, callerRealmName, sendToRealmName);
			log.info("processCounterpartyResponse | END");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("processCounterpartyResponse | END with false");
		return false;
	}

	@Override
	public boolean processBrokerResponse(DaddyDto quote, String callerRealmName) throws EntityNotFound {
		log.info("processBrokerResponse | START | " + Instant.now());
		try {

			eventPublisher.publishNewBrokerResponseEvent(quote, callerRealmName);
			log.info("processBrokerResponse | END");
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("processBrokerResponse | END | " + Instant.now());
		return false;
	}

	@Override
	public boolean processBrokerResponse(DaddyDto quote, String callerRealmName, int responseType,
			String sendToRealmName) throws EntityNotFound {
		log.info("processBrokerResponse | START | " + Instant.now());
		try {

			eventPublisher.publishNewBrokerResponseEvent(quote, callerRealmName, responseType, sendToRealmName);
			log.info("processBrokerResponse | END");
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("processBrokerResponse | END | " + Instant.now());
		return false;
	}

	@Override
	public boolean processNegotiationResponse(DaddyDto negotiation, int responseType) throws EntityNotFound {
		log.info("processNegotiationResponse | START | " + Instant.now());
		try {

			KotakWorldDto kotakWorldDto = modelMapper.map(negotiation, KotakWorldDto.class);
			eventPublisher.publishEvent(kotakWorldDto, responseType);
			log.info("processNegotiationResponse | END");
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("processNegotiationResponse | END | " + Instant.now());
		return false;
	}

	@Override
	public boolean processNegotiationBrokerResponse(DaddyDto negotiation, int responseType) throws EntityNotFound {
		log.info("processNegotiationResponse | START | " + Instant.now());
		try {

			KanjiWorldDto kanjiWorldDto = modelMapper.map(negotiation, KanjiWorldDto.class);
			eventPublisher.publishEvent(kanjiWorldDto, responseType);
			log.info("processNegotiationResponse | END");
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("processNegotiationResponse | END | " + Instant.now());
		return false;
	}

	private JsonArray getJsonArray(String jsonString) {
		JsonObject convertedObject = gson.fromJson(jsonString, JsonObject.class);
		return convertedObject.getAsJsonArray(ApplicationConstants.PAYLOAD_TAG);
	}

	@Override
	public boolean forwardRequestToBroker(FXRequestModel fxModel, List<RequestQuote> requests) throws EntityNotFound {
		// Publish request to be displayed on broker view
		log.info("forwardRequestToBroker | START");

		/**
		 * 1 -> 2 -> 3
		 */
		// Publish request on broker topic
		requests.forEach(request -> kafkaProducerService.send(request,
				request.getDestination().getRealmName() + "." + request.getDestination().getClientName() + "."
						+ request.getDestination().getClientRoleName() + "." + request.getDestination().getGroupName(),
				null, null));
		log.info("forwardRequestToBroker | START");
		return true;
	}

	@Override
	public Map<String, Object> findActiveRequestsForInstitution(String institutionName, int page, int pageSize) {
		log.info("Fetching active requests for route id: " + institutionName);
		try {
			return dal.findActiveRequestsForInstitution(institutionName, page, pageSize);
		} catch (EntityNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Object> findConfirmedOrdersForInstitution(String institutionName, int page, int pageSize) {
		log.info("Fetching Confirmed requests for route id: " + institutionName);
		try {
			return dal.findConfirmedOrdersForInstitution(institutionName, page, pageSize);
		} catch (EntityNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Collections.emptyMap();
	}

	@Override
	public Map<String, List<SuperInstitutionalEntityDto>> findInOutForTransaction(String transactionId,
			String institutionName) {
//	public Map<String, Object> findInOutForTransaction(String transactionId, String institutionName) {
		log.info("Fetching in out for transaction id: " + transactionId);
		try {
			return dal.findInOutForTransaction(transactionId, institutionName);
		} catch (EntityNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Collections.emptyMap();
	}

	@Override
//	public Map<String, List<RequestQuote>> findInTransitRequestsForInstitution(long institutionId) {
	public List<RequestQuote> findInTransitRequestsForInstitution(long institutionId) {
		log.info("Fetching in transit requests for route id: " + institutionId);
		String quoteRequestJson = kafkaRestService.send(
				serialDataBusModelBuilder(institutionId, EventType.FIND_INTRANSIT_FOR_INSTITUTION),
				applicationProperties.getProducerTopicName());

//		Type typeListRequestQuote = new TypeToken<Map<String, List<RequestQuote>>>() {
//			private static final long serialVersionUID = 1L;
//		}.getType();

		Type typeListRequestQuote = new TypeToken<List<RequestQuote>>() {
			private static final long serialVersionUID = 1L;
		}.getType();
		JsonArray payloadObject = getJsonArray(quoteRequestJson);

//		Map<String, List<RequestQuote>> requests = gson.fromJson(payloadObject, typeListRequestQuote);
		List<RequestQuote> requests = gson.fromJson(payloadObject, typeListRequestQuote);
		return requests;
	}

	@Override
//	public Map<String, List<RequestQuote>> findInTransitRequestsForInstitution(long institutionId) {
	public List<RequestQuote> findTrailForTransaction(Long institutionId, String transactionId) {
		log.info("Fetching in transit requests for route id: " + institutionId);
		Map<String, Object> messageAttributes = new HashMap<>();
		messageAttributes.put("institutionId", institutionId);
		messageAttributes.put("transactionId", transactionId);
		String quoteRequestJson = kafkaRestService.send(
				serialDataBusModelBuilder(messageAttributes, EventType.FIND_INTRANSIT_FOR_TRANSACTION),
				applicationProperties.getProducerTopicName());

		Type typeListRequestQuote = new TypeToken<List<RequestQuote>>() {
			private static final long serialVersionUID = 1L;
		}.getType();
		JsonArray payloadObject = getJsonArray(quoteRequestJson);

		List<RequestQuote> requests = gson.fromJson(payloadObject, typeListRequestQuote);
		return requests;
	}

	@Override
	public List<RequestQuote> findExpiredRequestsForInstitution(long institutionId) {
		log.info("Fetching in transit requests for route id: " + institutionId);
		String quoteRequestJson = kafkaRestService.send(
				serialDataBusModelBuilder(institutionId, EventType.FIND_EXPIRED_FOR_INSTITUTION),
				applicationProperties.getProducerTopicName());
		Type typeListRequestQuote = new TypeToken<List<RequestQuote>>() {
			private static final long serialVersionUID = 1L;
		}.getType();
		JsonArray payloadObject = getJsonArray(quoteRequestJson);

		List<RequestQuote> requests = gson.fromJson(payloadObject, typeListRequestQuote);
		return requests;
	}

	@Override
	public List<RequestQuote> findConfirmedRequestsForInstitution(long institutionId) {
		log.info("Fetching in transit requests for route id: " + institutionId);
		String quoteRequestJson = kafkaRestService.send(
				serialDataBusModelBuilder(institutionId, EventType.FIND_CONFIRM_FOR_INSTITUTION),
				applicationProperties.getProducerTopicName());
		Type typeListRequestQuote = new TypeToken<List<RequestQuote>>() {
			private static final long serialVersionUID = 1L;
		}.getType();
		JsonArray payloadObject = getJsonArray(quoteRequestJson);

		List<RequestQuote> requests = gson.fromJson(payloadObject, typeListRequestQuote);
		return requests;
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
	public boolean forwardRequestToCounterparties(List<RequestQuote> counterPartyRequests) throws EntityNotFound {
		log.info("Publishing request to counterparties");

		counterPartyRequests.forEach(counterpartyRequest -> kafkaProducerService.send(counterpartyRequest,
				counterpartyRequest.getDestination().getRealmName() + "."
						+ counterpartyRequest.getDestination().getClientName() + "."
						+ counterpartyRequest.getDestination().getClientRoleName() + "."
						+ counterpartyRequest.getDestination().getGroupName(),
				null, null));

		return true;
	}

	@Override
	public boolean expireOrder(String transactionId, String reasonForExpiry) {
		try {
			return dal.expireOrderRequest(transactionId, reasonForExpiry);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return false;
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
	public DaddyDto findDaddyRequestByTransactionId(String transactionId) {
		log.info("Fetching Daddy request for transaction id: " + transactionId);

		return modelMapper.map(dal.findDaddyRequestByTransactionId(transactionId), DaddyDto.class);

	}

	@Override
	public Map<String, Object> findInRequestsForInstitution(String institutionName, int page, int pageSize) {
		try {
			return dal.findInForInstitution(institutionName, page, pageSize);
		} catch (EntityNotFound e) {
			e.printStackTrace();
		}
		return Collections.emptyMap();
	}
}
