package com.finmamba.resolver.operations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.Daddy;
import com.finmamba.entities.transaction.KotakWorld;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.KotakWorldDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.resolver.exception.EntityNotFound;
import com.finmamba.resolver.repositories.CurrencyRepository;
import com.finmamba.resolver.repositories.CustomerRepository;
import com.finmamba.resolver.repositories.DaddyRepository;
import com.finmamba.resolver.repositories.DirectionRepository;
import com.finmamba.resolver.repositories.EventTypeRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.repositories.KotakWorldRepository;
import com.finmamba.resolver.repositories.ProductRepository;
import com.finmamba.resolver.repositories.TenorRepository;
import com.finmamba.resolver.repositories.UserRepository;
import com.finmamba.resolver.services.EventPublisher;
import com.finmamba.resolver.services.TransactionServiceDal;
import com.finmamba.resolver.util.ApplicationConstants;

import lombok.Builder;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@Builder
public class KotakWorldOperations implements InstitutionalEntityOperations {

	@Autowired
	KotakWorldRepository kotakWorldRepository;

	@Autowired
	DaddyRepository daddyRepository;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	DirectionRepository directionRepository;

	@Autowired
	EventTypeRepository eventTypeRepository;

	@Autowired
	TenorRepository tenorRepository;

	@Autowired
	InstitutionalEntityRepository institutionalEntityRepository;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	TransactionServiceDal dal;

	@Autowired
	EventPublisher eventPublisher;

	@Autowired
	CurrencyRepository currencyRepository;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	InstitutionalEntityResolver entityResolver;

	@Autowired
	KafkaProducerService kafkaProducerService;
	
	@Autowired
	UserRepository userRepository;

	private static final long INSTITUTIONAL_ENTITY_CODE = 1L;

	@Override
	public SuperInstitutionalEntityDto insertDaddyRequest(DaddyDto daddyDto) {
		log.info("insertDaddyRequest started for " + daddyDto.getSource().getRealmName() + " with transactionId: "
				+ daddyDto.getTransactionId());

		KotakWorld kotakWorld = KotakWorld.builder().transactionId(daddyDto.getTransactionId())
				.customer(customerRepository.getById(daddyDto.getCustomer().getId()))
				.product(productRepository.getById(daddyDto.getProduct().getId()))
				.baseCurrency(currencyRepository.getById(daddyDto.getBaseCurrency().getId()))
				.quoteCurrency(currencyRepository.getById(daddyDto.getQuoteCurrency().getId()))
				.buySellDirection(directionRepository.getById(daddyDto.getBuySellDirection().getId()))
				.notional(daddyDto.getNotional()).buyAmount(daddyDto.getBuyAmount())
				.sellAmount(daddyDto.getSellAmount()).sourceSpotRate(daddyDto.getSourceSpotRate())
				.sourceSpotSpread(daddyDto.getSourceSpotSpread()).destinationSpotRate(daddyDto.getDestinationSpotRate())
				.sourceSwapPoints(daddyDto.getSourceSwapPoints()).destinationSwapRate(daddyDto.getDestinationSwapRate())
				.sourceOutright(daddyDto.getSourceOutright()).spreadOutright(daddyDto.getSpreadOutright())
				.destinationOutright(daddyDto.getDestinationOutright()).isRequestActive(false)
				.destinationId(institutionalEntityRepository.getById(daddyDto.getSource().getId())) // SELF
				.tenorUnit(tenorRepository.getById(1L))
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId()))
				.createdAt(Instant.now())
				.createdBy(userRepository.getById(Long.valueOf(daddyDto.getCreatedBy().getId())))
				.build();
		KotakWorld kotakWorldSaved = kotakWorldRepository.save(kotakWorld);

		KotakWorldDto kotakWorldDto = modelMapper.map(kotakWorldSaved, KotakWorldDto.class);
		kotakWorldDto.setSource(daddyDto.getSource());

		log.info("insertDaddyRequest ended for " + daddyDto.getSource().getRealmName());

		return kotakWorldDto;
	}

	@Override
	public KotakWorld getInstitutionalEntityInstance() {
		return KotakWorld.builder().source(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE)).build();
	}

	@Override
	public InstitutionalEntityOperations getInstitutionalEntityOperationsInstance() {
		return this;
	}

	@Override
	public Boolean dispatchRequestToBrokers(SuperInstitutionalEntityDto entityDto) {

		long sourceId = entityDto.getSource().getId();

		/*
		 * Find all eligible brokers Dispatch the request to all of the
		 */
		try {
			List<InstitutionalEntity> listEligibleBrokers = dal.findEligibleBrokers(sourceId);

			listEligibleBrokers.forEach(broker -> {
				/*
				 * Insert OUT_REQUEST Build object with common data
				 */
				KotakWorld kotakWorld = KotakWorld.builder().transactionId(entityDto.getTransactionId())
						.source(institutionalEntityRepository.findById(1L).get()) // SELF
						.product(productRepository.getById(entityDto.getProduct().getId()))
						.baseCurrency(currencyRepository.getById(entityDto.getBaseCurrency().getId()))
						.customer(customerRepository.getById(entityDto.getCustomer().getId()))
						.quoteCurrency(currencyRepository.getById(entityDto.getQuoteCurrency().getId()))
						.buySellDirection(directionRepository.getById(entityDto.getBuySellDirection().getId()))
						.notional(entityDto.getNotional()).buyAmount(entityDto.getBuyAmount())
						.sellAmount(entityDto.getSellAmount()).sourceSpotRate(entityDto.getSourceSpotRate())
						.sourceSpotSpread(entityDto.getSourceSpotSpread())
						.destinationSpotRate(entityDto.getDestinationSpotRate())
						.sourceSwapPoints(entityDto.getSourceSwapPoints())
						.destinationSwapRate(entityDto.getDestinationSwapRate())
						.sourceOutright(entityDto.getSourceOutright()).spreadOutright(entityDto.getSpreadOutright())
						.isRequestActive(true)
						// .destinationId(institutionalEntityRepository.getById(3L))
						.destinationOutright(entityDto.getDestinationOutright()).tenorUnit(tenorRepository.getById(entityDto.getTenorUnit().getId()))
						.eventType(eventTypeRepository.getById(2L))
						.createdAt(Instant.now())
						.createdBy(userRepository.getById( Long.valueOf(entityDto.getCreatedBy().getId())))
						.build(); // OUT_REQUEST
				kotakWorld.setDestinationId(broker);
				kotakWorldRepository.save(kotakWorld);

				// Insert IN_REQUEST for broker
				InstitutionalEntityOperations destinationOperationsHandler = entityResolver
						.getInstitutionalEntityOperationsHandle(broker.getRealmName());
				SuperInstitutionalEntityDto savedRequestDto = destinationOperationsHandler.insertInRequest(entityDto);

				// Publish on kafka topic
				String kafkaTopic = broker.getRealmName() + "." + broker.getClientName() + "."
						+ broker.getClientRoleName() + "." + broker.getGroupName();

				log.info("Pushing on kafka topic: " + kafkaTopic);

				List<SuperInstitutionalEntityDto> listSavedQuote = new ArrayList<>();
				listSavedQuote.add(savedRequestDto);

				kafkaProducerService.send(listSavedQuote, kafkaTopic, null, null);

			});

			return Boolean.TRUE;

		} catch (EntityNotFound enf) {
			log.error("Entity with id: " + sourceId + " not found");
			return Boolean.FALSE;
		}

	}

	@Override
	public Boolean dispatchRequestToCounterparties(DaddyDto daddyDto, String callerRealmName) {
		return Boolean.FALSE;
	}

	@Override
	public SuperInstitutionalEntityDto insertInRequest(SuperInstitutionalEntityDto entityDto) {
		return null;
	}

	@Override
	public SuperInstitutionalEntityDto insertInRequest(DaddyDto daddyDto) {
		log.info("insertInRequest started for KotakWorld with transactionId: " + daddyDto.getTransactionId());

		KotakWorld kotakWorld = KotakWorld.builder().transactionId(daddyDto.getTransactionId())
				.source(institutionalEntityRepository.findById(daddyDto.getSource().getId()).get())
				.product(productRepository.getById(daddyDto.getProduct().getId()))
				.customer(customerRepository.getById(daddyDto.getCustomer().getId()))
				.baseCurrency(currencyRepository.getById(daddyDto.getBaseCurrency().getId()))
				.quoteCurrency(currencyRepository.getById(daddyDto.getQuoteCurrency().getId()))
				.buySellDirection(directionRepository.getById(daddyDto.getBuySellDirection().getId()))
				.notional(daddyDto.getNotional()).buyAmount(daddyDto.getBuyAmount())
				.sellAmount(daddyDto.getSellAmount()).sourceSpotRate(daddyDto.getSourceSpotRate())
				.sourceSpotSpread(daddyDto.getSourceSpotSpread()).destinationSpotRate(daddyDto.getDestinationSpotRate())
				.sourceSwapPoints(daddyDto.getSourceSwapPoints()).destinationSwapRate(daddyDto.getDestinationSwapRate())
				.sourceOutright(daddyDto.getSourceOutright()).spreadOutright(daddyDto.getSpreadOutright())
				.destinationOutright(daddyDto.getDestinationOutright()).isRequestActive(true)
				.destinationId(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE))
				.tenorUnit(tenorRepository.getById(1L))
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId()))
				.createdAt(Instant.now())
				.createdBy(userRepository.getById( Long.valueOf(daddyDto.getCreatedBy().getId())))
				.build(); // IN_REQUEST
		
				//Check if counterparty is the deal starter i.e buyer
		Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
		kotakWorld.setBrokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip());
		//if(daddyRequest.getSource().getId() == INSTITUTIONAL_ENTITY_CODE) {
			
		kotakWorld.setBuySideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageBaseCurrency()));
		kotakWorld.setSellSideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageBaseCurrency()));
		
			kotakWorld.setBuySideSpread(daddyDto.getBuySideSpread());
			kotakWorld.setBuySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()));
			kotakWorld.setBuySideConversionRate(daddyDto.getBuySideConversionRate());
			kotakWorld.setBuySideBaseAmount(daddyDto.getBuySideBaseAmount());
		//}
		//else {
			kotakWorld.setSellSideSpread(daddyDto.getSellSideSpread());
			kotakWorld.setSellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()));
			kotakWorld.setSellSideConversionRate(daddyDto.getSellSideConversionRate());
			kotakWorld.setSellSideBaseAmount(daddyDto.getSellSideBaseAmount());		
		//	}
		
		KotakWorld savedQuote = kotakWorldRepository.save(kotakWorld);

		KotakWorldDto kotakWorldDto = modelMapper.map(savedQuote, KotakWorldDto.class);

		return kotakWorldDto;
	}

	@Override
	public SuperInstitutionalEntityDto insertOutResponse(DaddyDto daddyDto,String sendToRealmName) {

		try {
			
			Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
			
			if(daddyRequest.getOrderStatus() == "CONFIRMED") {
				return null;
			}
			
			
			EventType inRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId());

			InstitutionalEntity destinationRealm = institutionalEntityRepository.findByRealmName(sendToRealmName);

			
			EventType inNegotiationEvent = eventTypeRepository
					.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId());

			EventType inResponseEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId());

			EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId());

			KotakWorld awaitingRequest = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
					daddyDto.getTransactionId(), 
					institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), 
					awaiting, 
					true);
			
			KotakWorld inRequest = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
					daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
					inRequestEvent, true);

			KotakWorld inNegotiation = kotakWorldRepository
					.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
							institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);

			// IN_RESPONSE
			KotakWorld inResponse = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
					daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
					inResponseEvent, true);

			KotakWorld kotakWorld = KotakWorld.builder().transactionId(daddyDto.getTransactionId())
					.source(institutionalEntityRepository.findById(INSTITUTIONAL_ENTITY_CODE).get())
					.baseCurrency(currencyRepository.getById(daddyDto.getBaseCurrency().getId()))
					.customer(customerRepository.getById(daddyDto.getCustomer().getId()))
					.quoteCurrency(currencyRepository.getById(daddyDto.getQuoteCurrency().getId()))
					.product(productRepository.getById(daddyDto.getProduct().getId()))
					.buySellDirection(directionRepository.getById(daddyDto.getBuySellDirection().getId()))
					.notional(daddyDto.getNotional()).buyAmount(daddyDto.getBuyAmount())
					.sellAmount(daddyDto.getSellAmount()).sourceSpotRate(daddyDto.getSourceSpotRate())
					.sourceSpotSpread(daddyDto.getSourceSpotSpread()).destinationSpotRate(daddyDto.getDestinationSpotRate())
					.sourceSwapPoints(daddyDto.getSourceSwapPoints()).destinationSwapRate(daddyDto.getDestinationSwapRate())
					.sourceOutright(daddyDto.getSourceOutright()).spreadOutright(daddyDto.getSpreadOutright())
					.destinationOutright(daddyDto.getDestinationOutright()).isRequestActive(true)
					.destinationId(institutionalEntityRepository.getById(destinationRealm.getId()))
					.tenorUnit(tenorRepository.getById(1L)).eventType(eventTypeRepository.getById(4L))
					.createdAt(Instant.now())
					.confirmationInitiatedBy(institutionalEntityRepository.findById(daddyDto.getConfirmationInitiatedBy().getId()).get())
					.negotiationInitiatedBy(institutionalEntityRepository.findById(daddyDto.getNegotiationInitiatedBy().getId()).get())
					.createdBy(userRepository.getById( Long.valueOf(daddyDto.getCreatedBy().getId())))
					.build(); // OUT_RESPONSE
			
//			Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
			kotakWorld.setBrokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip());
			//if(daddyRequest.getSource().getId() == INSTITUTIONAL_ENTITY_CODE) {
				kotakWorld.setBuySideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageBaseCurrency()));
				kotakWorld.setSellSideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageBaseCurrency()));				
				
				kotakWorld.setBuySideSpread(daddyDto.getBuySideSpread());
				kotakWorld.setBuySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()));
				kotakWorld.setBuySideConversionRate(daddyDto.getBuySideConversionRate());
				kotakWorld.setBuySideBaseAmount(daddyDto.getBuySideBaseAmount());
			//}
			//else {
				kotakWorld.setSellSideSpread(daddyDto.getSellSideSpread());
				kotakWorld.setSellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()));
				kotakWorld.setSellSideConversionRate(daddyDto.getSellSideConversionRate());
				kotakWorld.setSellSideBaseAmount(daddyDto.getSellSideBaseAmount());		
			//	}

			if (inRequest != null) {
				inRequest.setRequestActive(false);
				kotakWorldRepository.save(inRequest);
				kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId()));
				
			} else if (inNegotiation != null) {
				
				inNegotiation.setRequestActive(false);
				
//				if(daddyDto.getConfirmationInitiatedBy() != null) {
//					kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId()));
//					kotakWorld.setRequestActive(false);
//				}
//				else {
//					kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.AWAITING.getEventId()));
//					kotakWorld.setRequestActive(true);
//				}	
				
				kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId()));
				kotakWorld.setRequestActive(true);
				kotakWorldRepository.save(inNegotiation);
			} 
			else if(awaitingRequest != null) {
				
				awaitingRequest.setRequestActive(false);
				kotakWorldRepository.save(awaitingRequest);
				kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_RESPONSE.getEventId()));
			//	Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
				daddyRequest.setOrderStatus("CONFIRMED");
				daddyRequest.setDestinationOutright(daddyDto.getDestinationOutright());
				daddyRepository.save(daddyRequest);
				
			}
			else if (inResponse != null) { //Redundant
			//	Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
				daddyRequest.setOrderStatus("CONFIRMED");
				daddyRepository.save(daddyRequest);
				inResponse.setRequestActive(false);
				kotakWorld.setConfirmationInitiatedBy(
						institutionalEntityRepository.findById(daddyDto.getConfirmationInitiatedBy().getId()).get()
						);
				kotakWorldRepository.save(inResponse);
			}

			KotakWorld savedQuote = kotakWorldRepository.save(kotakWorld);
			return modelMapper.map(savedQuote, KotakWorldDto.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public SuperInstitutionalEntityDto insertOutNegotiation(DaddyDto daddyDto,String sendToRealmName) {
		
		
		Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
		
		if(daddyRequest.getOrderStatus() == "CONFIRMED") {
			return null;
		}
		
		
		InstitutionalEntity destinationRealm = institutionalEntityRepository.findByRealmName(sendToRealmName);

		
		EventType inRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId());
		EventType inNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId());
		
		
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId());

		
		
		KotakWorld awaitingRequest = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), 
				institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), 
				awaiting, 
				true);
		
		KotakWorld inRequest = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inRequestEvent, true);
		KotakWorld inNegotiation = kotakWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);

		KotakWorld kotakWorld = KotakWorld.builder().transactionId(daddyDto.getTransactionId())
				.source(institutionalEntityRepository.findById(INSTITUTIONAL_ENTITY_CODE).get())
				.negotiationInitiatedBy(
						institutionalEntityRepository.findById(daddyDto.getNegotiationInitiatedBy().getId()).get())
				.baseCurrency(currencyRepository.getById(daddyDto.getBaseCurrency().getId()))
				.customer(customerRepository.getById(daddyDto.getCustomer().getId()))
				.quoteCurrency(currencyRepository.getById(daddyDto.getQuoteCurrency().getId()))
				.product(productRepository.getById(daddyDto.getProduct().getId()))
				.buySellDirection(directionRepository.getById(daddyDto.getBuySellDirection().getId()))
				.notional(daddyDto.getNotional()).buyAmount(daddyDto.getBuyAmount())
				.sellAmount(daddyDto.getSellAmount()).sourceSpotRate(daddyDto.getSourceSpotRate())
				.sourceSpotSpread(daddyDto.getSourceSpotSpread()).destinationSpotRate(daddyDto.getDestinationSpotRate())
				.sourceSwapPoints(daddyDto.getSourceSwapPoints()).destinationSwapRate(daddyDto.getDestinationSwapRate())
				.sourceOutright(daddyDto.getSourceOutright()).spreadOutright(daddyDto.getSpreadOutright())
				.destinationOutright(daddyDto.getDestinationOutright())
				.isRequestActive(true)
				.destinationId(institutionalEntityRepository.getById(destinationRealm.getId()))
				.tenorUnit(tenorRepository.getById(1L))
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId()))
				.createdAt(Instant.now())
				.createdBy(userRepository.getById( Long.valueOf(daddyDto.getCreatedBy().getId())))
				.build(); // OUT_RESPONSE
		
//		Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
		kotakWorld.setBrokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip());
		//if(daddyRequest.getSource().getId() == INSTITUTIONAL_ENTITY_CODE) {
		kotakWorld.setBuySideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageBaseCurrency()));
		kotakWorld.setSellSideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageBaseCurrency()));	
		
			kotakWorld.setBuySideSpread(daddyDto.getBuySideSpread());
			kotakWorld.setBuySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()));
			kotakWorld.setBuySideConversionRate(daddyDto.getBuySideConversionRate());
			kotakWorld.setBuySideBaseAmount(daddyDto.getBuySideBaseAmount());
			//}
			//else {
			kotakWorld.setSellSideSpread(daddyDto.getSellSideSpread());
			kotakWorld.setSellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()));
			kotakWorld.setSellSideConversionRate(daddyDto.getSellSideConversionRate());
			kotakWorld.setSellSideBaseAmount(daddyDto.getSellSideBaseAmount());		
			//	}
		
		if (inRequest != null) {
			inRequest.setRequestActive(false);
			kotakWorldRepository.save(inRequest);
			
		} 
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			kotakWorldRepository.save(awaitingRequest);
			
		}
		else {
			inNegotiation.setRequestActive(false);
			kotakWorldRepository.save(inNegotiation);
		}
		KotakWorld savedQuote = kotakWorldRepository.save(kotakWorld);
		return modelMapper.map(savedQuote, KotakWorldDto.class);
	}

	@Override
	public SuperInstitutionalEntityDto insertInResponse(DaddyDto daddyDto) {

		// deavtivate the request for this response
		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());
		
		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());
		
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());
		
		KotakWorld awaitingRequest = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), 
				institutionalEntityRepository.getById(daddyDto.getSource().getId()), 
				awaiting, 
				true);
		
		KotakWorld outRequest = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(daddyDto.getSource().getId()),
				outRequestEvent, true);
		
		KotakWorld outNegotiation = kotakWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), outNegotiationEvent, true);
	

		KotakWorld kotakWorld = KotakWorld.builder().transactionId(daddyDto.getTransactionId())
				.source(institutionalEntityRepository.findById(daddyDto.getSource().getId()).get())
				.baseCurrency(currencyRepository.getById(daddyDto.getBaseCurrency().getId()))
				.customer(customerRepository.getById(daddyDto.getCustomer().getId()))
				.quoteCurrency(currencyRepository.getById(daddyDto.getQuoteCurrency().getId()))
				.product(productRepository.getById(daddyDto.getProduct().getId()))
				.buySellDirection(directionRepository.getById(daddyDto.getBuySellDirection().getId()))
				.notional(daddyDto.getNotional()).buyAmount(daddyDto.getBuyAmount())
				.sellAmount(daddyDto.getSellAmount()).sourceSpotRate(daddyDto.getSourceSpotRate())
				.sourceSpotSpread(daddyDto.getSourceSpotSpread()).destinationSpotRate(daddyDto.getDestinationSpotRate())
				.sourceSwapPoints(daddyDto.getSourceSwapPoints()).destinationSwapRate(daddyDto.getDestinationSwapRate())
				.sourceOutright(daddyDto.getSourceOutright()).spreadOutright(daddyDto.getSpreadOutright())
				.destinationOutright(daddyDto.getDestinationOutright()).tenorUnit(tenorRepository.getById(1L))
				.isRequestActive(daddyDto.isRequestActive())
				.destinationId(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE))
				.confirmationInitiatedBy(institutionalEntityRepository.findById(daddyDto.getConfirmationInitiatedBy().getId()).get())
				.negotiationInitiatedBy(institutionalEntityRepository.findById(daddyDto.getNegotiationInitiatedBy().getId()).get())
				.createdAt(Instant.now())
				.createdBy(userRepository.getById( Long.valueOf(daddyDto.getCreatedBy().getId())))
				.build();
		// True coz of double confirmation
//		kotakWorld.setEventType(eventTypeRepository.getById(daddyDto.getSource().getId())); // IN_RESPONSE
		
		Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
		kotakWorld.setBrokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip());
		//if(daddyRequest.getSource().getId() == INSTITUTIONAL_ENTITY_CODE) {
		kotakWorld.setBuySideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageBaseCurrency()));
		kotakWorld.setSellSideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageBaseCurrency()));

		
			kotakWorld.setBuySideSpread(daddyDto.getBuySideSpread());
			kotakWorld.setBuySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()));
			kotakWorld.setBuySideConversionRate(daddyDto.getBuySideConversionRate());
			kotakWorld.setBuySideBaseAmount(daddyDto.getBuySideBaseAmount());
			//}
			//else {
			kotakWorld.setSellSideSpread(daddyDto.getSellSideSpread());
			kotakWorld.setSellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()));
			kotakWorld.setSellSideConversionRate(daddyDto.getSellSideConversionRate());
			kotakWorld.setSellSideBaseAmount(daddyDto.getSellSideBaseAmount());		
			//	}
		
		if (outRequest != null) {
			outRequest.setRequestActive(false);
			kotakWorldRepository.save(outRequest);
			kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId()));
			kotakWorld.setRequestActive(true); 
		} 
		else if(awaitingRequest != null) {
			
			awaitingRequest.setRequestActive(false);
			kotakWorldRepository.save(awaitingRequest);
			if(daddyDto.getConfirmationInitiatedBy().getId() == awaitingRequest.getConfirmationInitiatedBy().getId()) {
				
				kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId())); // IN_RESPONSE
				kotakWorld.setRequestActive(false); 

			}
			else {
				kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId())); // IN_RESPONSE
				kotakWorld.setRequestActive(true); 

			}
		}
		else if(outNegotiation != null){
			outNegotiation.setRequestActive(false);
			kotakWorldRepository.save(outNegotiation);
			kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId()));
			kotakWorld.setRequestActive(true); 
		}
		else {
			return null;
		}
		KotakWorld response = kotakWorldRepository.save(kotakWorld);
		KotakWorldDto reponseDto = modelMapper.map(response, KotakWorldDto.class);

		return reponseDto;
	}

	@Override
	public SuperInstitutionalEntityDto insertInNegotiation(DaddyDto daddyDto) {
		// deavtivate the request for this response
		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());
		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());
		
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());

		KotakWorld awaitingRequest = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), 
				 institutionalEntityRepository.getById(daddyDto.getSource().getId()), 
				awaiting, 
				true);
		
		
		KotakWorld outRequest = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(daddyDto.getSource().getId()),
				outRequestEvent, true);
		KotakWorld outNegotiation = kotakWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), outNegotiationEvent, true);
		if (outRequest != null) {
			outRequest.setRequestActive(false);
			kotakWorldRepository.save(outRequest);
		}
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			kotakWorldRepository.save(awaitingRequest);
		}
		else {
			outNegotiation.setRequestActive(false);
			kotakWorldRepository.save(outNegotiation);
		}

		KotakWorld kotakWorld = KotakWorld.builder().transactionId(daddyDto.getTransactionId())
				.source(institutionalEntityRepository.findById(daddyDto.getSource().getId()).get())
				.negotiationInitiatedBy(
						institutionalEntityRepository.findById(daddyDto.getNegotiationInitiatedBy().getId()).get())
				.baseCurrency(currencyRepository.getById(daddyDto.getBaseCurrency().getId()))
				.customer(customerRepository.getById(daddyDto.getCustomer().getId()))
				.quoteCurrency(currencyRepository.getById(daddyDto.getQuoteCurrency().getId()))
				.product(productRepository.getById(daddyDto.getProduct().getId()))
				.buySellDirection(directionRepository.getById(daddyDto.getBuySellDirection().getId()))
				.notional(daddyDto.getNotional()).buyAmount(daddyDto.getBuyAmount())
				.sellAmount(daddyDto.getSellAmount()).sourceSpotRate(daddyDto.getSourceSpotRate())
				.sourceSpotSpread(daddyDto.getSourceSpotSpread()).destinationSpotRate(daddyDto.getDestinationSpotRate())
				.sourceSwapPoints(daddyDto.getSourceSwapPoints()).destinationSwapRate(daddyDto.getDestinationSwapRate())
				.sourceOutright(daddyDto.getSourceOutright()).spreadOutright(daddyDto.getSpreadOutright())
				.destinationOutright(daddyDto.getDestinationOutright()).tenorUnit(tenorRepository.getById(1L))
				.isRequestActive(daddyDto.isRequestActive())
				.destinationId(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE))
				.createdAt(Instant.now())
				.createdBy(userRepository.getById( Long.valueOf(daddyDto.getCreatedBy().getId())))
				.build();
		
		Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
		kotakWorld.setBrokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip());
		//if(daddyRequest.getSource().getId() == INSTITUTIONAL_ENTITY_CODE) {
		kotakWorld.setBuySideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageBaseCurrency()));
		kotakWorld.setSellSideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageBaseCurrency()));

		
			kotakWorld.setBuySideSpread(daddyDto.getBuySideSpread());
			kotakWorld.setBuySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()));
			kotakWorld.setBuySideConversionRate(daddyDto.getBuySideConversionRate());
			kotakWorld.setBuySideBaseAmount(daddyDto.getBuySideBaseAmount());
			//}
			//else {
			kotakWorld.setSellSideSpread(daddyDto.getSellSideSpread());
			kotakWorld.setSellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()));
			kotakWorld.setSellSideConversionRate(daddyDto.getSellSideConversionRate());
			kotakWorld.setSellSideBaseAmount(daddyDto.getSellSideBaseAmount());		
			//}
		
		kotakWorld.setRequestActive(true);
		kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId())); // IN_NEGOTIATION
		KotakWorld response = kotakWorldRepository.save(kotakWorld);
		KotakWorldDto reponseDto = modelMapper.map(response, KotakWorldDto.class);

		return reponseDto;
	}

	@Override
	public SuperInstitutionalEntityDto insertInNegotiation(SuperInstitutionalEntityDto entityDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SuperInstitutionalEntityDto insertOutReject(DaddyDto daddyDto, boolean isBrokerReject,String sendToRealmName) {
		
		Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
		
		if(daddyRequest.getOrderStatus() == "CONFIRMED") {
			return null;
		}
		
		
		
		InstitutionalEntity destinationRealm = institutionalEntityRepository.findByRealmName(sendToRealmName);

		
		EventType inRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId());
		EventType inNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId());
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId());

		KotakWorld awaitingRequest = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), 
				institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), 
				awaiting, 
				true);
		
		
		KotakWorld inRequest = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inRequestEvent, true);
		KotakWorld inNegotiation = kotakWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);
		if (inRequest != null) {
			inRequest.setRequestActive(false);
			kotakWorldRepository.save(inRequest);
		}
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			kotakWorldRepository.save(awaitingRequest);
		}
		else {
			inNegotiation.setRequestActive(false);
			kotakWorldRepository.save(inNegotiation);
		}

		KotakWorld iciciWorld = KotakWorld.builder().transactionId(daddyDto.getTransactionId())
				.source(institutionalEntityRepository.findById(INSTITUTIONAL_ENTITY_CODE).get())
				.baseCurrency(currencyRepository.getById(daddyDto.getBaseCurrency().getId()))
				.customer(customerRepository.getById(daddyDto.getCustomer().getId()))
				.quoteCurrency(currencyRepository.getById(daddyDto.getQuoteCurrency().getId()))
				.product(productRepository.getById(daddyDto.getProduct().getId()))
				.buySellDirection(directionRepository.getById(daddyDto.getBuySellDirection().getId()))
				.notional(daddyDto.getNotional()).buyAmount(daddyDto.getBuyAmount())
				.sellAmount(daddyDto.getSellAmount()).sourceSpotRate(daddyDto.getSourceSpotRate())
				.sourceSpotSpread(daddyDto.getSourceSpotSpread()).destinationSpotRate(daddyDto.getDestinationSpotRate())
				.sourceSwapPoints(daddyDto.getSourceSwapPoints()).destinationSwapRate(daddyDto.getDestinationSwapRate())
				.sourceOutright(daddyDto.getSourceOutright()).spreadOutright(daddyDto.getSpreadOutright())
				.destinationOutright(daddyDto.getDestinationOutright()).isRequestActive(false)
				.destinationId(institutionalEntityRepository.getById(destinationRealm.getId())) // get
				.tenorUnit(tenorRepository.getById(1L))
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_REJECT.getEventId()))
				.createdAt(Instant.now())
				.createdBy(userRepository.getById( Long.valueOf(daddyDto.getCreatedBy().getId())))
				.build(); // OUT_RESPONSE
		
//		Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
		iciciWorld.setBrokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip());
		//if(daddyRequest.getSource().getId() == INSTITUTIONAL_ENTITY_CODE) {
			
		iciciWorld.setBuySideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageBaseCurrency()));
		iciciWorld.setSellSideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageBaseCurrency()));	
		
			iciciWorld.setBuySideSpread(daddyDto.getBuySideSpread());
			iciciWorld.setBuySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()));
			iciciWorld.setBuySideConversionRate(daddyDto.getBuySideConversionRate());
			iciciWorld.setBuySideBaseAmount(daddyDto.getBuySideBaseAmount());
			//}
			//else {
			iciciWorld.setSellSideSpread(daddyDto.getSellSideSpread());
			iciciWorld.setSellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()));
			iciciWorld.setSellSideConversionRate(daddyDto.getSellSideConversionRate());
			iciciWorld.setSellSideBaseAmount(daddyDto.getSellSideBaseAmount());		
			//	}
		KotakWorld savedQuote = kotakWorldRepository.save(iciciWorld);
		KotakWorldDto result = modelMapper.map(savedQuote, KotakWorldDto.class);
		return result;
	}

	@Override
	public SuperInstitutionalEntityDto insertInReject(DaddyDto daddyDto) {
		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());
		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());
		
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());

		KotakWorld awaitingRequest = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), 
				institutionalEntityRepository.getById(daddyDto.getSource().getId()), 
				awaiting, 
				true);
		
		KotakWorld outRequest = kotakWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(daddyDto.getSource().getId()),
				outRequestEvent, true);
		KotakWorld outNegotiation = kotakWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), outNegotiationEvent, true);
		if (outRequest != null) {
			outRequest.setRequestActive(false);
			kotakWorldRepository.save(outRequest);
		}
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			kotakWorldRepository.save(awaitingRequest);
		}
		else {
			outNegotiation.setRequestActive(false);
			kotakWorldRepository.save(outNegotiation);
		}

		KotakWorld kotakWorld = KotakWorld.builder().transactionId(daddyDto.getTransactionId())
				.source(institutionalEntityRepository.findById(daddyDto.getSource().getId()).get())
				.baseCurrency(currencyRepository.getById(daddyDto.getBaseCurrency().getId()))
				.customer(customerRepository.getById(daddyDto.getCustomer().getId()))
				.quoteCurrency(currencyRepository.getById(daddyDto.getQuoteCurrency().getId()))
				.product(productRepository.getById(daddyDto.getProduct().getId()))
				.buySellDirection(directionRepository.getById(daddyDto.getBuySellDirection().getId()))
				.notional(daddyDto.getNotional()).buyAmount(daddyDto.getBuyAmount())
				.sellAmount(daddyDto.getSellAmount()).sourceSpotRate(daddyDto.getSourceSpotRate())
				.sourceSpotSpread(daddyDto.getSourceSpotSpread()).destinationSpotRate(daddyDto.getDestinationSpotRate())
				.sourceSwapPoints(daddyDto.getSourceSwapPoints()).destinationSwapRate(daddyDto.getDestinationSwapRate())
				.sourceOutright(daddyDto.getSourceOutright()).spreadOutright(daddyDto.getSpreadOutright())
				.destinationOutright(daddyDto.getDestinationOutright()).tenorUnit(tenorRepository.getById(1L))
				.destinationId(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE)) 
				.createdAt(Instant.now())
				.createdBy(userRepository.getById( Long.valueOf(daddyDto.getCreatedBy().getId())))
				.build();
		//Check if counterparty is the deal starter i.e buyer
		Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
		kotakWorld.setBrokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip());
		//if(daddyRequest.getSource().getId() == INSTITUTIONAL_ENTITY_CODE) {
		kotakWorld.setBuySideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageBaseCurrency()));
		kotakWorld.setSellSideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageBaseCurrency()));

		
			kotakWorld.setBuySideSpread(daddyDto.getBuySideSpread());
			kotakWorld.setBuySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()));
			kotakWorld.setBuySideConversionRate(daddyDto.getBuySideConversionRate());
			kotakWorld.setBuySideBaseAmount(daddyDto.getBuySideBaseAmount());
			//}
			//else {
			kotakWorld.setSellSideSpread(daddyDto.getSellSideSpread());
			kotakWorld.setSellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()));
			kotakWorld.setSellSideConversionRate(daddyDto.getSellSideConversionRate());
			kotakWorld.setSellSideBaseAmount(daddyDto.getSellSideBaseAmount());		
			//}
		kotakWorld.setRequestActive(false);
		kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_REJECT.getEventId())); // IN_RESPONSE
		KotakWorld response = kotakWorldRepository.save(kotakWorld);
		KotakWorldDto reponseDto = modelMapper.map(response, KotakWorldDto.class);

		return reponseDto;
	}

	@Override
	public SuperInstitutionalEntityDto insertInReject(SuperInstitutionalEntityDto entityDto) {
		return null;
	}

}
