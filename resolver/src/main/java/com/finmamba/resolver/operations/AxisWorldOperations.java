package com.finmamba.resolver.operations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.AxisWorld;
import com.finmamba.entities.transaction.Daddy;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.AxisWorldDto;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.resolver.exception.EntityNotFound;
import com.finmamba.resolver.repositories.AxisWorldRepository;
import com.finmamba.resolver.repositories.CurrencyRepository;
import com.finmamba.resolver.repositories.CustomerRepository;
import com.finmamba.resolver.repositories.DaddyRepository;
import com.finmamba.resolver.repositories.DirectionRepository;
import com.finmamba.resolver.repositories.EventTypeRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
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
public class AxisWorldOperations implements InstitutionalEntityOperations {

	@Autowired
	AxisWorldRepository axisWorldRepository;

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

		AxisWorld axisWorld = AxisWorld.builder().transactionId(daddyDto.getTransactionId())
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
		AxisWorld axisWorldSaved = axisWorldRepository.save(axisWorld);

		AxisWorldDto axisWorldDto = modelMapper.map(axisWorldSaved, AxisWorldDto.class);
		axisWorldDto.setSource(daddyDto.getSource());

		log.info("insertDaddyRequest ended for " + daddyDto.getSource().getRealmName());

		return axisWorldDto;
	}

	@Override
	public AxisWorld getInstitutionalEntityInstance() {
		return AxisWorld.builder().source(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE)).build();
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
				AxisWorld axisWorld = AxisWorld.builder().transactionId(entityDto.getTransactionId())
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
				axisWorld.setDestinationId(broker);
				axisWorldRepository.save(axisWorld);

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
		log.info("insertInRequest started for AxisWorld with transactionId: " + daddyDto.getTransactionId());

		AxisWorld axisWorld = AxisWorld.builder().transactionId(daddyDto.getTransactionId())
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
		AxisWorld savedQuote = axisWorldRepository.save(axisWorld);

		AxisWorldDto axisWorldDto = modelMapper.map(savedQuote, AxisWorldDto.class);

		return axisWorldDto;
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

			AxisWorld awaitingRequest = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
					daddyDto.getTransactionId(), 
					institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), 
					awaiting, 
					true);
			
			AxisWorld inRequest = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
					daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
					inRequestEvent, true);

			AxisWorld inNegotiation = axisWorldRepository
					.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
							institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);

			// IN_RESPONSE
			AxisWorld inResponse = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
					daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
					inResponseEvent, true);

			AxisWorld axisWorld = AxisWorld.builder().transactionId(daddyDto.getTransactionId())
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

			if (inRequest != null) {
				inRequest.setRequestActive(false);
				axisWorldRepository.save(inRequest);
				axisWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId()));
				
			} else if (inNegotiation != null) {
				
				inNegotiation.setRequestActive(false);
				
//				if(daddyDto.getConfirmationInitiatedBy() != null) {
//					axisWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId()));
//					axisWorld.setRequestActive(false);
//				}
//				else {
//					axisWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.AWAITING.getEventId()));
//					axisWorld.setRequestActive(true);
//				}	
				
				axisWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId()));
				axisWorld.setRequestActive(true);
				axisWorldRepository.save(inNegotiation);
			} 
			else if(awaitingRequest != null) {
				
				awaitingRequest.setRequestActive(false);
				axisWorldRepository.save(awaitingRequest);
				axisWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_RESPONSE.getEventId()));
			//	Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
				daddyRequest.setDestinationOutright(daddyDto.getDestinationOutright());
				daddyRequest.setOrderStatus("CONFIRMED");
				daddyRepository.save(daddyRequest);
				
			}
			else if (inResponse != null) { //Redundant
			//	Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
				daddyRequest.setOrderStatus("CONFIRMED");
				daddyRepository.save(daddyRequest);
				inResponse.setRequestActive(false);
				axisWorld.setConfirmationInitiatedBy(
						institutionalEntityRepository.findById(daddyDto.getConfirmationInitiatedBy().getId()).get()
						);
				axisWorldRepository.save(inResponse);
			}

			AxisWorld savedQuote = axisWorldRepository.save(axisWorld);
			return modelMapper.map(savedQuote, AxisWorldDto.class);
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

		
		
		AxisWorld awaitingRequest = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), 
				institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), 
				awaiting, 
				true);
		
		AxisWorld inRequest = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inRequestEvent, true);
		AxisWorld inNegotiation = axisWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);

		AxisWorld axisWorld = AxisWorld.builder().transactionId(daddyDto.getTransactionId())
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
		if (inRequest != null) {
			inRequest.setRequestActive(false);
			axisWorldRepository.save(inRequest);
			
		} 
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			axisWorldRepository.save(awaitingRequest);
			
		}
		else {
			inNegotiation.setRequestActive(false);
			axisWorldRepository.save(inNegotiation);
		}
		AxisWorld savedQuote = axisWorldRepository.save(axisWorld);
		return modelMapper.map(savedQuote, AxisWorldDto.class);
	}

	@Override
	public SuperInstitutionalEntityDto insertInResponse(DaddyDto daddyDto) {

		// deavtivate the request for this response
		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());
		
		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());
		
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());
		
		AxisWorld awaitingRequest = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), 
				institutionalEntityRepository.getById(daddyDto.getSource().getId()), 
				awaiting, 
				true);
		
		AxisWorld outRequest = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(daddyDto.getSource().getId()),
				outRequestEvent, true);
		
		AxisWorld outNegotiation = axisWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), outNegotiationEvent, true);
	

		AxisWorld axisWorld = AxisWorld.builder().transactionId(daddyDto.getTransactionId())
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
//		axisWorld.setEventType(eventTypeRepository.getById(daddyDto.getSource().getId())); // IN_RESPONSE
		
		if (outRequest != null) {
			outRequest.setRequestActive(false);
			axisWorldRepository.save(outRequest);
			axisWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId()));
			axisWorld.setRequestActive(true); 
		} 
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			axisWorldRepository.save(awaitingRequest);
			if(daddyDto.getConfirmationInitiatedBy().getId() == awaitingRequest.getConfirmationInitiatedBy().getId()) {
				axisWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId())); // IN_RESPONSE
				axisWorld.setRequestActive(false); 

			}
			else {
				axisWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId())); // IN_RESPONSE
				axisWorld.setRequestActive(true); 

			}
		}
		else if(outNegotiation != null){
			outNegotiation.setRequestActive(false);
			axisWorldRepository.save(outNegotiation);
			axisWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId()));
			axisWorld.setRequestActive(true); 
		}
		else {
			return null;
		}
		AxisWorld response = axisWorldRepository.save(axisWorld);
		AxisWorldDto reponseDto = modelMapper.map(response, AxisWorldDto.class);

		return reponseDto;
	}

	@Override
	public SuperInstitutionalEntityDto insertInNegotiation(DaddyDto daddyDto) {
		// deavtivate the request for this response
		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());
		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());
		
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());

		AxisWorld awaitingRequest = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), 
				 institutionalEntityRepository.getById(daddyDto.getSource().getId()), 
				awaiting, 
				true);
		
		
		AxisWorld outRequest = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(daddyDto.getSource().getId()),
				outRequestEvent, true);
		AxisWorld outNegotiation = axisWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), outNegotiationEvent, true);
		if (outRequest != null) {
			outRequest.setRequestActive(false);
			axisWorldRepository.save(outRequest);
		}
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			axisWorldRepository.save(awaitingRequest);
		}
		else {
			outNegotiation.setRequestActive(false);
			axisWorldRepository.save(outNegotiation);
		}

		AxisWorld axisWorld = AxisWorld.builder().transactionId(daddyDto.getTransactionId())
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
		axisWorld.setRequestActive(true);
		axisWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId())); // IN_NEGOTIATION
		AxisWorld response = axisWorldRepository.save(axisWorld);
		AxisWorldDto reponseDto = modelMapper.map(response, AxisWorldDto.class);

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

		AxisWorld awaitingRequest = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), 
				institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), 
				awaiting, 
				true);
		
		
		AxisWorld inRequest = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inRequestEvent, true);
		AxisWorld inNegotiation = axisWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);
		if (inRequest != null) {
			inRequest.setRequestActive(false);
			axisWorldRepository.save(inRequest);
		}
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			axisWorldRepository.save(awaitingRequest);
		}
		else {
			inNegotiation.setRequestActive(false);
			axisWorldRepository.save(inNegotiation);
		}

		AxisWorld iciciWorld = AxisWorld.builder().transactionId(daddyDto.getTransactionId())
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
		AxisWorld savedQuote = axisWorldRepository.save(iciciWorld);
		AxisWorldDto result = modelMapper.map(savedQuote, AxisWorldDto.class);
		return result;
	}

	@Override
	public SuperInstitutionalEntityDto insertInReject(DaddyDto daddyDto) {
		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());
		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());
		
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());

		AxisWorld awaitingRequest = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), 
				institutionalEntityRepository.getById(daddyDto.getSource().getId()), 
				awaiting, 
				true);
		
		AxisWorld outRequest = axisWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(daddyDto.getSource().getId()),
				outRequestEvent, true);
		AxisWorld outNegotiation = axisWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), outNegotiationEvent, true);
		if (outRequest != null) {
			outRequest.setRequestActive(false);
			axisWorldRepository.save(outRequest);
		}
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			axisWorldRepository.save(awaitingRequest);
		}
		else {
			outNegotiation.setRequestActive(false);
			axisWorldRepository.save(outNegotiation);
		}

		AxisWorld axisWorld = AxisWorld.builder().transactionId(daddyDto.getTransactionId())
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
		axisWorld.setRequestActive(false);
		axisWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_REJECT.getEventId())); // IN_RESPONSE
		AxisWorld response = axisWorldRepository.save(axisWorld);
		AxisWorldDto reponseDto = modelMapper.map(response, AxisWorldDto.class);

		return reponseDto;
	}

	@Override
	public SuperInstitutionalEntityDto insertInReject(SuperInstitutionalEntityDto entityDto) {
		return null;
	}

}
