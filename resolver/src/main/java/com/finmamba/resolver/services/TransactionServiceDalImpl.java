package com.finmamba.resolver.services;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.staticdata.Product;
import com.finmamba.entities.staticdata.Tenor;
import com.finmamba.entities.transaction.BrokerToBrokerTransactionDetails;
import com.finmamba.entities.transaction.ConfirmedOrderDetails;
import com.finmamba.entities.transaction.CounterpartyTransactionDetails;
import com.finmamba.entities.transaction.Customer;
import com.finmamba.entities.transaction.InstitutionalEntityAssociation;
import com.finmamba.entities.transaction.MatchingTable;
import com.finmamba.entities.transaction.TransactionBrokerDetails;
import com.finmamba.entities.transaction.TransactionDetail;
import com.finmamba.entities.transaction.User;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.bus.EventType;
import com.finmamba.models.bus.HttpResponseModel;
import com.finmamba.models.bus.SerialDataBusModel;
import com.finmamba.models.dto.BrokerOrderDto;
import com.finmamba.models.dto.BrokerToBrokerDto;
import com.finmamba.models.dto.ConfirmedOrderDto;
import com.finmamba.models.dto.CounterPartyTransactionFetchingDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.dto.SellSideConfirmationDto;
import com.finmamba.models.dto.buySideConfirmDto;
import com.finmamba.models.transaction.LatestQuoteDetailRequest;
import com.finmamba.models.transaction.MarketDepthRequest;
import com.finmamba.models.transaction.QuoteRequestModel;
import com.finmamba.resolver.exception.EntityNotFound;
import com.finmamba.resolver.repositories.BrokerToBrokerRepository;
import com.finmamba.resolver.repositories.ConfirmationRepostory;
import com.finmamba.resolver.repositories.CounterPartyTransactionRepository;
import com.finmamba.resolver.repositories.CurrencyPairDetailsRepository;
import com.finmamba.resolver.repositories.CurrencyRepository;
import com.finmamba.resolver.repositories.CustomerRepository;
import com.finmamba.resolver.repositories.CustomerSegmentRepository;
import com.finmamba.resolver.repositories.DirectionRepository;
import com.finmamba.resolver.repositories.EntityDetailsRepository;
import com.finmamba.resolver.repositories.EventTypeRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityAssociationRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.repositories.ProductRepository;
import com.finmamba.resolver.repositories.QuoteRequestRepository;
import com.finmamba.resolver.repositories.RecordStatusRepository;
import com.finmamba.resolver.repositories.RequestTypeRepository;
import com.finmamba.resolver.repositories.TenorRepository;
import com.finmamba.resolver.repositories.TransactionBrokerDetailRepository;
import com.finmamba.resolver.repositories.TransactionRepository;
import com.finmamba.resolver.repositories.UserRepository;
import com.finmamba.resolver.util.UniqueIdentifierGenerator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransactionServiceDalImpl implements TransactionServiceDal {

	@Autowired
	ProductRepository productRepository;

	@Autowired
	DirectionRepository directionRepository;

	@Autowired
	TenorRepository tenorRepository;

	@Autowired
	InstitutionalEntityAssociationRepository institutionalEntityAssociationRepository;

	@Autowired
	InstitutionalEntityRepository institutionalEntityRepository;

	@Autowired
	CurrencyPairDetailsRepository currencyPairDetailsRepository;

	@Autowired
	CurrencyRepository currencyRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RequestTypeRepository requestTypeRepository;

	@Autowired
	EntityDetailsRepository entityDetailsRepository;

	@Autowired
	CustomerSegmentRepository customerSegmentRepository;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	RecordStatusRepository recordStatusRepository;

	@Autowired
	QuoteRequestRepository quoteRequestRepository;

	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	EventPublisher eventPublisher;

	@Autowired
	Gson gson;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	EventTypeRepository eventTypeRepository;

	@Autowired
	CounterPartyTransactionRepository counterPartyTransactionRepository;

	@Autowired
	ConfirmationRepostory confirmationRepostory;

	@Autowired
	OrderMatch match;
	
	@Autowired
	TransactionBrokerDetailRepository transactionBrokerDetailRepository;
	
	@Autowired
	BrokerToBrokerRepository brokerToBrokerRepository;

//	@Autowired
//	HistoryWriter historyWriter;

	private static final Long BANK = 1L;
	private static final Long BROKER = 2L;

	Direction getBuyObject() {
		return directionRepository.getById(1L);
	}

	Direction getSellObject() {
		return directionRepository.getById(2L);
	}

	private org.springframework.data.domain.Sort.Direction getDirection(String direction) {
		if (direction.equalsIgnoreCase("ASC")) {
			return org.springframework.data.domain.Sort.Direction.ASC;
		} else {
			return org.springframework.data.domain.Sort.Direction.DESC;
		}
	}

	private Pageable preparePageRequest(int start, int end, org.springframework.data.domain.Sort.Direction direction,
			String parameter) {
		return PageRequest.of(start, end, Sort.by(direction, parameter));
	}

	public InstitutionalEntity findInstitutionalEntityFromId(long id) throws EntityNotFound {
		return institutionalEntityRepository.findById(id)
				.orElseThrow(() -> new EntityNotFound("Institutional Entity not found"));
	}

	@Override
	public List<QuoteRequestDto> saveRequest(QuoteRequestModel quoteRequestModel) throws EntityNotFound {

		// Product
		Product product = productRepository.findById(quoteRequestModel.getProductId())
				.orElseThrow(() -> new EntityNotFound("product  not found"));

		// Get base and quote currency
		Currency baseCurrency = currencyRepository.findById(quoteRequestModel.getBaseCurrencyId())
				.orElseThrow(() -> new EntityNotFound("baseCurrency  not found"));

		Currency quoteCurrency = currencyRepository.findById(quoteRequestModel.getQuoteCurrencyId())
				.orElseThrow(() -> new EntityNotFound("quoteCurrency  not found"));

		Long customerId = quoteRequestModel.getCustomerId();

		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new EntityNotFound("customer  not found"));

		// Get Buy or Sell
		Direction direction = directionRepository.getById(quoteRequestModel.getBuySellDirectionId());

		BigDecimal bankBuyAmount = quoteRequestModel.getBuyAmount();
		BigDecimal bankSellAmount = quoteRequestModel.getSellAmount();

		BigDecimal bankOutright = quoteRequestModel.getSourceOutright();
		BigDecimal spreadOutright = quoteRequestModel.getSpreadOutright();
		BigDecimal clientOutright = quoteRequestModel.getDestinationOutright();

		// Tenor value
		int tenor = quoteRequestModel.getTenorValue();

		// Tenor unit
		Tenor tenorUnit = tenorRepository.findById(quoteRequestModel.getTenorUnitId())
				.orElseThrow(() -> new EntityNotFound("tenor unit  not found"));

		// All dates
		LocalDateTime localDateTime = LocalDateTime.now();
		Date requestTimeStamp = Date.valueOf(localDateTime.toLocalDate());

		// User

		User userId = userRepository.getById(Long.valueOf(quoteRequestModel.getUserId()));

		/*
		 * Prepare a request template
		 */
		TransactionDetail quoteRequest = TransactionDetail.builder().product(product).buyAmount(bankBuyAmount)
				.sellAmount(bankSellAmount)

				.sourceOutright(bankOutright).spreadOutright(spreadOutright).destinationOutright(clientOutright)
				.tenorUnit(tenorUnit)
				.source(findInstitutionalEntityFromId(quoteRequestModel.getInstitutionalEntityId()))
				// .transactionId(UniqueIdentifierGenerator.unique())
				.baseCurrency(baseCurrency).quoteCurrency(quoteCurrency).customer(customer)
				.transactionId(UniqueIdentifierGenerator.unique()) // Saved by Db halted
				.buySellDirection(direction).createdAt(Instant.now()).createdBy(userId)
				.isPartial(quoteRequestModel.getIsPartial()).isConfirmed(false).build();

		TransactionDetail savedRequest = quoteRequestRepository.save(quoteRequest);

		log.info("Quote request saved with id: " + savedRequest.getId());

		QuoteRequestDto quoteRequestDto = modelMapper.map(savedRequest, QuoteRequestDto.class);

		List<QuoteRequestDto> listQuoteRequest = new ArrayList<>();
		listQuoteRequest.add(quoteRequestDto);

		// Publish new daddy request event
		eventPublisher.publishEvent(quoteRequestDto);

		return listQuoteRequest;
	}

	/******************************************
	 * FIND START
	 **************************************/

	@Override
	@Transactional
	public HttpResponseModel findActiveRequestsForInstitution(String institutionId, int page, int pageSize)
			throws EntityNotFound {

		InstitutionalEntity institutionalEntity = institutionalEntityRepository.findById(Long.valueOf(institutionId))
				.get();

		List<TransactionDetail> activeTransactions = transactionRepository.findAllBySourceAndIsConfirmed(
				institutionalEntity, false,
				preparePageRequest(page, pageSize, org.springframework.data.domain.Sort.Direction.ASC, "id"));

		return HttpResponseModel.builder().data(activeTransactions)
				.total(transactionRepository.countBySourceAndIsConfirmed(institutionalEntity, false)).build();

	}

	@Override
	@Transactional
	public HttpResponseModel findInOutForTransaction(String transactionId, String institutionId) throws EntityNotFound {

		try {
			logTimeUpdate("findInOutForTransaction", "START");

			TransactionDetail transaction = transactionRepository.getById(Long.valueOf(transactionId));

			InstitutionalEntity institution = institutionalEntityRepository.getById(Long.valueOf(institutionId));

			Type listType;

			List<CounterpartyTransactionDetails> listAwaitingOrders = new ArrayList<>();

			com.finmamba.entities.staticdata.EventType inEventType = eventTypeRepository.getById(1L);

			com.finmamba.entities.staticdata.EventType inResponse = eventTypeRepository.getById(3L);

			com.finmamba.entities.staticdata.EventType inNegotiation = eventTypeRepository.getById(5L);

			com.finmamba.entities.staticdata.EventType inReject = eventTypeRepository.getById(7L);

			com.finmamba.entities.staticdata.EventType inAwait = eventTypeRepository.getById(9L);

			com.finmamba.entities.staticdata.EventType outEventType = eventTypeRepository.getById(2L);

			com.finmamba.entities.staticdata.EventType outResponse = eventTypeRepository.getById(4L);

			com.finmamba.entities.staticdata.EventType outNegotiation = eventTypeRepository.getById(6L);

			com.finmamba.entities.staticdata.EventType outReject = eventTypeRepository.getById(8L);

			List<com.finmamba.entities.staticdata.EventType> inEvents = new ArrayList<>();

			inEvents.add(inEventType);

			inEvents.add(inNegotiation);

			List<com.finmamba.entities.staticdata.EventType> outEvents = new ArrayList<>();

			outEvents.add(outEventType);

			outEvents.add(outNegotiation);

			outEvents.add(outReject);

			outEvents.add(outResponse);

			List<com.finmamba.entities.staticdata.EventType> rejects = new ArrayList<>();

			rejects.add(inReject);

			List<com.finmamba.entities.staticdata.EventType> confirmed = new ArrayList<>();

			confirmed.add(inResponse);

			List<CounterpartyTransactionDetails> inTransactions = counterPartyTransactionRepository
					.findAllByDestinationIdAndEventTypeInAndTransactionIdAndIsRequestActive(institution, inEvents,
							transaction, true);

			List<CounterpartyTransactionDetails> outTransactions = counterPartyTransactionRepository
					.findAllBySourceIdAndEventTypeInAndTransactionIdAndIsRequestActive(institution, outEvents,
							transaction, true);

			List<CounterpartyTransactionDetails> rejectedTransactions = counterPartyTransactionRepository
					.findAllByDestinationIdAndEventTypeInAndTransactionIdAndIsRequestActive(institution, rejects,
							transaction,

							true);

			List<CounterpartyTransactionDetails> confirmedTransactions = counterPartyTransactionRepository
					.findAllByDestinationIdAndEventTypeInAndTransactionIdAndIsRequestActive(institution, confirmed,
							transaction,

							true);


//			if (parentTransaction.getSource().getId() == Long.valueOf(institutionId)) {
//				// BuySide
//				
//			} else {
//				// SellSide
//				listType = new TypeToken<ArrayList<SellSideTransactionDto>>() {
//				}.getType();
//			}
			
			listType = new TypeToken<List<CounterPartyTransactionFetchingDto>>() {
			}.getType();

			Map<String, Object> mapInOut = new HashMap<>();

			mapInOut.put("in", modelMapper.map(inTransactions, listType));
			mapInOut.put("out", modelMapper.map(outTransactions, listType));
			mapInOut.put("confirm", modelMapper.map(confirmedTransactions, listType));
			mapInOut.put("reject", modelMapper.map(rejectedTransactions, listType));
			mapInOut.put("awaiting", modelMapper.map(listAwaitingOrders, listType));

			logTimeUpdate("findInOutForTransaction", "END");

			// Publish request to be displayed on broker view
			return HttpResponseModel.builder().data(mapInOut).build();
		} catch (Exception e) {
			Map<String, Object> mapInOut = new HashMap<>();

			mapInOut.put("in", Collections.emptyList());
			mapInOut.put("out", Collections.emptyList());
			mapInOut.put("confirm", Collections.emptyList());
			mapInOut.put("reject", Collections.emptyList());
			mapInOut.put("awaiting", Collections.emptyList());

			logTimeUpdate("findInOutForTransaction", "END");

			return HttpResponseModel.builder().data(mapInOut).message(e.getMessage()).build();

		}

	}

	@Override
	@Transactional
	public HttpResponseModel findConfirmedOrdersForInstitution(String institutionId, int page, int pageSize)
			throws EntityNotFound {

		Map<String, Object> mapConfirmOrders = new HashMap<>();

		try {
			InstitutionalEntity entity = findInstitutionalEntityFromId(Long.valueOf(institutionId));

			List<ConfirmedOrderDetails> buySideConfirmationList = confirmationRepostory.findAllBybuySideId(entity);

			List<ConfirmedOrderDetails> sellSideConfirmationList = confirmationRepostory.findAllBysellSideId(entity);

			Type buySide = new TypeToken<List<buySideConfirmDto>>() {
			}.getType();

			Type sellSide = new TypeToken<List<SellSideConfirmationDto>>() {
			}.getType();

			mapConfirmOrders.put("buySide", modelMapper.map(buySideConfirmationList, buySide));
			mapConfirmOrders.put("sellSide", modelMapper.map(sellSideConfirmationList, sellSide));

			return HttpResponseModel.builder().data(mapConfirmOrders).build();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			mapConfirmOrders.put("buySide", Collections.emptyList());
			mapConfirmOrders.put("sellSide", Collections.emptyList());

			return HttpResponseModel.builder().data(mapConfirmOrders)
					.message("Error occured inside findConfirmedOrdersForInstitution " + e.getMessage()).status(500)
					.build();

		} catch (EntityNotFound e) {
			mapConfirmOrders.put("buySide", Collections.emptyList());
			mapConfirmOrders.put("sellSide", Collections.emptyList());

			return HttpResponseModel.builder().data(mapConfirmOrders)
					.message("Error occured inside findConfirmedOrdersForInstitution " + e.getMessage()).status(500)
					.build();
		}

	}

	@Override
	@Transactional
	public HttpResponseModel findConfirmedOrdersForBroker(String institutionId, int page, int pageSize)
			throws EntityNotFound {

		try {
			InstitutionalEntity entity = findInstitutionalEntityFromId(Long.valueOf(institutionId));

			List<ConfirmedOrderDetails> confirmationList = confirmationRepostory.findAllBybrokerId(entity);

			Type confirmOrders = new TypeToken<List<ConfirmedOrderDto>>() {
			}.getType();

			return HttpResponseModel.builder().data(modelMapper.map(confirmationList, confirmOrders)).build();
		} catch (NumberFormatException e) {

			return HttpResponseModel.builder().data(Collections.emptyList())
					.message("Error occured inside findConfirmedOrdersForInstitution " + e.getMessage()).status(500)
					.build();

		} catch (EntityNotFound e) {
			return HttpResponseModel.builder().data(Collections.emptyList())
					.message("Error occured inside findConfirmedOrdersForInstitution " + e.getMessage()).status(500)
					.build();
		}

	}

	/******************************************
	 * FIND END
	 **************************************/

	private SerialDataBusModel serialDataBusModelBuilder(Object payload, EventType eventType) {
		// @// @formatter:off
		return SerialDataBusModel.builder().payload(payload).eventType(eventType)
				.messageTimestamp(LocalDateTime.now().toString()).build();
		// @formatter:on
	}

	@Override
	public List<InstitutionalEntity> findEligibleCounterparties(long source, Boolean isBroadcastToBrokers)
			throws EntityNotFound {

		List<InstitutionalEntity> eligibleSellers = new ArrayList<>();

		List<InstitutionalEntityAssociation> institutionalEntityAssociations = new ArrayList<>();

		InstitutionalEntity sourceEntity = institutionalEntityRepository.findById(source)
				.orElseThrow(() -> new EntityNotFound("Source not found"));

		if (isBroadcastToBrokers) {
			institutionalEntityAssociations = institutionalEntityAssociationRepository
					.findDestinationEntityByWillingToBuyAndSourceEntityAndDestinationEntityNotAndEntityType(true,
							sourceEntity, sourceEntity, BROKER);
		} else {
			institutionalEntityAssociations = institutionalEntityAssociationRepository
					.findDestinationEntityByWillingToBuyAndSourceEntityAndDestinationEntityNotAndEntityType(true,
							sourceEntity, sourceEntity, BANK);
		}

		log.info("Fetched all associations for " + sourceEntity.getRealmName());

		if (institutionalEntityAssociations.isEmpty()) {
			throw new EntityNotFound("No associations found for source: " + sourceEntity.getShortName());
		}
		// TODO: Need to check failure
		// institutionalEntityAssociations.forEach(entity ->
		// eligibleSellers.add(entity.getDestinationEntity()));
		for (InstitutionalEntityAssociation association : institutionalEntityAssociations) {
			eligibleSellers.add(association.getDestinationEntity());
		}

		return eligibleSellers;

		/**
		 * Need to write logic
		 */
	}

	@Override
	public List<InstitutionalEntity> findEligibleBrokers(long sourceId) throws EntityNotFound {

		List<InstitutionalEntity> eligibleSellers = new ArrayList<>();

		InstitutionalEntity sourceEntity = institutionalEntityRepository.findById(sourceId)
				.orElseThrow(() -> new EntityNotFound("Source not found"));

		// TODO: Add condition of Role=BROKER
		List<InstitutionalEntityAssociation> institutionalEntityAssociations = institutionalEntityAssociationRepository
				.findDestinationEntityByWillingToBuyAndSourceEntity(true, sourceEntity);

		if (institutionalEntityAssociations.isEmpty()) {
			log.info("No associations found for source: " + sourceEntity.getShortName());
			return Collections.emptyList(); // returning empty list as no associations found.
		}
		// TODO: Need to check failure
		for (InstitutionalEntityAssociation association : institutionalEntityAssociations) {
			eligibleSellers.add(association.getDestinationEntity());
		}

		return eligibleSellers;
	}

	public InstitutionalEntity findInstitutionEntity(long institutionalEntityId) throws EntityNotFound {

		return institutionalEntityRepository.findById(institutionalEntityId)
				.orElseThrow(() -> new EntityNotFound("Institutional Entity not found"));
	}

	@Transactional
	@Override
	public HttpResponseModel findInForInstitution(String institutionId, int page, int pageSize) throws EntityNotFound {

		try {

			InstitutionalEntity destination = institutionalEntityRepository.findById(Long.valueOf(institutionId)).get();

			com.finmamba.entities.staticdata.EventType inEventType = eventTypeRepository.getById(1L);

			com.finmamba.entities.staticdata.EventType inResponse = eventTypeRepository.getById(3L);

			com.finmamba.entities.staticdata.EventType inNegotiation = eventTypeRepository.getById(5L);

			com.finmamba.entities.staticdata.EventType inReject = eventTypeRepository.getById(7L);

			com.finmamba.entities.staticdata.EventType inAwait = eventTypeRepository.getById(9L);

			List<com.finmamba.entities.staticdata.EventType> events = new ArrayList();

			events.add(inEventType);

			events.add(inResponse);

			events.add(inNegotiation);

			events.add(inReject);

			events.add(inAwait);

			List<CounterpartyTransactionDetails> transactions = counterPartyTransactionRepository
					.findAllByDestinationIdAndIsRequestActiveAndEventTypeIn(destination,true, events,

							preparePageRequest(page, pageSize, org.springframework.data.domain.Sort.Direction.ASC,
									"id"));

			List<CounterPartyTransactionFetchingDto> allTransactions = new ArrayList();

			for (CounterpartyTransactionDetails transaction : transactions) {

				allTransactions.add(modelMapper.map(transaction, CounterPartyTransactionFetchingDto.class));

			}

			return HttpResponseModel.builder().data(allTransactions).build();

		} catch (Exception e) {
			return HttpResponseModel.builder().data(Collections.emptyList())
					.message("Exception Occured inside method findInForInstitution: " + e.getMessage()).build();
		}

	}

	private void logTimeUpdate(String functionName, String status) {
		log.info(functionName + " " + status + " " + Instant.now());
	}

	@Override
	public HttpResponseModel findDepthOfCcyPairByInstituionId(MarketDepthRequest marketDepthRequest) {

		logTimeUpdate("findDepthOfCcyPairByInstituionId", "START");

		Map<String, Object> orders = new HashMap<>();

		try {

			com.finmamba.entities.staticdata.EventType inEventType = eventTypeRepository.getById(1L);

			com.finmamba.entities.staticdata.EventType inResponse = eventTypeRepository.getById(3L);

			com.finmamba.entities.staticdata.EventType inNegotiation = eventTypeRepository.getById(5L);

			com.finmamba.entities.staticdata.EventType inReject = eventTypeRepository.getById(7L);

			com.finmamba.entities.staticdata.EventType inAwait = eventTypeRepository.getById(9L);

			List<com.finmamba.entities.staticdata.EventType> events = new ArrayList();

			events.add(inEventType);

			events.add(inResponse);

			events.add(inNegotiation);

			events.add(inReject);

			events.add(inAwait);
//

			List<CounterPartyTransactionFetchingDto> buyTransactionList = new ArrayList();

			List<CounterPartyTransactionFetchingDto> sellTransactionList = new ArrayList();

			List<CounterpartyTransactionDetails> buyDepth = counterPartyTransactionRepository
					.findByDestinationIdAndSellCcyAndBuyCcyAndDirectionIdAndIsRequestActiveAndEventTypeIn(
							institutionalEntityRepository.getById(marketDepthRequest.getInstituionId()),
							currencyRepository.getById(marketDepthRequest.getSellCurrencyId()),
							currencyRepository.getById(marketDepthRequest.getBuyCurrencyId()),
							directionRepository.getById(1L), true, events);

			for (CounterpartyTransactionDetails buyOrders : buyDepth) {
				buyTransactionList.add(modelMapper.map(buyOrders, CounterPartyTransactionFetchingDto.class));
			}

			orders.put("BUY", buyTransactionList);

			List<CounterpartyTransactionDetails> sellDepth = counterPartyTransactionRepository
					.findByDestinationIdAndSellCcyAndBuyCcyAndDirectionIdAndIsRequestActiveAndEventTypeIn(
							institutionalEntityRepository.getById(marketDepthRequest.getInstituionId()),
							currencyRepository.getById(marketDepthRequest.getSellCurrencyId()),
							currencyRepository.getById(marketDepthRequest.getBuyCurrencyId()),
							directionRepository.getById(2L), true, events);

			for (CounterpartyTransactionDetails sellOrders : sellDepth) {
				sellTransactionList.add(modelMapper.map(sellOrders, CounterPartyTransactionFetchingDto.class));
			}

			orders.put("SELL", sellTransactionList);

			logTimeUpdate("findDepthOfCcyPairByInstituionId", "END");

			return HttpResponseModel.builder().data(orders).build();

		} catch (Exception e) {
			// TODO: handle exception

			orders.put("BUY", Collections.emptyList());

			orders.put("SELL", Collections.emptyList());

			return HttpResponseModel.builder().data(orders)
					.message("Exception Occured inside method findDepthOfCcyPairByInstituionId: " + e.getMessage())
					.build();
		}
	}

	@Override
	public HttpResponseModel findBuyInOrdersForBroker(String brokerId, int page, int pageSize) throws EntityNotFound {
		logTimeUpdate("findBuyInOrdersForBroker", "START");

		try {

			List<TransactionBrokerDetails> buyTransaction = new ArrayList<>();

			Direction buy = directionRepository.getById(1L);

//			InstitutionalEntity destination = institutionalEntityRepository.findById(Long.valueOf(brokerId)).get();
//
//			com.finmamba.entities.staticdata.EventType inEventType = eventTypeRepository.getById(1L);
//
//			com.finmamba.entities.staticdata.EventType inResponse = eventTypeRepository.getById(3L);
//
//			com.finmamba.entities.staticdata.EventType inNegotiation = eventTypeRepository.getById(5L);
//
//			com.finmamba.entities.staticdata.EventType inReject = eventTypeRepository.getById(7L);
//
//			com.finmamba.entities.staticdata.EventType inAwait = eventTypeRepository.getById(9L);
//
//			List<com.finmamba.entities.staticdata.EventType> events = new ArrayList();
//
//			events.add(inEventType);
//
//			events.add(inResponse);
//
//			events.add(inNegotiation);
//
//			events.add(inReject);
//
//			events.add(inAwait);
//
//			List<CounterpartyTransactionDetails> buyTransactions = counterPartyTransactionRepository
//					.findByDestinationIdAndDirectionIdAndIsRequestActiveAndEventTypeIn(destination, buy, true, events,
//							preparePageRequest(page, pageSize, org.springframework.data.domain.Sort.Direction.ASC,
//									"id"));
//
//			List<CounterPartyTransactionFetchingDto> buyTransactionList = new ArrayList();
//
//			for (CounterpartyTransactionDetails transaction : buyTransactions) {
//
//				buyTransactionList.add(modelMapper.map(transaction, CounterPartyTransactionFetchingDto.class));
//
//			}
//			
//			List<InstitutionalEntityAssociation> listOfEntities = institutionalEntityAssociationRepository.findByDestinationEntityAndByWillingToBuyAndByWillingToSell
//					(destination, true, true);

			buyTransaction = transactionBrokerDetailRepository.findAllByBrokerIdAndDirectionIdOrderByLastTransactionAtDesc(Long.valueOf(brokerId), buy,
					preparePageRequest(page, pageSize, org.springframework.data.domain.Sort.Direction.ASC,
							"id")
					);

			List<BrokerOrderDto> buyTransactionList = new ArrayList();

			
			for(TransactionBrokerDetails transaction: buyTransaction) {
				if(!transaction.getTransactionId().getIsConfirmed()) {
					buyTransactionList.add(modelMapper.map(transaction, BrokerOrderDto.class));
				}
			}
			
			logTimeUpdate("findBuyInOrdersForBroker", "END");

			return HttpResponseModel.builder().data(buyTransactionList)
					.total(transactionBrokerDetailRepository.countByBrokerIdAndDirectionId(Long.valueOf(brokerId), buy))
					.build();

		} catch (Exception e) {
			// TODO: handle exception

			return HttpResponseModel.builder().data(Collections.emptyList())
					.message("Exception Occured inside method findBuyInOrdersForBroker: " + e.getMessage()).build();
		}
	}

	@Override
	public HttpResponseModel findSellInOrdersForBroker(String brokerId, int page, int pageSize) throws EntityNotFound {
		logTimeUpdate("findSellInOrdersForBroker", "START");

		try {

			Direction sell = directionRepository.getById(2L);

			InstitutionalEntity destination = institutionalEntityRepository.findById(Long.valueOf(brokerId)).get();

//			com.finmamba.entities.staticdata.EventType inEventType = eventTypeRepository.getById(1L);
//
//			com.finmamba.entities.staticdata.EventType inResponse = eventTypeRepository.getById(3L);
//
//			com.finmamba.entities.staticdata.EventType inNegotiation = eventTypeRepository.getById(5L);
//
//			com.finmamba.entities.staticdata.EventType inReject = eventTypeRepository.getById(7L);
//
//			com.finmamba.entities.staticdata.EventType inAwait = eventTypeRepository.getById(9L);
//
//			List<com.finmamba.entities.staticdata.EventType> events = new ArrayList();
//
//			events.add(inEventType);
//
//			events.add(inResponse);
//
//			events.add(inNegotiation);
//
//			events.add(inReject);
//
//			events.add(inAwait);
//
//			List<CounterpartyTransactionDetails> sellTransactions = counterPartyTransactionRepository
//					.findByDestinationIdAndDirectionIdAndIsRequestActiveAndEventTypeIn(destination, sell, true, events,
//							preparePageRequest(page, pageSize, org.springframework.data.domain.Sort.Direction.ASC,
//									"id"));
//
//			List<CounterPartyTransactionFetchingDto> sellTransactionList = new ArrayList();
//
//			for (CounterpartyTransactionDetails transaction : sellTransactions) {
//
//				sellTransactionList.add(modelMapper.map(transaction, CounterPartyTransactionFetchingDto.class));
//
//			}

			List<TransactionBrokerDetails>sellTransactions = transactionBrokerDetailRepository.findAllByBrokerIdAndDirectionIdOrderByLastTransactionAtDesc(Long.valueOf(brokerId), sell,
					preparePageRequest(page, pageSize, org.springframework.data.domain.Sort.Direction.ASC,
							"id")
					);

			List<BrokerOrderDto> sellTransactionList = new ArrayList();

			
			for(TransactionBrokerDetails transaction: sellTransactions) {
				
				if(!transaction.getTransactionId().getIsConfirmed()) {
					sellTransactionList.add(modelMapper.map(transaction, BrokerOrderDto.class));
				}
			}
			
			
			logTimeUpdate("findSellInOrdersForBroker", "END");

			return HttpResponseModel.builder().data(sellTransactionList)
					.total(transactionBrokerDetailRepository.countByBrokerIdAndDirectionId(Long.valueOf(brokerId), sell))
					.build();

		} catch (Exception e) {
			// TODO: handle exception

			return HttpResponseModel.builder().data(Collections.emptyList())
					.message("Exception Occured inside method findSellInOrdersForBroker: " + e.getMessage()).build();
		}
	}

	@Override
	public HttpResponseModel intiateMatchingAlgorithms(MatchingTable order) {
		try {

			List<CounterpartyTransactionDetails> matchingOrders = match.matchRequest(order);

			List<CounterPartyTransactionFetchingDto> matchingTransaction = new ArrayList<>();

			for (CounterpartyTransactionDetails buyOrders : matchingOrders) {
				matchingTransaction.add(modelMapper.map(buyOrders, CounterPartyTransactionFetchingDto.class));
			}

			return HttpResponseModel.builder().message("Matching all your buy sell orders. Please wait ... ")
					.data(matchingTransaction).build();

		} catch (Exception e) {
			return HttpResponseModel.builder().data(Collections.emptyList())
					.message("Exception Occured inside method intiateMatchingAlgorithms: " + e.getMessage()).build();
		}

	}

	@Override
	public HttpResponseModel getLatestQuoteBySource(LatestQuoteDetailRequest latestQuoteDetailRequest) {
		try {

			com.finmamba.entities.staticdata.EventType inEventType = eventTypeRepository.getById(1L);

			com.finmamba.entities.staticdata.EventType inResponse = eventTypeRepository.getById(3L);

			com.finmamba.entities.staticdata.EventType inNegotiation = eventTypeRepository.getById(5L);

			List<com.finmamba.entities.staticdata.EventType> events = new ArrayList();

			events.add(inEventType);

			events.add(inResponse);

			events.add(inNegotiation);

			List<CounterpartyTransactionDetails> latestQuote = counterPartyTransactionRepository
					.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionIdAndEventTypeInOrderByIdDesc(
							institutionalEntityRepository.findById(latestQuoteDetailRequest.getSourceId()).get(),
							institutionalEntityRepository.findById(latestQuoteDetailRequest.getDestinationId()).get(),
							true, transactionRepository.findById(latestQuoteDetailRequest.getTransactionId()).get(),
							events);
			
			

			List<CounterPartyTransactionFetchingDto> latestOrder = new ArrayList<>();

			
			
			
			latestOrder.add(modelMapper.map(latestQuote.get(0), CounterPartyTransactionFetchingDto.class));
			

			return HttpResponseModel.builder().data(latestOrder).build();

		} catch (Exception e) {
			return HttpResponseModel.builder().data(Collections.emptyList())
					.message("Exception Occured inside method getLatestQuoteBySource: " + e.getMessage()).build();
		}
	}

	@Override
	public HttpResponseModel findInOutForTransactionForBroker(String transactionId, String institutionName)
			throws EntityNotFound {
		try {
			logTimeUpdate("findInOutForTransactionForBroker", "START");

			TransactionDetail transaction = transactionRepository.getById(Long.valueOf(transactionId));

			InstitutionalEntity institution = institutionalEntityRepository.getById(Long.valueOf(institutionName));

			Type listType;

			List<CounterpartyTransactionDetails> listAwaitingOrders = new ArrayList<>();

			com.finmamba.entities.staticdata.EventType inEventType = eventTypeRepository.getById(1L);

			com.finmamba.entities.staticdata.EventType inResponse = eventTypeRepository.getById(3L);

			com.finmamba.entities.staticdata.EventType inNegotiation = eventTypeRepository.getById(5L);

			com.finmamba.entities.staticdata.EventType inReject = eventTypeRepository.getById(7L);

			com.finmamba.entities.staticdata.EventType inAwait = eventTypeRepository.getById(9L);

			com.finmamba.entities.staticdata.EventType outEventType = eventTypeRepository.getById(2L);

			com.finmamba.entities.staticdata.EventType outResponse = eventTypeRepository.getById(4L);

			com.finmamba.entities.staticdata.EventType outNegotiation = eventTypeRepository.getById(6L);

			com.finmamba.entities.staticdata.EventType outReject = eventTypeRepository.getById(8L);

			List<com.finmamba.entities.staticdata.EventType> inEvents = new ArrayList<>();

			inEvents.add(inEventType);

			inEvents.add(inNegotiation);

			List<com.finmamba.entities.staticdata.EventType> outEvents = new ArrayList<>();

			outEvents.add(outEventType);

			outEvents.add(outNegotiation);

			outEvents.add(outReject);

			outEvents.add(outResponse);

			List<com.finmamba.entities.staticdata.EventType> rejects = new ArrayList<>();

			rejects.add(inReject);

			List<com.finmamba.entities.staticdata.EventType> confirmed = new ArrayList<>();

			confirmed.add(inResponse);

			List<CounterpartyTransactionDetails> inTransactions = counterPartyTransactionRepository
					.findAllByDestinationIdAndEventTypeInAndTransactionIdAndIsRequestActiveAndSourceIdNot(institution,
							inEvents, transaction, true, transaction.getSource());

			List<CounterpartyTransactionDetails> outTransactions = counterPartyTransactionRepository
					.findAllBySourceIdAndEventTypeInAndTransactionIdAndIsRequestActive(institution, outEvents,
							transaction, true);

			List<CounterpartyTransactionDetails> rejectedTransactions = counterPartyTransactionRepository
					.findAllByDestinationIdAndEventTypeInAndTransactionIdAndIsRequestActive(institution, rejects,
							transaction,

							true);

			List<CounterpartyTransactionDetails> confirmedTransactions = counterPartyTransactionRepository
					.findAllByDestinationIdAndEventTypeInAndTransactionIdAndIsRequestActive(institution, confirmed,
							transaction,

							true);


//			if (parentTransaction.getSource().getId() == Long.valueOf(institutionName)) {
//				// BuySide
//				listType = new TypeToken<List<BuySideTransactionDto>>() {
//				}.getType();
//			} else {
//				// SellSide
//				listType = new TypeToken<ArrayList<SellSideTransactionDto>>() {
//				}.getType();
//			}
			
			listType = new TypeToken<List<CounterPartyTransactionFetchingDto>>() {
			}.getType();

			Map<String, Object> mapInOut = new HashMap<>();

			mapInOut.put("in", modelMapper.map(inTransactions, listType));
			mapInOut.put("out", modelMapper.map(outTransactions, listType));
			mapInOut.put("confirm", modelMapper.map(confirmedTransactions, listType));
			mapInOut.put("reject", modelMapper.map(rejectedTransactions, listType));
			mapInOut.put("awaiting", modelMapper.map(listAwaitingOrders, listType));

			logTimeUpdate("findInOutForTransactionForBroker", "END");

			// Publish request to be displayed on broker view
			return HttpResponseModel.builder().data(mapInOut).build();
		} catch (Exception e) {
			Map<String, Object> mapInOut = new HashMap<>();

			mapInOut.put("in", Collections.emptyList());
			mapInOut.put("out", Collections.emptyList());
			mapInOut.put("confirm", Collections.emptyList());
			mapInOut.put("reject", Collections.emptyList());
			mapInOut.put("awaiting", Collections.emptyList());

			logTimeUpdate("findInOutForTransactionForBroker", "END");

			return HttpResponseModel.builder().data(mapInOut).message(e.getMessage()).build();

		}
	}

	@Override
	public HttpResponseModel getBuyOrdersFromBroker(Long brokerId) {
		try {
			
			java.util.ArrayList<BrokerOrderDto> orders = new ArrayList<>();
			
			Direction buy = directionRepository.getById(1L);
			
			List<BrokerToBrokerTransactionDetails>buyOrders = brokerToBrokerRepository.findAllByDirectionIdAndBrokerIdAndIsActiveOrderByLastTransactionAtDesc
					(buy, brokerId,
							true);
			
			Type listType = new TypeToken<List<BrokerToBrokerDto>>() {
			}.getType();
			
//			for(BrokerToBrokerTransactionDetails order: buyOrders) {
//				orders.add(modelMapper.map(order, listType));
//			}
			
			return HttpResponseModel.builder().data(modelMapper.map(buyOrders, listType)).message("Fetched all buy orders via Broker!").build();

			
		} catch (Exception e) {
			return HttpResponseModel.builder().data(Collections.emptyList()).message(e.getMessage()).build();

		}
	}
	
	@Override
	public HttpResponseModel getSellOrdersFromBroker(Long brokerId) {
		try {
			
			java.util.ArrayList<BrokerOrderDto> orders = new ArrayList<>();
			
			Direction sell = directionRepository.getById(2L);
			
			List<BrokerToBrokerTransactionDetails>buyOrders = brokerToBrokerRepository.findAllByDirectionIdAndBrokerIdAndIsActiveOrderByLastTransactionAtDesc
					(sell, brokerId,
							true);
			
			Type listType = new TypeToken<List<BrokerToBrokerDto>>() {
			}.getType();
			
//			for(BrokerToBrokerTransactionDetails order: buyOrders) {
//				orders.add(modelMapper.map(order, listType));
//			}
			
			return HttpResponseModel.builder().data(modelMapper.map(buyOrders, listType)).message("Fetched all buy orders via Broker!").build();

			
		} catch (Exception e) {
			return HttpResponseModel.builder().data(Collections.emptyList()).message(e.getMessage()).build();

		}
	}

}
