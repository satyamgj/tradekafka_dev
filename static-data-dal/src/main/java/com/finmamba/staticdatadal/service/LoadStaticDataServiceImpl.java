package com.finmamba.staticdatadal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.finmamba.entities.staticdata.AssetClass;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.CurrencyPairDetails;
import com.finmamba.entities.staticdata.CustomerSegment;
import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.staticdata.EntityRole;
import com.finmamba.entities.staticdata.GeographicalEntity;
import com.finmamba.entities.staticdata.Product;
import com.finmamba.entities.staticdata.ProductValidityMap;
import com.finmamba.entities.staticdata.RequestType;
import com.finmamba.entities.staticdata.Tenor;
import com.finmamba.entities.staticdata.TimerUnit;
import com.finmamba.entities.staticdata.UserRole;
import com.finmamba.kafka.starter.KafkaEventModel;
import com.finmamba.models.bus.SerialDataBusModel;
import com.finmamba.models.bus.StaticDataKeys;
import com.finmamba.staticdatadal.repositories.AssetClassRepository;
import com.finmamba.staticdatadal.repositories.InstitutionalEntityRepository;
import com.finmamba.staticdatadal.repositories.InstitutionalEntityRoleRepository;
import com.finmamba.staticdatadal.repositories.CurrencyPairDetailsRepository;
import com.finmamba.staticdatadal.repositories.CurrencyRepository;
import com.finmamba.staticdatadal.repositories.CustomerSegmentRepository;
import com.finmamba.staticdatadal.repositories.DirectionRepository;
import com.finmamba.staticdatadal.repositories.GeographicalEntityRepository;
import com.finmamba.staticdatadal.repositories.ProductRepository;
import com.finmamba.staticdatadal.repositories.ProductValidityMapRepository;
import com.finmamba.staticdatadal.repositories.RequestTypeRepository;
import com.finmamba.staticdatadal.repositories.TenorRepository;
import com.finmamba.staticdatadal.repositories.TimerUnitRepository;
import com.finmamba.staticdatadal.repositories.UserRoleRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoadStaticDataServiceImpl implements LoadStaticDataService {

	@Autowired
	AssetClassRepository assetClassRepository;
	@Autowired
	InstitutionalEntityRepository institutionalEntityRepository;
	@Autowired
	CurrencyRepository currencyRepository;
	@Autowired
	CustomerSegmentRepository customerSegmentRepository;
	@Autowired
	CurrencyPairDetailsRepository currencyPairDetailsRepository;
	@Autowired
	DirectionRepository directionRepository;
	@Autowired
	GeographicalEntityRepository entityDetailsRepository;
	@Autowired
	ProductRepository productRepository;
	@Autowired
	ProductValidityMapRepository productValidityMapRepository;
	@Autowired
	RequestTypeRepository requestTypeRepository;
	@Autowired
	TenorRepository tenorRepository;
	@Autowired
	TimerUnitRepository timerUnitRepository;
	@Autowired
	UserRoleRepository userRoleRepository;
	@Autowired
	InstitutionalEntityRoleRepository institutionalEntityRoleRepository;

	@Autowired
	KafkaProducerService kafkaProducerService;

	private static final String HASHREFERENCE = "matter-list";
	EnumMap<StaticDataKeys, Object> STATICDATA = new EnumMap<>(StaticDataKeys.class);

	/** CACHE POC | Starts */

	@Resource(name = "redisTemplate") // 'redisTemplate' is defined as a Bean in AppConfig.java
	private HashOperations<String, StaticDataKeys, Object> hashOperations;

	@Override
	@EventListener
	public void eventTopicListener(KafkaEventModel event) throws JsonProcessingException {

		log.info("Listening kafka topic: static-data-request-1");
		String data = event.getMessage();
		Gson gson = new Gson();
		JsonObject convertedObject = gson.fromJson(data, JsonObject.class);
		JsonObject payloadObject = convertedObject.getAsJsonObject("payload");
		SerialDataBusModel action = gson.fromJson(payloadObject, SerialDataBusModel.class);
		switch (action.getEventType().toString()) {
		case "FIND_ALL":
			// Loading AssetClass
			List<AssetClass> assetClassList = (List<AssetClass>) hashOperations.get(HASHREFERENCE,
					StaticDataKeys.ASSET_CLASS);
			insertIntoStaticData(StaticDataKeys.ASSET_CLASS, assetClassList);

			// Loading Bank
			List<InstitutionalEntity> bankList = (List<InstitutionalEntity>) hashOperations.get(HASHREFERENCE, StaticDataKeys.BANK);
			insertIntoStaticData(StaticDataKeys.BANK, bankList);

			// Loading Currency
			List<Currency> currencyList = (List<Currency>) hashOperations.get(HASHREFERENCE, StaticDataKeys.CURRENCY);
			insertIntoStaticData(StaticDataKeys.CURRENCY, currencyList);

			// Loading CurrencyPairDetails
			List<CurrencyPairDetails> currencyPairDetailsList = (List<CurrencyPairDetails>) hashOperations
					.get(HASHREFERENCE, StaticDataKeys.CURRENCY_PAIR);
			insertIntoStaticData(StaticDataKeys.CURRENCY_PAIR, currencyPairDetailsList);

			// Loading formattedCurrencyPairList
			List<String> formattedCurrencyPairList = (List<String>) hashOperations
					.get(HASHREFERENCE, StaticDataKeys.FORMATTED_CURRENCY_PAIR);
			insertIntoStaticData(StaticDataKeys.FORMATTED_CURRENCY_PAIR, formattedCurrencyPairList);

			// Loading CustomerSegment
			List<CustomerSegment> customerSegmentList = (List<CustomerSegment>) hashOperations.get(HASHREFERENCE,
					StaticDataKeys.CUSTOMER_SEGMENT);
			insertIntoStaticData(StaticDataKeys.CUSTOMER_SEGMENT, customerSegmentList);

			// Loading Direction
			List<Direction> directionList = (List<Direction>) hashOperations.get(HASHREFERENCE,
					StaticDataKeys.DIRECTION);
			insertIntoStaticData(StaticDataKeys.DIRECTION, directionList);

			// Loading EntityDetail
			List<GeographicalEntity> entityDetailList = (List<GeographicalEntity>) hashOperations.get(HASHREFERENCE,
					StaticDataKeys.ENTITY);
			insertIntoStaticData(StaticDataKeys.ENTITY, entityDetailList);

			// Loading Product
			List<Product> productList = (List<Product>) hashOperations.get(HASHREFERENCE, StaticDataKeys.PRODUCT);
			insertIntoStaticData(StaticDataKeys.PRODUCT, productList);

			// Loading ProductValidityMap
			List<ProductValidityMap> productValidityMapList = (List<ProductValidityMap>) hashOperations
					.get(HASHREFERENCE, StaticDataKeys.PRODUCT_VALIDITY);
			insertIntoStaticData(StaticDataKeys.PRODUCT_VALIDITY, productValidityMapList);

			// Loading RequestType
			List<RequestType> requestTypeList = (List<RequestType>) hashOperations.get(HASHREFERENCE,
					StaticDataKeys.REQUEST_TYPE);
			insertIntoStaticData(StaticDataKeys.REQUEST_TYPE, requestTypeList);

			// Loading RequestType
			List<Tenor> tenorList = (List<Tenor>) hashOperations.get(HASHREFERENCE, StaticDataKeys.TENOR);
			insertIntoStaticData(StaticDataKeys.TENOR, tenorList);

			// Loading TimerUnit
			List<TimerUnit> timerUnitList = (List<TimerUnit>) hashOperations.get(HASHREFERENCE,
					StaticDataKeys.TIMER_UNIT);
			insertIntoStaticData(StaticDataKeys.TIMER_UNIT, timerUnitList);

			// Loading UserRole
			List<UserRole> userRoleList = (List<UserRole>) hashOperations.get(HASHREFERENCE, StaticDataKeys.USER_ROLE);
			insertIntoStaticData(StaticDataKeys.USER_ROLE, userRoleList);

			log.info(STATICDATA.toString());
			kafkaProducerService.sendOnKafka(STATICDATA, event.getCorrelationId(), null);
			break;
		case "FIND_BY_ID":
			refreshcache();
			kafkaProducerService.sendOnKafka("Refreshed Cache", event.getCorrelationId(), null);
			break;
		default:
			log.info("Could not fetch static data due to invalid action type");
			kafkaProducerService.sendOnKafka("Invalid Action Type in parameter while fetching static data",
					event.getCorrelationId(), null);
			break;
		}

	}

	protected void insertIntoStaticData(StaticDataKeys StaticDataKeys, Object staticdata) {
		STATICDATA.put(StaticDataKeys, staticdata);
	}

	/**
	 * First Step: To put data in redis cache Second step: To get data from the
	 * database for the first time
	 */

	public void getAssetClasses() {
		log.info("caching AssetClasses");
		List<AssetClass> assetClassList = assetClassRepository.findAll();
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.ASSET_CLASS, assetClassList);
	}

	@Override
	public void getInstitutionalEntitiesAsBanks() {
		log.info("caching Bank");
		EntityRole instEntityAsBank = institutionalEntityRoleRepository.findByName("BANK");
		List<InstitutionalEntity> bankList = Collections.emptyList();
				//institutionalEntityRepository.findByEntityRole(instEntityAsBank);
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.BANK, bankList);
	}

	@Override
	public void getCurrencies() {
		log.info("caching AssetClasses");
		List<Currency> currencyList = currencyRepository.findAll();
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.CURRENCY, currencyList);
	}

	@Override
	public void getCurrencyPairDetails() {
		log.info("caching CurrencyPairDetails");
		List<CurrencyPairDetails> currencyPairDetailsList = currencyPairDetailsRepository.findAll();
		getFormattedCurrencyPairList(currencyPairDetailsList);
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.CURRENCY_PAIR, currencyPairDetailsList);
	}
	
	public void getFormattedCurrencyPairList(List<CurrencyPairDetails> currencyPairDetailsList) {
		List<String> formattedCurrencyPairs = new ArrayList<>();
		for (CurrencyPairDetails currencyPairDetails : currencyPairDetailsList) {
			formattedCurrencyPairs.add(formatCurrencyPair(currencyPairDetails.getBaseCurrency().getIsoCode(),
					currencyPairDetails.getQuoteCurrency().getIsoCode()));
		}
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.FORMATTED_CURRENCY_PAIR, formattedCurrencyPairs);
	}

	private String formatCurrencyPair(String baseCurrency, String quoteCurrency) {
		return baseCurrency.trim() + "/" + quoteCurrency.trim();
	}

	@Override
	public void getCustomerSegments() {
		log.info("caching CustomerSegment");
		List<CustomerSegment> customerSegmentList = customerSegmentRepository.findAll();
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.CUSTOMER_SEGMENT, customerSegmentList);
	}

	@Override
	public void getDirections() {
		log.info("caching Direction");
		List<Direction> directionList = directionRepository.findAll();
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.DIRECTION, directionList);
	}

	@Override
	public void getEntityDetails() {
		log.info("caching EntityDetail");
		List<GeographicalEntity> entityDetailList = entityDetailsRepository.findAll();
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.ENTITY, entityDetailList);
	}

	@Override
	public void getProducts() {
		log.info("caching Product");
		List<Product> productList = productRepository.findAll();
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.PRODUCT, productList);
	}

	@Override
	public void getProductValidityMap() {
		log.info("caching ProductValidityMap");
		List<ProductValidityMap> productValidityMapList = productValidityMapRepository.findAll();
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.PRODUCT_VALIDITY, productValidityMapList);
	}

	@Override
	public void getRequestTypes() {
		log.info("caching RequestType");
		List<RequestType> requestTypeList = requestTypeRepository.findAll();
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.REQUEST_TYPE, requestTypeList);
	}

	@Override
	public void getTenors() {
		log.info("caching Tenor");
		List<Tenor> tenorList = tenorRepository.findAll();
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.TENOR, tenorList);
	}

	@Override
	public void getTimerUnits() {
		log.info("caching TimerUnit");
		List<TimerUnit> timerUnitList = timerUnitRepository.findAll();
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.TIMER_UNIT, timerUnitList);
	}

	@Override
	public void getUserRoles() {
		log.info("caching UserRoles");
		List<UserRole> userRoleList = userRoleRepository.findAll();
		hashOperations.putIfAbsent(HASHREFERENCE, StaticDataKeys.USER_ROLE, userRoleList);
	}

	@Override
	public void refreshcache() {

		hashOperations.delete(HASHREFERENCE, StaticDataKeys.ASSET_CLASS);
		hashOperations.delete(HASHREFERENCE, StaticDataKeys.BANK);
		hashOperations.delete(HASHREFERENCE, StaticDataKeys.CURRENCY);
		hashOperations.delete(HASHREFERENCE, StaticDataKeys.CURRENCY_PAIR);
		hashOperations.delete(HASHREFERENCE, StaticDataKeys.CUSTOMER_SEGMENT);
		hashOperations.delete(HASHREFERENCE, StaticDataKeys.DIRECTION);
		hashOperations.delete(HASHREFERENCE, StaticDataKeys.ENTITY);
		hashOperations.delete(HASHREFERENCE, StaticDataKeys.PRODUCT);
		hashOperations.delete(HASHREFERENCE, StaticDataKeys.PRODUCT_VALIDITY);
		hashOperations.delete(HASHREFERENCE, StaticDataKeys.REQUEST_TYPE);
		hashOperations.delete(HASHREFERENCE, StaticDataKeys.TENOR);
		hashOperations.delete(HASHREFERENCE, StaticDataKeys.TIMER_UNIT);
		hashOperations.delete(HASHREFERENCE, StaticDataKeys.USER_ROLE);

		getAssetClasses();
		getInstitutionalEntitiesAsBanks();
		getCurrencies();
		getCurrencyPairDetails();
		getCustomerSegments();
		getDirections();
		getEntityDetails();
		getProducts();
		getProductValidityMap();
		getRequestTypes();
		getTenors();
		getTimerUnits();
		getUserRoles();
	}

}
