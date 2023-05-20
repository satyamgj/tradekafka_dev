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
import com.finmamba.entities.transaction.IciciWorld;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.IciciWorldDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.resolver.exception.EntityNotFound;
import com.finmamba.resolver.repositories.CurrencyRepository;
import com.finmamba.resolver.repositories.CustomerRepository;
import com.finmamba.resolver.repositories.DaddyRepository;
import com.finmamba.resolver.repositories.DirectionRepository;
import com.finmamba.resolver.repositories.EventTypeRepository;
import com.finmamba.resolver.repositories.IciciWorldRepository;
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
public class IciciWorldOperations implements InstitutionalEntityOperations {

	@Autowired
	IciciWorldRepository iciciWorldRepository;

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
	UserRepository userRepository;

	@Autowired
	KafkaProducerService kafkaProducerService;
	
	@Autowired
	DaddyRepository daddyRepository;

	private static final long INSTITUTIONAL_ENTITY_CODE = 2L;

	@Override
	public SuperInstitutionalEntityDto insertDaddyRequest(DaddyDto daddyDto) {
		log.info("insertDaddyRequest started for " + daddyDto.getSource().getRealmName() + " with transactionId: "
				+ daddyDto.getTransactionId());

		IciciWorld iciciWorld = IciciWorld.builder().transactionId(daddyDto.getTransactionId())
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
				.createdBy(userRepository.getById( Long.valueOf(daddyDto.getCreatedBy().getId())))
				.build();
		IciciWorld iciciWorldSaved = iciciWorldRepository.save(iciciWorld);

		IciciWorldDto icicWorldDto = modelMapper.map(iciciWorldSaved, IciciWorldDto.class);
		icicWorldDto.setSource(daddyDto.getSource());

		log.info("insertDaddyRequest ended for " + daddyDto.getSource().getRealmName());

		return icicWorldDto;
	}

	@Override
	public IciciWorld getInstitutionalEntityInstance() {
		return IciciWorld.builder().source(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE)).build();
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
				 * Insert OUT_REQUEST* Build object with common data
				 */
				IciciWorld iciciWorld = IciciWorld.builder().transactionId(entityDto.getTransactionId())
						.source(institutionalEntityRepository.findById(INSTITUTIONAL_ENTITY_CODE).get()) // SELF
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
				iciciWorld.setDestinationId(broker);
				iciciWorldRepository.save(iciciWorld);

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SuperInstitutionalEntityDto insertInRequest(SuperInstitutionalEntityDto entityDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SuperInstitutionalEntityDto insertInRequest(DaddyDto daddyDto) {
		log.info("insertInRequest started for KanjiWorld with transactionId: " + daddyDto.getTransactionId());

		IciciWorld iciciWorld = IciciWorld.builder().transactionId(daddyDto.getTransactionId())
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
		
		iciciWorld.setBrokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip());
		Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
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
			iciciWorld.setSellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()));
			iciciWorld.setSellSideConversionRate(daddyDto.getSellSideConversionRate());
			iciciWorld.setSellSideBaseAmount(daddyDto.getSellSideBaseAmount());		
		//	}
		
		IciciWorld savedQuote = iciciWorldRepository.save(iciciWorld);

		IciciWorldDto iciciWorldDto = modelMapper.map(savedQuote, IciciWorldDto.class);

		return iciciWorldDto;
	}

	@Override
	public SuperInstitutionalEntityDto insertOutResponse(DaddyDto daddyDto,String sendToRealmName) {

		Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
		
		if(daddyRequest.getOrderStatus() == "CONFIRMED") {
			return null;
		}
		
		InstitutionalEntity destinationRealm = institutionalEntityRepository.findByRealmName(sendToRealmName);

		
		EventType inRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId());
		EventType inNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId());
		EventType inResponseEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId());
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId());
	
		
		IciciWorld awaitingRequest = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
										daddyDto.getTransactionId()
										,institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE) 
										, awaiting
										, true
										);
		
		IciciWorld inRequest = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inRequestEvent, true);
		
		IciciWorld inNegotiation = iciciWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);

		IciciWorld inResponse = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
							daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
							inResponseEvent, true);
		
		IciciWorld iciciWorld = IciciWorld.builder().transactionId(daddyDto.getTransactionId())
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
				.destinationOutright(daddyDto.getDestinationOutright())
				.isRequestActive(true) // Wait for double confirmation
				.destinationId(institutionalEntityRepository.getById(destinationRealm.getId())) // get
				.tenorUnit(tenorRepository.getById(1L))
				.confirmationInitiatedBy(institutionalEntityRepository.findById(daddyDto.getConfirmationInitiatedBy().getId()).get())
				.createdAt(Instant.now())
				.createdBy(userRepository.getById( Long.valueOf(daddyDto.getCreatedBy().getId())))
				.build(); 

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
		
		if (inRequest != null) {
			inRequest.setRequestActive(false);
			iciciWorldRepository.save(inRequest);
			iciciWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId()));

		}
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			iciciWorldRepository.save(awaitingRequest);
			iciciWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_RESPONSE.getEventId()));
			daddyRequest.setDestinationOutright(daddyDto.getDestinationOutright());
			//Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
			daddyRequest.setOrderStatus("CONFIRMED");
			daddyRepository.save(daddyRequest);
		}
		else if (inNegotiation != null){
			inNegotiation.setRequestActive(false);
			iciciWorld.setNegotiationInitiatedBy(
					institutionalEntityRepository.findById(daddyDto.getNegotiationInitiatedBy().getId()).get());
			iciciWorldRepository.save(inNegotiation);
			
//			//will check this
//			if(daddyDto.getConfirmationInitiatedBy() != null) {
//				iciciWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId()));
//				iciciWorld.setRequestActive(false);
//			}
//			else {
//				iciciWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.AWAITING.getEventId()));
//				iciciWorld.setRequestActive(true);
//			}
			iciciWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId()));
			iciciWorld.setRequestActive(true);

		}
		else if (inResponse != null) { //Will never be used
			//Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
			daddyRequest.setOrderStatus("CONFIRMED");
			daddyRepository.save(daddyRequest);
			inResponse.setRequestActive(false);
			iciciWorld.setConfirmationInitiatedBy(
					institutionalEntityRepository.findById(daddyDto.getConfirmationInitiatedBy().getId()).get()
					);
			iciciWorldRepository.save(inResponse);
		}

		IciciWorld savedQuote = iciciWorldRepository.save(iciciWorld);
		return modelMapper.map(savedQuote, IciciWorldDto.class);
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
		
		
		
		IciciWorld awaitingRequest = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
										daddyDto.getTransactionId()
										,institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE) 
										, awaiting
										, true
										);
		IciciWorld inRequest = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inRequestEvent, true);
		IciciWorld inNegotiation = iciciWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);
		
		IciciWorld iciciWorld = IciciWorld.builder().transactionId(daddyDto.getTransactionId())
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
				.tenorUnit(tenorRepository.getById(1L))
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId()))
				.createdAt(Instant.now())
				.createdBy(userRepository.getById( Long.valueOf(daddyDto.getCreatedBy().getId())))
				.build(); // OUT_RESPONSE
		
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
			//}
		
		if (inRequest != null) {
			inRequest.setRequestActive(false);
			iciciWorldRepository.save(inRequest);
			iciciWorld.setNegotiationInitiatedBy(
					institutionalEntityRepository.findById(daddyDto.getNegotiationInitiatedBy().getId()).get()
					);		}
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			iciciWorldRepository.save(awaitingRequest);
			iciciWorld.setNegotiationInitiatedBy(
					institutionalEntityRepository.findById(daddyDto.getNegotiationInitiatedBy().getId()).get()
					);
		}
		else {
			inNegotiation.setRequestActive(false);
			iciciWorldRepository.save(inNegotiation);
			iciciWorld.setNegotiationInitiatedBy(
					institutionalEntityRepository.findById(daddyDto.getNegotiationInitiatedBy().getId()).get()
					);	
		}

		
		IciciWorld savedQuote = iciciWorldRepository.save(iciciWorld);
		return modelMapper.map(savedQuote, IciciWorldDto.class);
	}

	@Override
	public SuperInstitutionalEntityDto insertInResponse(DaddyDto daddyDto) {

		// deavtivate the request for this response
		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());
		
		// deavtivate the negotiation for this response
		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());
		
		// deavtivate the Out Response for this response
		EventType outResponseEvent = eventTypeRepository
				.findById(ApplicationConstants.Event.OUT_RESPONSE.getEventId()).get();
		
		// deavtivate the awaiting for this response
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());
		
		
		
		IciciWorld awaitingRequest = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
										daddyDto.getTransactionId()
										,institutionalEntityRepository.getById(daddyDto.getSource().getId())
										, awaiting
										, true
										);
		
		
		IciciWorld outRequest = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(daddyDto.getSource().getId()),
				outRequestEvent, true);
		
		IciciWorld outNegotiation = iciciWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), outNegotiationEvent, true);
		
		IciciWorld outResponse = iciciWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), outResponseEvent, true);
		

		IciciWorld iciciWorld = IciciWorld.builder().transactionId(daddyDto.getTransactionId())
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
				.createdAt(Instant.now())
				.confirmationInitiatedBy(institutionalEntityRepository.findById(daddyDto.getConfirmationInitiatedBy().getId()).get())
				.negotiationInitiatedBy(institutionalEntityRepository.findById(daddyDto.getNegotiationInitiatedBy().getId()).get())
				.createdBy(userRepository.getById( Long.valueOf(daddyDto.getCreatedBy().getId())))
				.build();
		
		Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
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
		
		if (outRequest != null) {
			outRequest.setRequestActive(false);
			iciciWorldRepository.save(outRequest);
			iciciWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId())); // IN_RESPONSE
			iciciWorld.setRequestActive(true);

		} else if (outNegotiation != null) {
			outNegotiation.setRequestActive(false);
			iciciWorldRepository.save(outNegotiation);
			iciciWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId())); // IN_RESPONSE
			iciciWorld.setRequestActive(true);

		}else if (outResponse != null) {
			outResponse.setRequestActive(false);
			iciciWorldRepository.save(outResponse);
			iciciWorld.setRequestActive(true);

			
		}else if(awaitingRequest !=null) {
			awaitingRequest.setRequestActive(false);
			iciciWorldRepository.save(awaitingRequest);
			if(daddyDto.getConfirmationInitiatedBy().getId() == awaitingRequest.getConfirmationInitiatedBy().getId()) {
				iciciWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId())); // IN_RESPONSE
				iciciWorld.setRequestActive(false);

			}
			else {
				iciciWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId())); // IN_RESPONSE
				iciciWorld.setRequestActive(true);

			}

		}
		

		IciciWorld response = iciciWorldRepository.save(iciciWorld);
		IciciWorldDto reponseDto = modelMapper.map(response, IciciWorldDto.class);

		return reponseDto;
	}

	@Override
	public SuperInstitutionalEntityDto insertInNegotiation(DaddyDto daddyDto) {
		// deavtivate the request for this response
		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());
		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());
		
		
		IciciWorld awaitingRequest = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
										daddyDto.getTransactionId()
										,institutionalEntityRepository.getById(daddyDto.getSource().getId())
										, awaiting
										, true
										);
		
		IciciWorld outRequest = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(daddyDto.getSource().getId()),
				outRequestEvent, true);
		IciciWorld outNegotiation = iciciWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), outNegotiationEvent, true);
		if (outRequest != null) {
			outRequest.setRequestActive(false);
			iciciWorldRepository.save(outRequest);
		}else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			iciciWorldRepository.save(awaitingRequest);
		}
		else {
			outNegotiation.setRequestActive(false);
			iciciWorldRepository.save(outNegotiation);
		}

		IciciWorld kotakWorld = IciciWorld.builder().transactionId(daddyDto.getTransactionId())
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
				.confirmationInitiatedBy(null)
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
		//	}
		kotakWorld.setRequestActive(true);
		kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId())); // IN_NEGOTIATION
		IciciWorld response = iciciWorldRepository.save(kotakWorld);
		IciciWorldDto reponseDto = modelMapper.map(response, IciciWorldDto.class);

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
		
		
		IciciWorld awaitingRequest = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
										daddyDto.getTransactionId()
										,institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE) 
										, awaiting
										, true
										);
		
		IciciWorld inRequest = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inRequestEvent, true);
		
		IciciWorld inNegotiation = iciciWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);
		
		if (inRequest != null) {
			inRequest.setRequestActive(false);
			iciciWorldRepository.save(inRequest);
		}
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			iciciWorldRepository.save(awaitingRequest);
		}
		else {
			inNegotiation.setRequestActive(false);
			iciciWorldRepository.save(inNegotiation);
		}

		IciciWorld iciciWorld = IciciWorld.builder().transactionId(daddyDto.getTransactionId())
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
		
		iciciWorld.setBrokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip());
//		Daddy daddyRequest = daddyRepository.findByTransactionId(daddyDto.getTransactionId());
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
			//}
		
		IciciWorld savedQuote = iciciWorldRepository.save(iciciWorld);
		IciciWorldDto result = modelMapper.map(savedQuote, IciciWorldDto.class);
		return result;
	}

	@Override
	public SuperInstitutionalEntityDto insertInReject(DaddyDto daddyDto) {
		
		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());
		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());
		
		
		IciciWorld awaitingRequest = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
										daddyDto.getTransactionId()
										,institutionalEntityRepository.getById(daddyDto.getSource().getId())
										, awaiting
										, true
										);
		
		IciciWorld outRequest = iciciWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(daddyDto.getSource().getId()),
				outRequestEvent, true);
		IciciWorld outNegotiation = iciciWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), outNegotiationEvent, true);
		if (outRequest != null) {
			outRequest.setRequestActive(false);
			iciciWorldRepository.save(outRequest);
		} 
		else if(awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			iciciWorldRepository.save(awaitingRequest);
		}
		else {
			outNegotiation.setRequestActive(false);
			iciciWorldRepository.save(outNegotiation);
		}

		IciciWorld kotakWorld = IciciWorld.builder().transactionId(daddyDto.getTransactionId())
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
		
		
		kotakWorld.setRequestActive(false);
		kotakWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_REJECT.getEventId())); // IN_RESPONSE
		IciciWorld response = iciciWorldRepository.save(kotakWorld);
		IciciWorldDto reponseDto = modelMapper.map(response, IciciWorldDto.class);

		return reponseDto;
	}

	@Override
	public SuperInstitutionalEntityDto insertInReject(SuperInstitutionalEntityDto entityDto) {
		// TODO Auto-generated method stub
		return null;
	}

}
