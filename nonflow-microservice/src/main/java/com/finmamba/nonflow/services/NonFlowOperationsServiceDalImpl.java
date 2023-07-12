package com.finmamba.nonflow.services;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.CustomerSegment;
import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.staticdata.Product;
import com.finmamba.entities.staticdata.Tenor;
import com.finmamba.entities.transaction.Customer;
import com.finmamba.entities.transaction.NonFlow;
import com.finmamba.entities.transaction.User;
import com.finmamba.models.dto.NonFlowDto;
import com.finmamba.models.dto.SavedNonFlowDto;
import com.finmamba.nonflow.NonFlowRequest;
import com.finmamba.nonflow.exception.CustomNonFlowException;
import com.finmamba.nonflow.repositories.CurrencyRepository;
import com.finmamba.nonflow.repositories.CustomerRepository;
import com.finmamba.nonflow.repositories.CustomerSegmentRepository;
import com.finmamba.nonflow.repositories.DirectionRepository;
import com.finmamba.nonflow.repositories.InstitutionalEntityRepository;
import com.finmamba.nonflow.repositories.NonFlowRepository;
import com.finmamba.nonflow.repositories.ProductRepository;
import com.finmamba.nonflow.repositories.TenorRepository;
import com.finmamba.nonflow.repositories.UserRepository;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class NonFlowOperationsServiceDalImpl implements NonFlowOperationsServiceDal {

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	NonFlowRepository nonFlowRepository;
	
	@Autowired
	CurrencyRepository currencyRepository;
	
	@Autowired
	CustomerSegmentRepository customerSegmentRepository;

	@Autowired
	CustomerRepository customerRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	InstitutionalEntityRepository institutionalEntityRepository;
	
	@Autowired
	ProductRepository productRepository;

	@Autowired
	DirectionRepository directionRepository;

	@Autowired
	TenorRepository tenorRepository;


	@Override
	@Transactional
	public List<SavedNonFlowDto> saveNonFlowRequest(NonFlowDto nonFlowDto) throws CustomNonFlowException {

		// Product
				Product product = productRepository.findById(nonFlowDto.getProductId())
						.orElseThrow(() -> new CustomNonFlowException("product  not found"));
				// Get base and quote currency
				Currency baseCurrency = currencyRepository.findById(nonFlowDto.getBaseCurrencyId())
						.orElseThrow(() -> new CustomNonFlowException("baseCurrency  not found"));
				Currency quoteCurrency = currencyRepository.findById(nonFlowDto.getQuoteCurrency())
						.orElseThrow(() -> new CustomNonFlowException("quoteCurrency  not found"));

				Currency perBaseBuyCurrency = currencyRepository.findById(nonFlowDto.getPerBaseBuyCurrency())
						.orElseThrow(() -> new CustomNonFlowException("perBaseBuyCurrency  not found"));

				Currency perBaseSellCurrency = currencyRepository.findById(nonFlowDto.getPerBaseSellCurrency())
						.orElseThrow(() -> new CustomNonFlowException("perBaseSellCurrency  not found"));

				Currency buySideCurrencyId = currencyRepository.findById(nonFlowDto.getBuySideCurrencyId())
						.orElseThrow(() -> new CustomNonFlowException("buySideCurrencyId  not found"));

				Currency sellSideCurrencyId = currencyRepository.findById(nonFlowDto.getSellSideCurrencyId())
						.orElseThrow(() -> new CustomNonFlowException("sellSideCurrencyId  not found"));

				CustomerSegment customerSegment = customerSegmentRepository.findById(nonFlowDto.getCustomerSegmentId())
						.orElseThrow(() -> new CustomNonFlowException("customer segment  not found"));

				long customerId = nonFlowDto.getCustomerId();

				Customer customer = customerRepository.findById(customerId)
						.orElseThrow(() -> new CustomNonFlowException("customer  not found"));

				// Get Buy or Sell
				Direction direction = directionRepository.getById(nonFlowDto.getBuySellDirection());

				// Notional
				BigDecimal notional = BigDecimal.ZERO;
				BigDecimal bankBuyAmount = nonFlowDto.getBuyAmount();
				BigDecimal bankSellAmount = nonFlowDto.getSellAmount();
				if (direction.getName().equalsIgnoreCase("BUY")) {
					notional = bankBuyAmount;
				} else
					notional = bankSellAmount;
				
				// Tenor value
				long tenor = nonFlowDto.getTenorValue();

				// Tenor unit
				Tenor tenorUnit = tenorRepository.findById(nonFlowDto.getTenorUnitId())
						.orElseThrow(() -> new CustomNonFlowException("tenor unit  not found"));
				
				User userId = userRepository.getById(Long.valueOf(nonFlowDto.getUserId()));
				InstitutionalEntity selfInstitutionalEntity = institutionalEntityRepository.findById(nonFlowDto.getInstitutionalId())
						.orElseThrow(() -> new CustomNonFlowException("institutional entity not found"));
				
				InstitutionalEntity sourceInstitutionalEntity = institutionalEntityRepository.findById(nonFlowDto.getInstitutionalSourceEntityId())
						.orElseThrow(() -> new CustomNonFlowException("source institutional entity not found"));
				InstitutionalEntity destinationInstitutionalEntity = institutionalEntityRepository.findById(nonFlowDto.getInstitutionalDestinationEntityId())
						.orElseThrow(() -> new CustomNonFlowException("destination institutional entity not found"));

		
		NonFlow buildRequest = NonFlow.builder().baseCurrency(baseCurrency)
				.buySellDirection(direction)
				.userId(nonFlowDto.getUserId())
				.customer(customer)
				.customerSegmentId(customerSegment)
				.uiRequestTimestamp(nonFlowDto.getUiRequestTimeStamp())
				.institutionalSourcEntityId(sourceInstitutionalEntity)
				.institutionalSourceEntityName(sourceInstitutionalEntity.getLongName())
				.institutionalDestinationEntityId(destinationInstitutionalEntity)
				.institutionalDestinationEntityName(destinationInstitutionalEntity.getLongName())
				.outright(nonFlowDto.getOutright())
				.product(product)
				.baseCurrency(baseCurrency)
				.quoteCurrency(quoteCurrency)
				.buyAmount(bankBuyAmount)
				.sellAmount(bankSellAmount)
				.spotRate(nonFlowDto.getSpotRate())
				.tenorUnit(tenorUnit)
				.tenorValue(tenor)
				.userId(nonFlowDto.getUserId())
				.isPipOrAbsolute(nonFlowDto.isPipOrAbsolute())
				.buySideAbsoluteCommision(nonFlowDto.getBuySideAbsoluteCommision())
				.sellSideAbsoluteCommision(nonFlowDto.getSellSideAbsoluteCommision())
				.perBaseBuyCurrency(perBaseBuyCurrency)
				.perBaseSellCurrency(perBaseSellCurrency)
				.perBaseBuyCurrencyAmount(nonFlowDto.getPerBaseBuyCurrencyAmount())
				.perBaseSellCurrencyAmount(nonFlowDto.getPerBaseSellCurrencyAmount())
				.buySideCurrencyId(buySideCurrencyId)
				.sellSideCurrencyId(sellSideCurrencyId)
				.buySideCommisionRate(nonFlowDto.getBuySideCommisionRate())
				.sellSideCommisionRate(nonFlowDto.getBuySideCommisionRate())
				.institutionalId(selfInstitutionalEntity)
				.dbRequestTimestamp(new Date(System.currentTimeMillis()))
				.uiRequestTimestamp(new Date(System.currentTimeMillis()))
				.build();

		NonFlow savedRequest = nonFlowRepository.save(buildRequest);
		
		List<SavedNonFlowDto> listNonFlowRequest = new ArrayList<>();
		SavedNonFlowDto returnNonFlowDto = modelMapper.map(savedRequest, SavedNonFlowDto.class);
		listNonFlowRequest.add(returnNonFlowDto);

		return listNonFlowRequest;

	}
	
	@Override
	@Transactional
	public List<NonFlow> getNonFlowRequest(NonFlowRequest nonFlowRequest) {
		log.info("Inside get Non Flow Dal Implementation");
		List<NonFlowDto> returnList = null;
		int pageSize = nonFlowRequest.getPageSize().intValue();
		int offSet = nonFlowRequest.getOffset().intValue();
		String sortField = nonFlowRequest.getSortField();
		BigInteger institutionId = nonFlowRequest.getEntityId();
		String startDate = nonFlowRequest.getStartDate();
		String endDate = nonFlowRequest.getEndDate();
		
		// Make Pageable request as per the sort order
		Pageable pageableRequest = null;
		if (nonFlowRequest.getSortOrder().equalsIgnoreCase("asc")) {
			pageableRequest = PageRequest.of(offSet, pageSize,
					Sort.Direction.ASC, sortField);
		} else {
			pageableRequest = PageRequest.of(offSet, pageSize,
					Sort.Direction.DESC, sortField);
		}

		Type listType = new TypeToken<List<NonFlowDto>>() {
		}.getType();
		
			List<NonFlow> nonFlowList = nonFlowRepository
					.getNonFlowRequest(institutionId, startDate, endDate, pageableRequest);
			log.info("return list of NonFlow requests" + nonFlowList);
			//returnList = modelMapper.map(nonFlowList, listType);
		
		return nonFlowList;
	}

}
