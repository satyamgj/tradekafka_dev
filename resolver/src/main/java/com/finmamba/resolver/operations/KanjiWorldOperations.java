package com.finmamba.resolver.operations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.Daddy;
import com.finmamba.entities.transaction.KanjiWorld;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.InstitutionalEntityDto;
import com.finmamba.models.dto.KanjiWorldDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.resolver.exception.EntityNotFound;
import com.finmamba.resolver.repositories.CurrencyRepository;
import com.finmamba.resolver.repositories.CustomerRepository;
import com.finmamba.resolver.repositories.DaddyRepository;
import com.finmamba.resolver.repositories.DirectionRepository;
import com.finmamba.resolver.repositories.EventTypeRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.repositories.KanjiWorldRepository;
import com.finmamba.resolver.repositories.ProductRepository;
import com.finmamba.resolver.repositories.TenorRepository;
import com.finmamba.resolver.repositories.UserRepository;
import com.finmamba.resolver.services.EventPublisher;
import com.finmamba.resolver.services.TransactionServiceDal;
import com.finmamba.resolver.util.ApplicationConstants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import lombok.Builder;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@Builder
public class KanjiWorldOperations implements InstitutionalEntityOperations {

	@Autowired
	KanjiWorldRepository kanjiWorldRepository;

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

	@Autowired
	Gson gson;

	private static final long INSTITUTIONAL_ENTITY_CODE = 3L;

	@Override
	public SuperInstitutionalEntityDto insertDaddyRequest(DaddyDto daddyDto) {
		// unimplemented for broker realm
		return null;
	}

	@Override
	public KanjiWorld getInstitutionalEntityInstance() {

		return KanjiWorld.builder().source(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE)).build();
	}

	@Override
	public InstitutionalEntityOperations getInstitutionalEntityOperationsInstance() {
		return this;
	}

	@Override
	public Boolean dispatchRequestToBrokers(SuperInstitutionalEntityDto entityDto) {
		// Unimplemented for broker realm
		return Boolean.FALSE;
	}

	@Override
	public Boolean dispatchRequestToCounterparties(DaddyDto daddyDto, String callerRealmName) {

		// Gets Source
		long sourceId = daddyDto.getSource().getId();

		EventType inRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId());

		// Checks whether there is any IN_REQUEST
		// Parameters: Transaction ID, DestinationId(self),eventType,True

		KanjiWorld inRequest = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inRequestEvent, true);

		inRequest.setRequestActive(false); // Set To False
		kanjiWorldRepository.save(inRequest);

		InstitutionalEntity self = institutionalEntityRepository.findById(INSTITUTIONAL_ENTITY_CODE).get();
		InstitutionalEntityDto selfDto = modelMapper.map(self, InstitutionalEntityDto.class);

		/*
		 * Find all eligible counterparties and Dispatch the request to all of them
		 */
		try {
			// TODO: Rename mapping finder logic method name
			List<InstitutionalEntity> listEligibleCounterparties = dal.findEligibleBrokers(INSTITUTIONAL_ENTITY_CODE);

			listEligibleCounterparties.forEach(counterparty -> {
				// To avoid sending back request to the request initiator
				if (counterparty.getRealmName().equalsIgnoreCase(daddyDto.getSource().getRealmName()))
					return;

				// Build the template for out request
				KanjiWorld kanjiWorld = KanjiWorld.builder().transactionId(daddyDto.getTransactionId()).source(self) // //SELF
						.product(productRepository.getById(daddyDto.getProduct().getId()))
						.customer(customerRepository.getById(daddyDto.getCustomer().getId()))
						.buySellDirection(directionRepository.getById(daddyDto.getBuySellDirection().getId()))
						.baseCurrency(currencyRepository.getById(daddyDto.getBaseCurrency().getId()))
						.quoteCurrency(currencyRepository.getById(daddyDto.getQuoteCurrency().getId()))
						.notional(daddyDto.getNotional()).buyAmount(daddyDto.getBuyAmount())
						.sellAmount(daddyDto.getSellAmount()).sourceSpotRate(daddyDto.getSourceSpotRate())
						.sourceSpotSpread(daddyDto.getSourceSpotSpread())
						.destinationSpotRate(daddyDto.getDestinationSpotRate())
						.sourceSwapPoints(daddyDto.getSourceSwapPoints())
						.destinationSwapRate(daddyDto.getDestinationSwapRate())
						.sourceOutright(daddyDto.getSourceOutright()).spreadOutright(daddyDto.getSpreadOutright())
						.destinationOutright(daddyDto.getDestinationOutright()).tenorUnit(tenorRepository.getById(1L))
						.sellSideSpread(daddyDto.getSellSideSpread())
						.buySideSpread(daddyDto.getBuySideSpread())
						.calculatedBuyCommission(daddyDto.getCalculatedBuyCommission())
						.calculatedSellCommission(daddyDto.getCalculatedSellCommission())
						.brokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip())
						.buySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()))
						.sellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()))
						.buySideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageBaseCurrency()))
						.sellSideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageBaseCurrency()))

						.buySideBaseAmount(daddyDto.getBuySideBaseAmount())
						.sellSideBaseAmount(daddyDto.getSellSideBaseAmount())
						.brokerCommission(daddyDto.getBrokerCommission()) 
						
						.isRequestActive(true)

						// .destinationId(institutionalEntityRepository.getById(2L))
						// .isRequestActive(daddyDto.isRequestActive())
						.eventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId()))
						.createdBy(userRepository.getById(Long.valueOf(daddyDto.getCreatedBy().getId()))).build(); // OUT_REQUEST

				// Insert OUT_REQUEST
				kanjiWorld.setDestinationId(counterparty);
				kanjiWorldRepository.save(kanjiWorld);

				// Insert IN_REQUEST for counterparty
				InstitutionalEntityOperations destinationOperationsHandler = entityResolver
						.getInstitutionalEntityOperationsHandle(counterparty.getRealmName());

				DaddyDto daddyDtoClone = null;
				try {
					daddyDtoClone = gson.fromJson(gson.toJson(daddyDto), DaddyDto.class);
				} catch (JsonSyntaxException jse) {
					log.error("json syntax exceptio: " + jse.getLocalizedMessage());
					return;
				}

				daddyDtoClone.setSource(selfDto);
				SuperInstitutionalEntityDto savedRequestDto = destinationOperationsHandler
						.insertInRequest(daddyDtoClone);

				// Publish on kafka topic
				String kafkaTopic = counterparty.getRealmName() + "." + counterparty.getClientName() + "."
						+ counterparty.getClientRoleName() + "." + counterparty.getGroupName();

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
	public SuperInstitutionalEntityDto insertInRequest(SuperInstitutionalEntityDto entityDto) {
		log.info("insertInRequest started for KanjiWorld with transactionId: " + entityDto.getTransactionId());

		// Publish new broker request event to all eligible brokers
		KanjiWorld kanjiWorld = KanjiWorld.builder().transactionId(entityDto.getTransactionId())
				.source(institutionalEntityRepository.findById(entityDto.getSource().getId()).get())
				.product(productRepository.getById(entityDto.getProduct().getId()))
				.customer(customerRepository.getById(entityDto.getCustomer().getId()))
				.baseCurrency(currencyRepository.getById(entityDto.getBaseCurrency().getId()))
				.quoteCurrency(currencyRepository.getById(entityDto.getQuoteCurrency().getId()))
				.buySellDirection(directionRepository.getById(entityDto.getBuySellDirection().getId()))
				.notional(entityDto.getNotional()).buyAmount(entityDto.getBuyAmount())
				.sellAmount(entityDto.getSellAmount()).sourceSpotSpread(entityDto.getSourceSpotSpread())
				.destinationSpotRate(entityDto.getDestinationSpotRate())
				.sourceSwapPoints(entityDto.getSourceSwapPoints())
				.destinationSwapRate(entityDto.getDestinationSwapRate()).sourceOutright(entityDto.getSourceOutright())
				.spreadOutright(entityDto.getSpreadOutright()).destinationOutright(entityDto.getDestinationOutright())
				.isRequestActive(true).destinationId(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE))
				.tenorUnit(tenorRepository.getById(entityDto.getTenorUnit().getId()))
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId()))
				.createdBy(userRepository.getById(Long.valueOf(entityDto.getCreatedBy().getId()))).build(); // IN_REQUEST
		KanjiWorld savedQuote = kanjiWorldRepository.save(kanjiWorld);

		KanjiWorldDto kanjiWorldDto = modelMapper.map(kanjiWorld, KanjiWorldDto.class);

		return kanjiWorldDto;
	}

	@Override
	public SuperInstitutionalEntityDto insertInRequest(DaddyDto daddyDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SuperInstitutionalEntityDto insertOutResponse(DaddyDto daddyDto, String sendToRealmName) {

		// Fetches Daddy Request
		Daddy daddy = dal.findDaddyRequestByTransactionId(daddyDto.getTransactionId());

		if (daddy.getOrderStatus() == "CONFIRMED") {
			return null;
		}

		// Fetches Destination Realm
		InstitutionalEntity destinationRealm = institutionalEntityRepository.findByRealmName(sendToRealmName);

		// Fetches all possible eventTypes of existing orders
		EventType inResponseEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId());
		EventType inNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId());
		EventType awaitedRequest = eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId());

		// Fetches awaited order
		KanjiWorld awaitedOrder = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), awaitedRequest, true);

		// Fetches InResponse order
		KanjiWorld inResponse = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inResponseEvent, true);

		// Fetches Negotiation order
		KanjiWorld outNegotiation = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);

		KanjiWorld kanjiWorld = KanjiWorld.builder().transactionId(daddyDto.getTransactionId())
				.source(institutionalEntityRepository.findById(INSTITUTIONAL_ENTITY_CODE).get()) // SELF
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
				.isRequestActive(true)
				.confirmationInitiatedBy(
						institutionalEntityRepository.findById(daddyDto.getConfirmationInitiatedBy().getId()).get())
				.negotiationInitiatedBy(
						institutionalEntityRepository.findById(daddyDto.getNegotiationInitiatedBy().getId()).get())
				.createdBy(userRepository.getById(Long.valueOf(daddyDto.getCreatedBy().getId())))
				.buySideSpread(daddyDto.getBuySideSpread()).sellSideSpread(daddyDto.getSellSideSpread())
				.calculatedBuyCommission(daddyDto.getCalculatedBuyCommission()).calculatedSellCommission(daddyDto.getCalculatedSellCommission())
				.brokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip())
				.buySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()))
				.sellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()))
				.buySideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageBaseCurrency()))
				.sellSideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageBaseCurrency()))
				.buySideBaseAmount(daddyDto.getBuySideBaseAmount())
				.sellSideBaseAmount(daddyDto.getSellSideBaseAmount())
				.brokerCommission(daddyDto.getBrokerCommission()) //commission by conunterparty
				.build();

		
		if (inResponse != null) {
			inResponse.setRequestActive(false);
			kanjiWorldRepository.save(inResponse);
			kanjiWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId()));
		}

		else if (awaitedOrder != null) {
			awaitedOrder.setRequestActive(false);
			kanjiWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId()));
			kanjiWorldRepository.save(awaitedOrder);

		} else {
			outNegotiation.setRequestActive(false);
			kanjiWorldRepository.save(outNegotiation);
			kanjiWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId()));

		}

		String kafkaTopic = "";

		kanjiWorld.setDestinationId(destinationRealm);
		kanjiWorld.setNegotiationInitiatedBy(destinationRealm);

		kafkaTopic = destinationRealm.getRealmName() + "." + destinationRealm.getClientName() + "."
				+ destinationRealm.getClientRoleName() + "." + destinationRealm.getGroupName();
		kanjiWorldRepository.save(kanjiWorld);

		InstitutionalEntityOperations entityOperations = entityResolver
				.getInstitutionalEntityOperationsHandle(destinationRealm.getRealmName());

		daddyDto.setSource(modelMapper.map(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				InstitutionalEntityDto.class));

		// Removed this here
		// Will be added after double confirmation
		log.info("Pushing on kafka topic: " + kafkaTopic);

		SuperInstitutionalEntityDto saved = entityOperations.insertInResponse(daddyDto);

		List<SuperInstitutionalEntityDto> listSavedQuote = new ArrayList<>();
		listSavedQuote.add(saved);

		kafkaProducerService.send(listSavedQuote, kafkaTopic, null, null);

		return saved;
	}

	@Override
	public SuperInstitutionalEntityDto insertOutNegotiation(DaddyDto daddyDto, String sendToRealmName) {

		// Fetches Daddy Request
		Daddy daddy = dal.findDaddyRequestByTransactionId(daddyDto.getTransactionId());

		if (daddy.getOrderStatus() == "CONFIRMED") {
			return null;
		}

		InstitutionalEntity destinationRealm = institutionalEntityRepository.findByRealmName(sendToRealmName);

		EventType inResponseEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId());
		EventType inNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId());
		EventType awaitedRequest = eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId());

		KanjiWorld awaitedOrder = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), awaitedRequest, true);
		KanjiWorld inResponse = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inResponseEvent, true);
		KanjiWorld inNegotiation = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);

		if (inResponse != null) {
			inResponse.setRequestActive(false);
			kanjiWorldRepository.save(inResponse);

		} else if (awaitedOrder != null) {
			awaitedOrder.setRequestActive(false);
			kanjiWorldRepository.save(awaitedOrder);
		} else {
			inNegotiation.setRequestActive(false);
			kanjiWorldRepository.save(inNegotiation);
		}

		KanjiWorld kanjiWorld = KanjiWorld.builder().transactionId(daddyDto.getTransactionId())
				.source(institutionalEntityRepository.findById(INSTITUTIONAL_ENTITY_CODE).get()) // SELF
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
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId()))
				.isRequestActive(true).confirmationInitiatedBy(null)
				.createdBy(userRepository.getById(Long.valueOf(daddyDto.getCreatedBy().getId())))
				.calculatedBuyCommission(daddyDto.getCalculatedBuyCommission()).calculatedSellCommission(daddyDto.getCalculatedSellCommission())
				//.calculatedBuyCommission(daddyDto.getCalculatedBuyCommission()).calculatedSellCommission(daddyDto.getCalculatedSellCommission())
				.buySideSpread(daddyDto.getBuySideSpread()).sellSideSpread(daddyDto.getSellSideSpread())
				.brokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip())
				.buySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()))
				.sellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()))
				.buySideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageBaseCurrency()))
				.sellSideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageBaseCurrency()))		
				.buySideConversionRate(daddyDto.getBuySideConversionRate())
				.sellSideConversionRate(daddyDto.getSellSideConversionRate())
				.buySideBaseAmount(daddyDto.getBuySideBaseAmount())
				.sellSideBaseAmount(daddyDto.getSellSideBaseAmount())
				.brokerCommission(daddyDto.getBrokerCommission()) //commission by conunterparty
				.build();// .destinationId(1L)
																													// //OUT_RESPONSE

		String realmName = "";
		String kafkaTopic = "";

		kanjiWorld.setDestinationId(destinationRealm);
		kafkaTopic = destinationRealm.getRealmName() + "." + destinationRealm.getClientName() + "."
				+ destinationRealm.getClientRoleName() + "." + destinationRealm.getGroupName();

		InstitutionalEntityOperations entityOperations = entityResolver
				.getInstitutionalEntityOperationsHandle(destinationRealm.getRealmName());
		kanjiWorldRepository.save(kanjiWorld);

		daddyDto.setSource(modelMapper.map(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				InstitutionalEntityDto.class));

		log.info("Pushing on kafka topic: " + kafkaTopic);

		SuperInstitutionalEntityDto saved = entityOperations.insertInNegotiation(daddyDto);

		List<SuperInstitutionalEntityDto> listSavedQuote = new ArrayList<>();
		listSavedQuote.add(saved);

		kafkaProducerService.send(listSavedQuote, kafkaTopic, null, null);

		return saved;

	}

	@Override
	public SuperInstitutionalEntityDto insertInResponse(DaddyDto daddyDto) {

		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());
		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());
		EventType outResponseEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_RESPONSE.getEventId());
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());

		KanjiWorld awaitingRequest = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()) // from where request is
																							// originated
						, awaiting, true);

		KanjiWorld outRequest = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(daddyDto.getSource().getId()),
				outRequestEvent, true);

		KanjiWorld outNegotiation = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), outNegotiationEvent, true);

		KanjiWorld outResponse = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(daddyDto.getSource().getId()),
				outResponseEvent, true);

		String counterpartyRealmName = "";

		// Insert record for request initiator
		KanjiWorld kanjiWorld = KanjiWorld.builder().transactionId(daddyDto.getTransactionId())
				.source(institutionalEntityRepository.findById(daddyDto.getSource().getId()).get()) // counterparty
				.baseCurrency(currencyRepository.getById(daddyDto.getBaseCurrency().getId()))
				.quoteCurrency(currencyRepository.getById(daddyDto.getQuoteCurrency().getId()))
				.customer(customerRepository.getById(daddyDto.getCustomer().getId()))
				.product(productRepository.getById(daddyDto.getProduct().getId()))
				.buySellDirection(directionRepository.getById(daddyDto.getBuySellDirection().getId()))
				.notional(daddyDto.getNotional()).buyAmount(daddyDto.getBuyAmount())
				.sellAmount(daddyDto.getSellAmount()).sourceSpotRate(daddyDto.getSourceSpotRate())
				.sourceSpotSpread(daddyDto.getSourceSpotSpread()).destinationSpotRate(daddyDto.getDestinationSpotRate())
				.sourceSwapPoints(daddyDto.getSourceSwapPoints()).destinationSwapRate(daddyDto.getDestinationSwapRate())
				.sourceOutright(daddyDto.getSourceOutright()).spreadOutright(daddyDto.getSpreadOutright())
				.destinationOutright(daddyDto.getDestinationOutright())
				.destinationId(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE))
				.confirmationInitiatedBy(
						institutionalEntityRepository.getById(daddyDto.getConfirmationInitiatedBy().getId()))
				.negotiationInitiatedBy(
						institutionalEntityRepository.getById(daddyDto.getNegotiationInitiatedBy().getId()))
				.tenorUnit(tenorRepository.getById(1L)).isRequestActive(true).createdAt(Instant.now())
				.createdBy(userRepository.getById(Long.valueOf(daddyDto.getCreatedBy().getId())))
				.brokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip())
				.buySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()))
				.sellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()))
				.buySideBaseAmount(daddyDto.getBuySideBaseAmount())
				.sellSideBaseAmount(daddyDto.getSellSideBaseAmount())
				.brokerCommission(daddyDto.getBrokerCommission()) //commission by conunterparty
				.buySideConversionRate(daddyDto.getBuySideConversionRate())
				.sellSideConversionRate(daddyDto.getSellSideConversionRate())
				.buySideSpread(daddyDto.getBuySideSpread())
				.sellSideSpread(daddyDto.getSellSideSpread())
				.buySideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageBaseCurrency()))
				.sellSideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageBaseCurrency()))
				.build();

		if (outRequest != null) {

			outRequest.setRequestActive(false);
			kanjiWorldRepository.save(outRequest);

			kanjiWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId()));
			KanjiWorld savedInResponse = kanjiWorldRepository.save(kanjiWorld);
			return modelMapper.map(kanjiWorld, KanjiWorldDto.class);

		} else if (outNegotiation != null) {

			outNegotiation.setRequestActive(false);
			kanjiWorldRepository.save(outNegotiation);

			kanjiWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId()));
			KanjiWorld savedInResponse = kanjiWorldRepository.save(kanjiWorld);
			return modelMapper.map(kanjiWorld, KanjiWorldDto.class);

		} else if (awaitingRequest != null) {

			awaitingRequest.setRequestActive(false);
			kanjiWorldRepository.save(awaitingRequest);

			kanjiWorld.setEventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId()));

			if (daddyDto.getConfirmationInitiatedBy() != null) {

				Optional<InstitutionalEntity> confirmInitBy = institutionalEntityRepository
						.findById(daddyDto.getConfirmationInitiatedBy().getId());
				if (confirmInitBy.isPresent()) {
					kanjiWorld.setConfirmationInitiatedBy(confirmInitBy.get());
					counterpartyRealmName = confirmInitBy.get().getRealmName();
					kanjiWorld.setRequestActive(false);
					KanjiWorld savedInResponse = kanjiWorldRepository.save(kanjiWorld);
					return modelMapper.map(kanjiWorld, KanjiWorldDto.class);
				}
			}
		}
		return null;
	}

	@Override
	public SuperInstitutionalEntityDto insertInNegotiation(DaddyDto daddyDto) {

		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());
		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());

		KanjiWorld awaitingRequest = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), awaiting, true);
		KanjiWorld outRequest = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(daddyDto.getSource().getId()),
				outRequestEvent, true);
		KanjiWorld outNegotiation = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(daddyDto.getSource().getId()), outNegotiationEvent, true);

		if (outRequest != null) {
			outRequest.setRequestActive(false);
			kanjiWorldRepository.save(outRequest);
		} else if (awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			kanjiWorldRepository.save(awaitingRequest);
		} else {
			outNegotiation.setRequestActive(false);
			kanjiWorldRepository.save(outNegotiation);
		}

		KanjiWorld kanjiWorld = KanjiWorld.builder().transactionId(daddyDto.getTransactionId())
				.source(institutionalEntityRepository.findById(daddyDto.getSource().getId()).get()) // counterparty
				.negotiationInitiatedBy(
						institutionalEntityRepository.findById(daddyDto.getNegotiationInitiatedBy().getId()).get())
				.baseCurrency(currencyRepository.getById(daddyDto.getBaseCurrency().getId()))
				.quoteCurrency(currencyRepository.getById(daddyDto.getQuoteCurrency().getId()))
				.customer(customerRepository.getById(daddyDto.getCustomer().getId()))
				.product(productRepository.getById(daddyDto.getProduct().getId()))
				.buySellDirection(directionRepository.getById(daddyDto.getBuySellDirection().getId()))
				.notional(daddyDto.getNotional()).buyAmount(daddyDto.getBuyAmount())
				.sellAmount(daddyDto.getSellAmount()).sourceSpotRate(daddyDto.getSourceSpotRate())
				.sourceSpotSpread(daddyDto.getSourceSpotSpread()).destinationSpotRate(daddyDto.getDestinationSpotRate())
				.sourceSwapPoints(daddyDto.getSourceSwapPoints()).destinationSwapRate(daddyDto.getDestinationSwapRate())
				.sourceOutright(daddyDto.getSourceOutright()).spreadOutright(daddyDto.getSpreadOutright())
				.destinationOutright(daddyDto.getDestinationOutright())
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId()))
				.destinationId(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE))
				.tenorUnit(tenorRepository.getById(1L)).isRequestActive(true).createdAt(Instant.now())
				.confirmationInitiatedBy(null)
				.createdBy(userRepository.getById(Long.valueOf(daddyDto.getCreatedBy().getId())))
				.brokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip())
				.buySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()))
				.sellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()))
				.buySideBaseAmount(daddyDto.getBuySideBaseAmount())
				.sellSideBaseAmount(daddyDto.getSellSideBaseAmount())
				.brokerCommission(daddyDto.getBrokerCommission()) //commission by conunterparty
				.build(); // IN_RESPONSE
		KanjiWorld savedInResponse = kanjiWorldRepository.save(kanjiWorld);
		return modelMapper.map(savedInResponse, KanjiWorldDto.class);
	}

	@Override
	public SuperInstitutionalEntityDto insertInNegotiation(SuperInstitutionalEntityDto entityDto) {
		// deavtivate the request for this response

		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());

		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());

		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());

		KanjiWorld awaitingRequest = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(entityDto.getTransactionId()
				// ,institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE)
						, institutionalEntityRepository.getById(entityDto.getSource().getId()), awaiting, true);
		KanjiWorld outRequest = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				entityDto.getTransactionId(), institutionalEntityRepository.getById(entityDto.getSource().getId()),
				outRequestEvent, true);
		KanjiWorld outNegotiation = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(entityDto.getTransactionId(),
						institutionalEntityRepository.getById(entityDto.getSource().getId()), outNegotiationEvent,
						true);

		if (outRequest != null) {
			outRequest.setRequestActive(false);
			kanjiWorldRepository.save(outRequest);
		} else if (awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			kanjiWorldRepository.save(awaitingRequest);
		} else {
			outNegotiation.setRequestActive(false);
			kanjiWorldRepository.save(outNegotiation);
		}

		// Insert record for request initiator
		KanjiWorld kanjiWorld = KanjiWorld.builder().transactionId(entityDto.getTransactionId())
				.source(institutionalEntityRepository.findById(entityDto.getSource().getId()).get()) // counterparty
				.negotiationInitiatedBy(
						institutionalEntityRepository.findById(entityDto.getNegotiationInitiatedBy().getId()).get())
				.confirmationInitiatedBy(null)
				.baseCurrency(currencyRepository.getById(entityDto.getBaseCurrency().getId()))
				.quoteCurrency(currencyRepository.getById(entityDto.getQuoteCurrency().getId()))
				.customer(customerRepository.getById(entityDto.getCustomer().getId()))
				.product(productRepository.getById(entityDto.getProduct().getId()))
				.buySellDirection(directionRepository.getById(entityDto.getBuySellDirection().getId()))
				.notional(entityDto.getNotional()).buyAmount(entityDto.getBuyAmount())
				.sellAmount(entityDto.getSellAmount()).sourceSpotRate(entityDto.getSourceSpotRate())
				.sourceSpotSpread(entityDto.getSourceSpotSpread())
				.destinationSpotRate(entityDto.getDestinationSpotRate())
				.sourceSwapPoints(entityDto.getSourceSwapPoints())
				.destinationSwapRate(entityDto.getDestinationSwapRate()).sourceOutright(entityDto.getSourceOutright())
				.spreadOutright(entityDto.getSpreadOutright()).destinationOutright(entityDto.getDestinationOutright())
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId()))
				.destinationId(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE))
				.tenorUnit(tenorRepository.getById(1L)).isRequestActive(true).createdAt(Instant.now())
				.createdBy(userRepository.getById(Long.valueOf(entityDto.getCreatedBy().getId())))
				.brokerCommissionIsPip(entityDto.getBrokerCommissionIsPip())
				.brokerCommission(entityDto.getBrokerCommission()) //commission by conunterparty
				.sellSideBrokerageCurrency(currencyRepository.getById(entityDto.getSellSideBrokerageCurrency().getId()))
				.buySideBrokerageCurrency(currencyRepository.getById(entityDto.getSellSideBrokerageCurrency().getId()))
				.buySideBaseAmount(entityDto.getBuySideBaseAmount())
				.sellSideBaseAmount(entityDto.getSellSideBaseAmount())
				.buySideConversionRate(entityDto.getBuySideConversionRate())
				.sellSideConversionRate(entityDto.getSellSideConversionRate())
				.buySideSpread(entityDto.getBuySideSpread()).sellSideSpread(entityDto.getSellSideSpread())
				
				.buySideBrokerageBaseCurrency(currencyRepository.getById(entityDto.getBuySideBrokerageBaseCurrency().getId()))
				.sellSideBrokerageBaseCurrency(currencyRepository.getById(entityDto.getSellSideBrokerageBaseCurrency().getId()))
				.build(); // IN_RESPONSE
		KanjiWorld savedInResponse = kanjiWorldRepository.save(kanjiWorld);
		return modelMapper.map(savedInResponse, KanjiWorldDto.class);
	}

	@Override
	public SuperInstitutionalEntityDto insertOutReject(DaddyDto daddyDto, boolean isBrokerReject,
			String sendToRealmName) {

		// Fetches Daddy Request
		Daddy daddy = dal.findDaddyRequestByTransactionId(daddyDto.getTransactionId());

		if (daddy.getOrderStatus() == "CONFIRMED") {
			return null;
		}

		InstitutionalEntity destinationRealm = institutionalEntityRepository.findByRealmName(sendToRealmName);

		EventType inResponseEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId());
		EventType inNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId());
		EventType inRejectEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_REJECT.getEventId());
		EventType inRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId());

		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId());

		KanjiWorld awaitingRequest = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), awaiting, true);
		KanjiWorld inResponse = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inResponseEvent, true);

		// This is redundant here, Remove after logic test complete.
		KanjiWorld inNegotiation = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);

		KanjiWorld inReject = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inRejectEvent, true);

		KanjiWorld inRequest = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inRequestEvent, true);

		if (inResponse != null) {
			inResponse.setRequestActive(false);
			kanjiWorldRepository.save(inResponse);
		} else if (inNegotiation != null) {
			inNegotiation.setRequestActive(false);
			kanjiWorldRepository.save(inNegotiation);
		} else if (inReject != null) {
			inReject.setRequestActive(false);
			kanjiWorldRepository.save(inReject);
		} else if (inRequest != null) {
			inRequest.setRequestActive(false);
			kanjiWorldRepository.save(inRequest);
		} else if (awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			kanjiWorldRepository.save(awaitingRequest);
		}

		KanjiWorld kanjiWorld = KanjiWorld.builder().transactionId(daddyDto.getTransactionId())
				.source(institutionalEntityRepository.findById(INSTITUTIONAL_ENTITY_CODE).get()) // SELF
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
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_REJECT.getEventId()))
				.isRequestActive(false).createdAt(Instant.now())
				.createdBy(userRepository.getById(Long.valueOf(daddyDto.getCreatedBy().getId())))
				.brokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip())
				.buySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()))
				.sellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()))
				.buySideBaseAmount(daddyDto.getBuySideBaseAmount())
				.sellSideBaseAmount(daddyDto.getSellSideBaseAmount())
				.buySideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageBaseCurrency()))
				.sellSideBrokerageBaseCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageBaseCurrency()))
				.brokerCommission(daddyDto.getBrokerCommission()) //commission by conunterparty
				.build();// .destinationId(1L)
																											// //OUT_RESPONSE

		String realmName = "";
		String kafkaTopic = "";
		kanjiWorld.setDestinationId(destinationRealm);
		kafkaTopic = destinationRealm.getRealmName() + "." + destinationRealm.getClientName() + "."
				+ destinationRealm.getClientRoleName() + "." + destinationRealm.getGroupName();

		kanjiWorldRepository.save(kanjiWorld);
		InstitutionalEntityOperations entityOperations = entityResolver
				.getInstitutionalEntityOperationsHandle(destinationRealm.getRealmName());

		daddyDto.setSource(modelMapper.map(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				InstitutionalEntityDto.class));
		log.info("Pushing on kafka topic: " + kafkaTopic);

		SuperInstitutionalEntityDto saved = entityOperations.insertInReject(daddyDto);

		List<SuperInstitutionalEntityDto> listSavedQuote = new ArrayList<>();
		listSavedQuote.add(saved);

		kafkaProducerService.send(listSavedQuote, kafkaTopic, null, null);

		return saved;
	}

	@Override
	public SuperInstitutionalEntityDto insertInReject(DaddyDto daddyDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SuperInstitutionalEntityDto insertInReject(SuperInstitutionalEntityDto entityDto) {
		// deavtivate the request for this response
		EventType outRequestEvent = eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId());
		EventType outNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId());
		EventType awaiting = eventTypeRepository.getById(ApplicationConstants.Event.OUT_AWAITING.getEventId());

		KanjiWorld awaitingRequest = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(entityDto.getTransactionId(),
						institutionalEntityRepository.getById(entityDto.getSource().getId()), awaiting, true);

		KanjiWorld outRequest = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				entityDto.getTransactionId(), institutionalEntityRepository.getById(entityDto.getSource().getId()),
				outRequestEvent, true);
		KanjiWorld outNegotiation = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(entityDto.getTransactionId(),
						institutionalEntityRepository.getById(entityDto.getSource().getId()), outNegotiationEvent,
						true);
		// Insert record for request initiator
		KanjiWorld kanjiWorld = KanjiWorld.builder().transactionId(entityDto.getTransactionId())
				.source(institutionalEntityRepository.findById(entityDto.getSource().getId()).get()) // counterparty
				.baseCurrency(currencyRepository.getById(entityDto.getBaseCurrency().getId()))
				.quoteCurrency(currencyRepository.getById(entityDto.getQuoteCurrency().getId()))
				.customer(customerRepository.getById(entityDto.getCustomer().getId()))
				.product(productRepository.getById(entityDto.getProduct().getId()))
				.buySellDirection(directionRepository.getById(entityDto.getBuySellDirection().getId()))
				.notional(entityDto.getNotional()).buyAmount(entityDto.getBuyAmount())
				.sellAmount(entityDto.getSellAmount()).sourceSpotRate(entityDto.getSourceSpotRate())
				.sourceSpotSpread(entityDto.getSourceSpotSpread())
				.destinationSpotRate(entityDto.getDestinationSpotRate())
				.sourceSwapPoints(entityDto.getSourceSwapPoints())
				.destinationSwapRate(entityDto.getDestinationSwapRate()).sourceOutright(entityDto.getSourceOutright())
				.spreadOutright(entityDto.getSpreadOutright()).destinationOutright(entityDto.getDestinationOutright())
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_REJECT.getEventId()))
				.destinationId(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE))
				.tenorUnit(tenorRepository.getById(1L)).isRequestActive(false).createdAt(Instant.now())
				.createdBy(userRepository.getById(Long.valueOf(entityDto.getCreatedBy().getId())))
				.brokerCommissionIsPip(entityDto.getBrokerCommissionIsPip())
				.buySideBrokerageCurrency(currencyRepository.getById(entityDto.getBuySideBrokerageCurrency().getId()))
				.sellSideBrokerageCurrency(currencyRepository.getById(entityDto.getSellSideBrokerageCurrency().getId()))
				.buySideBaseAmount(entityDto.getBuySideBaseAmount())
				.sellSideBaseAmount(entityDto.getSellSideBaseAmount())
				.brokerCommission(entityDto.getBrokerCommission()) //commission by conunterparty
				.buySideConversionRate(entityDto.getBuySideConversionRate())
				.sellSideConversionRate(entityDto.getSellSideConversionRate())
				.buySideSpread(entityDto.getBuySideSpread())
				.sellSideSpread(entityDto.getSellSideSpread())
				.buySideBrokerageBaseCurrency(currencyRepository.getById(entityDto.getBuySideBrokerageBaseCurrency().getId()))
				.sellSideBrokerageBaseCurrency(currencyRepository.getById(entityDto.getSellSideBrokerageBaseCurrency().getId()))
				.build(); // IN_RESPONSE

		if (outRequest != null) {
			outRequest.setRequestActive(false);
			kanjiWorldRepository.save(outRequest);
		} else if (awaitingRequest != null) {
			awaitingRequest.setRequestActive(false);
			kanjiWorldRepository.save(awaitingRequest);
		} else {
			outNegotiation.setRequestActive(false);
			kanjiWorldRepository.save(outNegotiation);

			if (entityDto.getNegotiationInitiatedBy() != null) {
				Optional<InstitutionalEntity> negoInitBy = institutionalEntityRepository
						.findById(entityDto.getNegotiationInitiatedBy().getId());
				if (negoInitBy.isPresent())
					kanjiWorld.setNegotiationInitiatedBy(negoInitBy.get());
			}
		}

		KanjiWorld savedInResponse = kanjiWorldRepository.save(kanjiWorld);
		return modelMapper.map(savedInResponse, KanjiWorldDto.class);

	}

	public SuperInstitutionalEntityDto insertOutReNegotiation(DaddyDto daddyDto) {

		EventType inResponseEvent = eventTypeRepository.getById(ApplicationConstants.Event.IN_RESPONSE.getEventId());
		EventType inNegotiationEvent = eventTypeRepository
				.getById(ApplicationConstants.Event.IN_NEGOTIATION.getEventId());

		EventType awaitedRequest = eventTypeRepository.getById(ApplicationConstants.Event.IN_AWAITING.getEventId());

		KanjiWorld awaitedOrder = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), awaitedRequest, true);
		// This is redundant here, Remove after logic test complete.
		KanjiWorld inResponse = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
				daddyDto.getTransactionId(), institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				inResponseEvent, true);

		KanjiWorld inNegotiation = kanjiWorldRepository
				.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(daddyDto.getTransactionId(),
						institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE), inNegotiationEvent, true);

		if (inResponse != null) {
			inResponse.setRequestActive(false);
			kanjiWorldRepository.save(inResponse);
		} else if (awaitedOrder != null) {
			awaitedOrder.setRequestActive(false);
			kanjiWorldRepository.save(awaitedOrder);
		} else {
			// Find the destination
			inNegotiation.setRequestActive(false);
			kanjiWorldRepository.save(inNegotiation);
		}

		// here goes the logic for story writing from source side

		KanjiWorld kanjiWorld = KanjiWorld.builder().transactionId(daddyDto.getTransactionId())
				.source(institutionalEntityRepository.findById(INSTITUTIONAL_ENTITY_CODE).get()) // SELF
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
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_NEGOTIATION.getEventId()))
				.isRequestActive(true).confirmationInitiatedBy(null)
				.createdBy(userRepository.getById(Long.valueOf(daddyDto.getCreatedBy().getId())))
				.brokerCommissionIsPip(daddyDto.getBrokerCommissionIsPip())
				.buySideBrokerageCurrency(currencyRepository.getById(daddyDto.getBuySideBrokerageCurrency()))
				.sellSideBrokerageCurrency(currencyRepository.getById(daddyDto.getSellSideBrokerageCurrency()))
				.buySideBaseAmount(daddyDto.getBuySideBaseAmount())
				.sellSideBaseAmount(daddyDto.getSellSideBaseAmount())
				.brokerCommission(daddyDto.getBrokerCommission()) //commission by conunterparty
				.build();// .destinationId(1L)
																											// //OUT_RESPONSE

		String realmName = "";
		String kafkaTopic = "";

		InstitutionalEntityOperations entityOperations = entityResolver
				.getInstitutionalEntityOperationsHandle(realmName);
		kanjiWorldRepository.save(kanjiWorld);

		daddyDto.setSource(modelMapper.map(institutionalEntityRepository.getById(INSTITUTIONAL_ENTITY_CODE),
				InstitutionalEntityDto.class));

		log.info("Pushing on kafka topic: " + kafkaTopic);

		SuperInstitutionalEntityDto saved = entityOperations.insertInNegotiation(daddyDto);

		List<SuperInstitutionalEntityDto> listSavedQuote = new ArrayList<>();
		listSavedQuote.add(saved);

		kafkaProducerService.send(listSavedQuote, kafkaTopic, null, null);

		return saved;

	}

}
