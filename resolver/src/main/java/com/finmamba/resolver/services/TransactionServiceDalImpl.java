package com.finmamba.resolver.services;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.CurrencyPairDetails;
import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.staticdata.Product;
import com.finmamba.entities.staticdata.RequestType;
import com.finmamba.entities.staticdata.Tenor;
import com.finmamba.entities.transaction.Customer;
import com.finmamba.entities.transaction.Daddy;
import com.finmamba.entities.transaction.InstitutionalEntityAssociation;
import com.finmamba.entities.transaction.KotakWorld;
import com.finmamba.entities.transaction.RequestQuote;
import com.finmamba.entities.transaction.TransactionDetail;
import com.finmamba.entities.transaction.User;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.bus.EventType;
import com.finmamba.models.bus.SerialDataBusModel;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.InstitutionalEntityDto;
import com.finmamba.models.dto.KotakWorldDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.models.request.RecordProcessedStatusDetail;
import com.finmamba.models.request.UserProfileModel;
import com.finmamba.models.transaction.FXRequestModel;
import com.finmamba.models.transaction.Outright;
import com.finmamba.models.transaction.QuoteRequestModel;
import com.finmamba.models.transaction.SourceDestination;
import com.finmamba.models.transaction.SpotRate;
import com.finmamba.models.transaction.SwapPoint;
import com.finmamba.resolver.exception.EntityNotFound;
import com.finmamba.resolver.repositories.CurrencyPairDetailsRepository;
import com.finmamba.resolver.repositories.CurrencyRepository;
import com.finmamba.resolver.repositories.CustomerRepository;
import com.finmamba.resolver.repositories.CustomerSegmentRepository;
import com.finmamba.resolver.repositories.DaddyRepository;
import com.finmamba.resolver.repositories.DirectionRepository;
import com.finmamba.resolver.repositories.EntityDetailsRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityAssociationRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.repositories.KotakWorldRepository;
import com.finmamba.resolver.repositories.ProductRepository;
import com.finmamba.resolver.repositories.QuoteRequestRepository;
import com.finmamba.resolver.repositories.RecordStatusRepository;
import com.finmamba.resolver.repositories.RequestQuoteRepository;
import com.finmamba.resolver.repositories.RequestTypeRepository;
import com.finmamba.resolver.repositories.TenorRepository;
import com.finmamba.resolver.repositories.UserRepository;
import com.finmamba.resolver.util.ApplicationConstants;
import com.finmamba.resolver.util.ApplicationConstants.RecordStatus;
import com.finmamba.resolver.util.UniqueIdentifierGenerator;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransactionServiceDalImpl implements TransactionServiceDal {

//	@Value("${app.producer-topic-name}")
//	private String responseTopic;
//
//	@Value("${app.time-to-live}")
//	private int ttl;

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
	RequestQuoteRepository requestQuoteRepository;

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
	DaddyRepository daddyRepository; //will be obseleted
	
	@Autowired
	QuoteRequestRepository quoteRequestRepository;

	@Autowired
	KotakWorldRepository kotakWorldRepository;;

	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	EventPublisher eventPublisher;

	@Autowired
	Gson gson;

	@Autowired
	ModelMapper modelMapper;

//	@Autowired
//	HistoryWriter historyWriter;

	@Override
	public Boolean updateRecordProcessedStatus(RecordProcessedStatusDetail processedStatusDetail)
			throws EntityNotFound {
		// Find the record entity object
		RequestQuote record = requestQuoteRepository.findById(processedStatusDetail.getRecordId())
				.orElseThrow(() -> new EntityNotFound("Record npt fpund"));

		// Set request processed flag
		record.setRecordProcessed(processedStatusDetail.getProcessedStatus());

		// Save record
		requestQuoteRepository.save(record);

		// return status
		return Boolean.TRUE;
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


		long customerId = quoteRequestModel.getCustomerId();
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
		TransactionDetail quoteRequest  = TransactionDetail.builder().product(product).buyAmount(bankBuyAmount).sellAmount(bankSellAmount)
				
				.sourceOutright(bankOutright).spreadOutright(spreadOutright).destinationOutright(clientOutright)
				.tenorUnit(tenorUnit).source(findInstitutionalEntityFromId(quoteRequestModel.getInstitutionalEntityId()))
				.transactionId(UniqueIdentifierGenerator.unique())
				.baseCurrency(baseCurrency).quoteCurrency(quoteCurrency).customer(customer)
				.transactionId(UniqueIdentifierGenerator.unique()).buySellDirection(direction)
				.createdAt(Instant.now())
				.createdBy(userId)
				.build();

		TransactionDetail savedRequest = quoteRequestRepository.save(quoteRequest);

		log.info("Quote request saved with id: " + savedRequest.getId());

		QuoteRequestDto quoteRequestDto = modelMapper.map(savedRequest, QuoteRequestDto.class);
		
		List<QuoteRequestDto> listQuoteRequest = new ArrayList<>();
		listQuoteRequest.add(quoteRequestDto);

		// Publish new daddy request event
		eventPublisher.publishEvent(quoteRequestDto);

		return listQuoteRequest;
	}

	@Override
	public List<RequestQuote> saveRequestFromBroker(RequestQuote request)
			throws EntityNotFound, IllegalArgumentException {
		// Get parent id saved id
		long parentId = request.getId();

		// Update request record status flag to INTRANSIT
		request.setRecordStatus(recordStatusRepository.getById(RecordStatus.INTRANSIT.getAction()));
		requestQuoteRepository.save(request);
		//

		SourceDestination sourceDestinationModel = SourceDestination.builder()
				.sourceEntityId(request.getDestination().getId()).prevSourceEntityId(request.getSource().getId())
				.build();

		/*
		 * Make a new entity object by setting id = null
		 */
		request.setId(null);

		/*
		 * Parent is the id of request from initiator
		 */
		request.setParentId(parentId);
		/*
		 * Destination of the request initiator now becomes the source Flow: 1 -> 2 -> 3
		 * Request from initiator: 1 -> 2 Request from the broker: 2 -> 3
		 */
		request.setSource(request.getDestination());

		// request.setTimeToLive(ttl);

		request.setRequestActive(true);

		request.setRecordStatus(recordStatusRepository.getById(RecordStatus.ACTIVE.getAction()));

		/*
		 * Persist broker request for all eligible counter parties Each request will be
		 * shown on the accordion
		 */

		List<InstitutionalEntity> listEligibleCounterparties = findEligibleCounterparties(sourceDestinationModel);
		List<RequestQuote> listRequests = new ArrayList<>();
		List<SourceDestination> listSourceDestination = new ArrayList<>();
		listEligibleCounterparties.forEach(counterparty -> {
			try {
				RequestQuote clonedRequest = (RequestQuote) request.clone();
				clonedRequest.setDestination(counterparty);
				listRequests.add(clonedRequest);
				listSourceDestination.add(SourceDestination.builder().sourceEntityId(clonedRequest.getSource().getId())
						.destinationEntityId(counterparty.getId()).build());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		});
		List<RequestQuote> savedBrokerRequest = requestQuoteRepository.saveAllAndFlush(listRequests);
		List<RequestQuote> returnableBrokerRequest = new ArrayList<RequestQuote>();

		savedBrokerRequest.stream().forEach((savedRequest) -> {
			returnableBrokerRequest.add(requestQuoteRepository.findById(savedRequest.getId()).get());
		});

		Assert.isTrue(savedBrokerRequest.size() != -0, "The broker request could not be saved");
		return returnableBrokerRequest;
	}

	@Override
	public RequestQuote saveCounterpartyResponse(RequestQuote request) throws EntityNotFound, IllegalArgumentException {
		// Get parent id saved id
		long parentId = request.getId();

		/*
		 * Make a new entity object by setting id = null
		 */
		request.setId(null);

		/*
		 * Parent is the id of request from initiator
		 */
		request.setParentId(parentId);
		/*
		 * Destination of the request initiator now becomes the source Flow: 1 -> 2 -> 3
		 * Request from initiator: 1 -> 2 Request from the broker: 2 -> 3
		 */
		InstitutionalEntity source = request.getSource();
		InstitutionalEntity destination = request.getDestination();
		request.setSource(destination);
		request.setDestination(source);

		// request.setTimeToLive(ttl);

		request.setRequestActive(true);

		// Decide whether confirm or negotiation
		request.setRecordStatus(recordStatusRepository.getById(RecordStatus.CONFIRMED.getAction()));
		/*
		 * Persist counterparty response
		 */

		RequestQuote savedCounterpartyResponse = requestQuoteRepository.save(request);
		Assert.isTrue(savedCounterpartyResponse.getId() != -1, "The quote could not be saved");
		return savedCounterpartyResponse;
	}

	@Override
	public RequestQuote saveBrokerResponse(RequestQuote request) throws EntityNotFound, IllegalArgumentException {
		// Get parent id saved id
		long parentId = request.getId();

		/*
		 * Make a new entity object by setting id = null
		 */
		request.setId(null);

		/*
		 * Parent is the id of the previous record in the transaction chain The entire
		 * transaction can be traced back to the initiator by transaction id and parent
		 * ids
		 */
		request.setParentId(parentId);

		// Find the initiator request to obtain the destination. This completed the
		// request response loop for happy flow
		InstitutionalEntity destination = requestQuoteRepository
				.findByTransactionIdAndParentId(request.getTransactionId(), -1L).getSource();

		/*
		 * Destination of this record becomes the source of next record in the flow 2 ->
		 * 1
		 */
		InstitutionalEntity source = request.getDestination();

		request.setSource(source);
		request.setDestination(destination);

		// request.setTimeToLive(ttl);

		request.setRequestActive(true);

		// Decide whether confirm or negotiation
		request.setRecordStatus(recordStatusRepository.getById(RecordStatus.CONFIRMED.getAction()));

		/*
		 * Persist broker response
		 */

		RequestQuote savedBrokerResponse = requestQuoteRepository.save(request);
		Assert.isTrue(savedBrokerResponse.getId() != -1, "The quote could not be saved");
		return savedBrokerResponse;
	}

	/******************************************
	 * FIND START
	 **************************************/

	@Override
	@Transactional
	public Map<String, Object> findActiveRequestsForInstitution(String institutionName, int page, int pageSize)
			throws EntityNotFound {

//		List<Daddy> listRequestByUser = daddyRepository.findActiveRequestForInstitutionsWithValidity(institutionId,
//				RecordStatus.ACTIVE.getAction());

		List<Daddy> listRequestByUser = daddyRepository.findActiveRequestForInstitutionsWithValidity(institutionName);

		List<String> sortOrder = daddyRepository.getSortOrderForInstitutionName(institutionName);

		List<String> listRequestNoDuplicates = Lists.newArrayList(Sets.newLinkedHashSet(sortOrder));

		List<Daddy> customSortedList = new ArrayList<>();

		listRequestNoDuplicates.stream().forEach(transactionId -> {
			customSortedList.add(listRequestByUser.stream()
					.filter(daddyRequest -> daddyRequest.getTransactionId().equalsIgnoreCase(transactionId)).findFirst()
					.orElse(null));
		});

		// Implement pagination like logic
		int fromIndex = (page - 1) * pageSize;
		List<Daddy> customSortedSubList = new ArrayList<>();
		if (fromIndex < customSortedList.size()) {
			customSortedSubList = customSortedList.subList(fromIndex,
					Math.min(fromIndex + pageSize, customSortedList.size()));
		}

		customSortedSubList.removeAll(Collections.singletonList(null));

		Type listType = new TypeToken<List<DaddyDto>>() {
		}.getType();
		List<DaddyDto> daddyDtoList = modelMapper.map(customSortedSubList, listType);

		/*
		 * To store total count of records fetched in the list
		 */
		Map<String, Object> responseMap = new LinkedHashMap<>();
		responseMap.put("totalCount", listRequestByUser.size());
		responseMap.put("data", daddyDtoList);

		// Publish request to be displayed on broker view
		return responseMap;
	}

	@Override
	@Transactional
	public Map<String, Object> findConfirmedOrdersForInstitution(String institutionName, int page, int pageSize)
			throws EntityNotFound {

		List<Daddy> listRequestConfirmed = daddyRepository.findConfirmedOrderForInstitution(institutionName);

//		log.info(listRequestConfirmed.get(0).toString());
		
		// Implement pagination like logic
		int fromIndex = (page - 1) * pageSize;
		List<Daddy> customSortedSubList = new ArrayList<>();
		if (fromIndex < listRequestConfirmed.size()) {
			customSortedSubList = listRequestConfirmed.subList(fromIndex,
					Math.min(fromIndex + pageSize, listRequestConfirmed.size()));
		}

		Type listType = new TypeToken<List<DaddyDto>>() {
		}.getType();
		List<DaddyDto> listRequestConfirmedDtoList = modelMapper.map(customSortedSubList, listType);

		/*
		 * To store total count of records fetched in the list
		 */
		Map<String, Object> responseMap = new LinkedHashMap<>();
		responseMap.put("totalCount", listRequestConfirmedDtoList.size());
		responseMap.put("data", listRequestConfirmedDtoList);

		// Publish request to be displayed on broker view
		return responseMap;
	}

	@Override
	@Transactional
	public Map<String, List<SuperInstitutionalEntityDto>> findInOutForTransaction(String transactionId,
			String institutionName) throws EntityNotFound {
		
		// Applicable to all realms
		// Kotak world used only as placeholder
		List<KotakWorld> listRequestIn = kotakWorldRepository.findInForTransactionId(transactionId, institutionName);

		List<KotakWorld> listRequestOut = kotakWorldRepository.findOutForTransactionId(transactionId, institutionName);

		List<KotakWorld> listRequestConfirmed = kotakWorldRepository.findConfirmedOrderForTransactionId(transactionId,
				institutionName);

		List<KotakWorld> listRequestReject = kotakWorldRepository.findRejectedOrderForTransactionId(transactionId,
				institutionName);
		
		List<KotakWorld> listAwaitingOrders = kotakWorldRepository.findAwaitingOrderForTransactionId(transactionId, institutionName);

		Type listType = new TypeToken<List<SuperInstitutionalEntityDto>>() {
		}.getType();
//		List<DaddyDto> daddyInDtoList = modelMapper.map(listRequestIn, listType);
//		List<DaddyDto> daddyOutDtoList = modelMapper.map(listRequestOut, listType);

		/**
		 * Sort the collection basis id descending
		 */
//		Collections.sort(daddyInDtoList, new Comparator<DaddyDto>() {
//			@Override
//			public int compare(final DaddyDto object1, final DaddyDto object2) {
//				return object2.getId().compareTo(object1.getId());
//			}
//		});

		/**
		 * Sort the collection basis id descending
		 */
//		Collections.sort(daddyOutDtoList, new Comparator<DaddyDto>() {
//			@Override
//			public int compare(final DaddyDto object1, final DaddyDto object2) {
//				return object2.getId().compareTo(object1.getId());
//			}
//		});

		Map<String, List<SuperInstitutionalEntityDto>> mapInOut = new HashMap<>();

		mapInOut.put("in", modelMapper.map(listRequestIn, listType));
		mapInOut.put("out", modelMapper.map(listRequestOut, listType));
		mapInOut.put("confirm", modelMapper.map(listRequestConfirmed, listType));
		mapInOut.put("reject", modelMapper.map(listRequestReject, listType));
		mapInOut.put("awaiting", modelMapper.map(listAwaitingOrders,listType));

		// Publish request to be displayed on broker view
		return mapInOut;
	}

	@Override
	public List<RequestQuote> findInTransitRequestsForInstitution(long institutionId) throws EntityNotFound {

		List<RequestQuote> listInTransitRequest = requestQuoteRepository
				.findInTransitRequestForSourceInstitutionsWithValidity(institutionId);

//		List<RequestQuote> listRequestByDestinationAndStatus =
//				 requestQuoteRepository.findInTransitRequestForDestinationInstitutionsWithValidity(institutionId, RecordStatus.INTRANSIT.getAction());

//		listInTransitRequest.addAll(listRequestByDestinationAndStatus);

		// Group by transaction id

//		Map<String, List<RequestQuote>> studlistGrouped =
//				listInTransitRequest.stream().collect(Collectors.groupingBy(w -> w.getTransactionId()));

		// prepare placeholder list
//		List<RequestQuote> placeHolderList = new ArrayList<>();
//		studlistGrouped.forEach((k, v) -> {
//			placeHolderList.add(v.get(0));
//		});

		// then sort groups by minimum id in each of them
//		List<RequestQuote> sorted = studlistGrouped.entrySet().stream()
//		        .sorted(Comparator.comparing(e -> e.getValue().stream().map(RequestQuote::getId).min(Comparator.naturalOrder()).orElse(0)))
//		        .flatMap(e -> e.getValue().stream().sorted(Comparator.comparing(RequestQuote::getId))).collect(Collectors.toList());

		/**
		 * Sort the collection basis id descending
		 */
		/*
		 * Collections.sort(listRequestByUser, new Comparator<RequestQuote>() {
		 * 
		 * @Override public int compare(final RequestQuote object1, final RequestQuote
		 * object2) { return object2.getId().compareTo(object1.getId()); } });
		 */

		// Publish request to be displayed on broker view
		return listInTransitRequest;
	}

	@Override
	public List<RequestQuote> findInTransitRequestsForTransaction(Double institutionId, String transactionId)
			throws EntityNotFound {

		List<RequestQuote> listInTransitRequest = requestQuoteRepository
				.findInTransitRequestForSourceInstitutionsAndTransactionWithValidity(institutionId.longValue(),
						transactionId);

		List<RequestQuote> listRequestByDestinationAndStatus = requestQuoteRepository
				.findInTransitRequestForDestinationAndTransactionInstitutionsWithValidity(institutionId.longValue(),
						RecordStatus.INTRANSIT.getAction(), transactionId);

		listInTransitRequest.addAll(listRequestByDestinationAndStatus);

		return listInTransitRequest;
	}

	@Override
	public List<RequestQuote> findExpiredRequestsForInstitution(long institutionId) throws EntityNotFound {

		List<RequestQuote> listRequestByUser = requestQuoteRepository
				.findExpiredRequestForInstitutionsWithValidity(institutionId, RecordStatus.EXPIRED.getAction());

		/**
		 * Sort the collection basis id descending
		 */
		Collections.sort(listRequestByUser, new Comparator<RequestQuote>() {
			@Override
			public int compare(final RequestQuote object1, final RequestQuote object2) {
				return object2.getId().compareTo(object1.getId());
			}
		});

		// Publish request to be displayed on broker view
		return listRequestByUser;
	}

	@Override
	public List<RequestQuote> findConfirmedRequestsForInstitution(long institutionId) throws EntityNotFound {

		List<RequestQuote> listRequestByUser = requestQuoteRepository
				.findConfirmedRequestForInstitutionsWithValidity(institutionId, RecordStatus.CONFIRMED.getAction());

		/**
		 * Sort the collection basis id descending
		 */
		Collections.sort(listRequestByUser, new Comparator<RequestQuote>() {
			@Override
			public int compare(final RequestQuote object1, final RequestQuote object2) {
				return object2.getId().compareTo(object1.getId());
			}
		});

		// Publish request to be displayed on broker view
		return listRequestByUser;
	}

	/******************************************
	 * FIND END
	 **************************************/

	@Override
	public RequestQuote findQuoteRequestById(long requestId) throws EntityNotFound {
		// Find request by id
		RequestQuote request = requestQuoteRepository.findById(requestId)
				.orElseThrow(() -> new EntityNotFound("Request not found"));

		// Publish request to be displayed on broker view
		return request;
	}

	@Override
	public boolean findStatusOfTransaction(long requestId) throws EntityNotFound {
		// Find request by id
		RequestQuote request = requestQuoteRepository.findById(requestId)
				.orElseThrow(() -> new EntityNotFound("Request not found"));

		// Publish request to be displayed on broker view

		return request.isRequestActive();
	}

	@Override
	public List<RequestQuote> findQuoteRequestByUserId(long userId, boolean requestStatusFlag) throws EntityNotFound {
		// Find user
		// User user = userRepository.findById(userId).orElseThrow(() -> new
		// EntityNotFound("User not found"));

		// Find request by user
//		List<RequestQuote> listRequestByUser = requestQuoteRepository.findByUserAndIsRequestActive(user,
//				requestStatusFlag);
		List<RequestQuote> listRequestByUser = requestQuoteRepository.findByUserAndIsRequestActiveWithValidity(userId,
				requestStatusFlag);

		/**
		 * Sort the collection basis id descending
		 */
		Collections.sort(listRequestByUser, new Comparator<RequestQuote>() {
			@Override
			public int compare(final RequestQuote object1, final RequestQuote object2) {
				return object2.getId().compareTo(object1.getId());
			}
		});

		// Publish request to be displayed on broker view
		log.info("Publishing request Object");
		return listRequestByUser;
	}

	@Override
	public List<RequestQuote> findQuoteRequestByBrokerId(long brokerId) throws EntityNotFound {
		// Find user
		InstitutionalEntity broker = institutionalEntityRepository.findById(brokerId)
				.orElseThrow(() -> new EntityNotFound("Broker not found"));

		// Find request by user
		// List<RequestQuote> listRequestByBroker =
		// requestQuoteRepository.findByDestination(broker);
		// To find all the requests by brokers
		List<RequestQuote> listRequestByBroker = requestQuoteRepository.findBySource(broker);

		/**
		 * Sort the collection basis id descending
		 */
		Collections.sort(listRequestByBroker, new Comparator<RequestQuote>() {
			@Override
			public int compare(final RequestQuote object1, final RequestQuote object2) {
				return object2.getId().compareTo(object1.getId());
			}
		});

		// Publish request to be displayed on broker view
		log.info("Publishing request Object");
		return listRequestByBroker;
	}

	@Override
	public List<RequestQuote> findQuoteRequestForBrokerId(long brokerId, boolean recordProcessedStatus)
			throws EntityNotFound {
		// Find user
		InstitutionalEntity broker = institutionalEntityRepository.findById(brokerId)
				.orElseThrow(() -> new EntityNotFound("Broker not found"));

		// Find request by user
		// List<RequestQuote> listRequestByBroker =
		// requestQuoteRepository.findByDestination(broker);
		// To find all the requests by brokers
		// List<RequestQuote> listRequestByBroker =
		// requestQuoteRepository.findByDestination(broker);
		List<RequestQuote> listRequestByBroker = requestQuoteRepository.findByDestinationAndIsRecordProcessed(broker,
				recordProcessedStatus);
		/**
		 * Sort the collection basis id descending
		 */
		Collections.sort(listRequestByBroker, new Comparator<RequestQuote>() {
			@Override
			public int compare(final RequestQuote object1, final RequestQuote object2) {
				return object2.getId().compareTo(object1.getId());
			}
		});

		// Publish request to be displayed on broker view
		log.info("Publishing request Object");
		return listRequestByBroker;
	}

	@Override
	public List<RequestQuote> findExpiredQuoteRequestForBrokerId(long brokerId) throws EntityNotFound {
		// Find user
//		InstitutionalEntity broker = institutionalEntityRepository.findById(brokerId)
//				.orElseThrow(() -> new EntityNotFound("Broker not found"));

		// Find request by user
		// List<RequestQuote> listRequestByBroker =
		// requestQuoteRepository.findByDestination(broker);
		// To find all the requests by brokers
		// List<RequestQuote> listRequestByBroker =
		// requestQuoteRepository.findByDestination(broker);
		List<RequestQuote> listRequestByBroker = requestQuoteRepository
				.findByBrokerAndIsRequestActiveWithValidity(brokerId, false);
		/**
		 * Sort the collection basis id descending
		 */
		Collections.sort(listRequestByBroker, new Comparator<RequestQuote>() {
			@Override
			public int compare(final RequestQuote object1, final RequestQuote object2) {
				return object2.getId().compareTo(object1.getId());
			}
		});

		// Publish request to be displayed on broker view
		log.info("Publishing request Object");
		return listRequestByBroker;
	}

	private SerialDataBusModel serialDataBusModelBuilder(Object payload, EventType eventType) {
		// @// @formatter:off
		return SerialDataBusModel.builder().payload(payload).eventType(eventType)
				.messageTimestamp(LocalDateTime.now().toString()).build();
		// @formatter:on
	}

	@Override
	public boolean forwardRequestToCounterparties(long requestId) throws EntityNotFound {

		RequestQuote request = requestQuoteRepository.findById(requestId)
				.orElseThrow(() -> new EntityNotFound("Request with id: " + requestId + " not found"));

		// Run this logic on button click
		// Broker to add spread then release to mapped banks.
		// ConfigureTopicName and publish request
		log.info("Publishing request ...");

		Message<String> requestMessage = MessageBuilder.withPayload(request.toString()).build();

		List<InstitutionalEntityAssociation> willingToBuy = Collections.emptyList();
		String directionString = request.getBuySellDirection().getName();
		InstitutionalEntity source = request.getSource();
		if (directionString.equalsIgnoreCase("BUY")) {
			// Calculate customer rate
			// CustomerRate = BankRate + Spread
			// customerRate = bankRate.add(spread);

			willingToBuy = institutionalEntityAssociationRepository
					.findDestinationEntityByWillingToBuyAndSourceEntityAndDestinationEntityNot(true, source, source);
			log.info(willingToBuy.toString());
		}

		willingToBuy.forEach(seller -> kafkaProducerService.send(
				serialDataBusModelBuilder(requestMessage, EventType.REQUEST),
				ApplicationConstants.BROKER_TO_COUNTERPARTY_PREFIX + seller.getDestinationEntity().getShortName(), null,
				null));
		log.info("Request published to banks");
		return true;

	}

	@Override
	public List<InstitutionalEntity> findEligibleCounterparties(SourceDestination sourceDestinationModel)
			throws EntityNotFound {

		List<InstitutionalEntity> eligibleSellers = new ArrayList<>();

		InstitutionalEntity sourceEntity = institutionalEntityRepository
				.findById(sourceDestinationModel.getSourceEntityId())
				.orElseThrow(() -> new EntityNotFound("Source not found"));

		InstitutionalEntity prevSourceEntity = institutionalEntityRepository
				.findById(sourceDestinationModel.getPrevSourceEntityId())
				.orElseThrow(() -> new EntityNotFound("prevSourceEntity not found"));

		List<InstitutionalEntityAssociation> institutionalEntityAssociations = institutionalEntityAssociationRepository
				.findDestinationEntityByWillingToBuyAndSourceEntityAndDestinationEntityNot(true, sourceEntity,
						prevSourceEntity);

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
			throw new EntityNotFound("No associations found for source: " + sourceEntity.getShortName());
		}
		// TODO: Need to check failure
		for (InstitutionalEntityAssociation association : institutionalEntityAssociations) {
			eligibleSellers.add(association.getDestinationEntity());
		}

		return eligibleSellers;
	}

	@Override
	public InstitutionalEntity findInstitutionEntity(long institutionalEntityId) throws EntityNotFound {

		return institutionalEntityRepository.findById(institutionalEntityId)
				.orElseThrow(() -> new EntityNotFound("Institutional Entity not found"));
	}

	@Override
	public boolean expireOrderRequest(Long parentId, String reasonForExpiry)
			throws EntityNotFound, IllegalArgumentException {
		RequestQuote quoteRequest = requestQuoteRepository.findById(parentId)
				.orElseThrow(() -> new EntityNotFound("Request not found"));

		quoteRequest.setRequestActive(false);

		log.info("Request expired");

		requestQuoteRepository.save(quoteRequest);

		return true;
	}

	@Override
	public boolean expireOrderRequest(String transactionId, String reasonForExpiry)
			throws EntityNotFound, IllegalArgumentException {
		log.info("Expire transaction with tid: " + transactionId);
		Daddy daddyRequest = daddyRepository.findByTransactionId(transactionId);
		daddyRequest.setRequestActive(false);
		daddyRepository.save(daddyRequest);
		log.info("Request expired");
		return true;
	}

	@Override
	public InstitutionalEntityDto findInstitutionalEntityFromUserProfile(UserProfileModel userProfileModel)
			throws EntityNotFound {
		// Use model mapper to expose only relevant fields in the service and
		// presentation layer

		return modelMapper.map(
				institutionalEntityRepository.findByRealmNameAndClientRoleNameAndClientNameAndGroupName(
						userProfileModel.getRealmName(), userProfileModel.getClientRoleName(),
						userProfileModel.getClientName(), userProfileModel.getGroupName()),
				InstitutionalEntityDto.class);

	}

	@Override
	public TransactionDetail findDaddyRequestByTransactionId(String transactionId) {
		// Use model mapper to expose only relevant fields in the service and
		// presentation layer
		return null;
		//return daddyRepository.findByTransactionId(transactionId);

	}

	@Override
	public InstitutionalEntity findInstitutionalEntityFromId(long id) throws EntityNotFound {
		return institutionalEntityRepository.findById(id)
				.orElseThrow(() -> new EntityNotFound("Institutional Entity not found"));
	}

	@Transactional
	@Override
	public Map<String, Object> findInForInstitution(String institutionName, int page, int pageSize)
			throws EntityNotFound {
		Type listType = new TypeToken<List<KotakWorldDto>>() {
		}.getType();

		List<KotakWorld> kotakWorldList = kotakWorldRepository.getInEventForInstitution(institutionName);
		List<KotakWorld> kotakWorldSubList = new ArrayList<>();

		// Implement pagination like logic
		int fromIndex = (page - 1) * pageSize;
		if (fromIndex < kotakWorldList.size()) {
			kotakWorldSubList = kotakWorldList.subList(fromIndex,
					Math.min(fromIndex + pageSize, kotakWorldList.size()));
		}
		List<KotakWorldDto> kotakWorldDtoList = modelMapper.map(kotakWorldSubList, listType);

		/*
		 * To store total count of records fetched in the list
		 */
		Map<String, Object> responseMap = new LinkedHashMap<>();
		responseMap.put("totalCount", kotakWorldDtoList.size());
		responseMap.put("data", kotakWorldDtoList);

		// kotakWorldDtoList.get(0).setTotalCount(kotakWorldList.size());
		return responseMap;
	}
}
