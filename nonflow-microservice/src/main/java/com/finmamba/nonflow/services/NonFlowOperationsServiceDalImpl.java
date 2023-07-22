package com.finmamba.nonflow.services;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.finmamba.entities.reports.NonFlowReport;
import com.finmamba.entities.transaction.NonFlow;
import com.finmamba.models.reports.NonFlowReportDto;
import com.finmamba.models.reports.SaveNonFlowDto;
import com.finmamba.models.reports.SearchNonFlowDto;
//import com.finmamba.nonflow.NonFlowRequest;
import com.finmamba.nonflow.exception.CustomNonFlowException;
import com.finmamba.nonflow.repositories.CurrencyRepository;
import com.finmamba.nonflow.repositories.CustomerRepository;
import com.finmamba.nonflow.repositories.CustomerSegmentRepository;
import com.finmamba.nonflow.repositories.DirectionRepository;
import com.finmamba.nonflow.repositories.InstitutionalEntityRepository;
import com.finmamba.nonflow.repositories.NonFlowRepository;
import com.finmamba.nonflow.repositories.NonFlowRepositoryRetrieve;
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
	NonFlowRepositoryRetrieve nonFlowRepositoryRetrieve;

	
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
	public List<SaveNonFlowDto> saveNonFlowRequest(SaveNonFlowDto saveNonFlowDto) throws CustomNonFlowException {

		// Product
		/*
		 * Integer product = productRepository.findById(nonFlowDto.getProductId())
		 * .orElseThrow(() -> new CustomNonFlowException("product  not found")); // Get
		 * base and quote currency Currency baseCurrency =
		 * currencyRepository.findById(Long.valueOf(nonFlowDto.getBankBuyCcyId()))
		 * .orElseThrow(() -> new CustomNonFlowException("baseCurrency  not found"));
		 * Currency quoteCurrency =
		 * currencyRepository.findById(Long.valueOf(nonFlowDto.getBankSellCcyId()))
		 * .orElseThrow(() -> new CustomNonFlowException("quoteCurrency  not found"));
		 * 
		 * Currency perBaseBuyCurrency =
		 * currencyRepository.findById(Long.valueOf(nonFlowDto.
		 * getBuySideBrokerageBaseCcy())) .orElseThrow(() -> new
		 * CustomNonFlowException("perBaseBuyCurrency  not found"));
		 * 
		 * Currency perBaseSellCurrency =
		 * currencyRepository.findById(Long.valueOf(nonFlowDto.
		 * getSellSideBrokerageBaseCcy())) .orElseThrow(() -> new
		 * CustomNonFlowException("perBaseSellCurrency  not found"));
		 * 
		 * Currency buySideCurrencyId =
		 * currencyRepository.findById(Long.valueOf(nonFlowDto.getBuySideCurrencyId()))
		 * .orElseThrow(() -> new
		 * CustomNonFlowException("buySideCurrencyId  not found"));
		 * 
		 * Currency sellSideCurrencyId =
		 * currencyRepository.findById(nonFlowDto.getSellSideCurrencyId())
		 * .orElseThrow(() -> new
		 * CustomNonFlowException("sellSideCurrencyId  not found"));
		 * 
		 * CustomerSegment customerSegment =
		 * customerSegmentRepository.findById(nonFlowDto.getCustomerSegmentId())
		 * .orElseThrow(() -> new
		 * CustomNonFlowException("customer segment  not found"));
		 * 
		 * long customerId = nonFlowDto.getCustomerId();
		 * 
		 * Customer customer = customerRepository.findById(customerId) .orElseThrow(()
		 * -> new CustomNonFlowException("customer  not found"));
		 * 
		 * // Get Buy or Sell Direction direction =
		 * directionRepository.getById(nonFlowDto.getBuySellDirection());
		 * 
		 * // Notional Double notional = 0; Double bankBuyAmount =
		 * nonFlowDto.getBankBuyAmount(); Double bankSellAmount =
		 * nonFlowDto.getBankSellAmount(); if
		 * (direction.getName().equalsIgnoreCase("BUY")) { notional = bankBuyAmount; }
		 * else notional = bankSellAmount;
		 * 
		 * // Tenor value long tenor = nonFlowDto.getTenorValue();
		 * 
		 * // Tenor unit Tenor tenorUnit =
		 * tenorRepository.findById(nonFlowDto.getTenorUnitId()) .orElseThrow(() -> new
		 * CustomNonFlowException("tenor unit  not found"));
		 * 
		 * User userId = userRepository.getById(Long.valueOf(nonFlowDto.getUserId()));
		 * InstitutionalEntity selfInstitutionalEntity =
		 * institutionalEntityRepository.findById(nonFlowDto.getInstitutionalId())
		 * .orElseThrow(() -> new
		 * CustomNonFlowException("institutional entity not found"));
		 * 
		 * InstitutionalEntity sourceInstitutionalEntity =
		 * institutionalEntityRepository.findById(nonFlowDto.
		 * getInstitutionalSourceEntityId()) .orElseThrow(() -> new
		 * CustomNonFlowException("source institutional entity not found"));
		 * InstitutionalEntity destinationInstitutionalEntity =
		 * institutionalEntityRepository.findById(nonFlowDto.
		 * getInstitutionalDestinationEntityId()) .orElseThrow(() -> new
		 * CustomNonFlowException("destination institutional entity not found"));
		 * 
		 */
		NonFlow buildRequest = NonFlow.builder().bankBuyAmount(saveNonFlowDto.getBankBuyAmount())
				.bankSellAmount(saveNonFlowDto.getBankSellAmount())
				.brokerCommissionModeIsPip(saveNonFlowDto.getBrokerCommissionModeIspip())
				.buySideBaseAmount(saveNonFlowDto.getBuySideBaseAmount())
				.buySideBrokerageBaseCcy(saveNonFlowDto.getBuySideBrokerageBaseCcy())
				.buySideBrokerageCcy(saveNonFlowDto.getBuySideBrokerageCcy())
				.buySideConversionRate(saveNonFlowDto.getBuySideConversionRate())
				.buySideId(saveNonFlowDto.getBuySideId()).buySideSpread(saveNonFlowDto.getBuySideSpread())
				.calculatedBuyCommission(saveNonFlowDto.getCalculatedBuyCommission())
				.calculatedSellCommission(saveNonFlowDto.getCalculatedSellCommission())
				.clientOutright(saveNonFlowDto.getClientOutright()).createdAt(saveNonFlowDto.getCreatedAt())
				.createdBy(saveNonFlowDto.getCreatedBy()).direction(saveNonFlowDto.getDirection())
				.entityId(saveNonFlowDto.getEntityId()).product(saveNonFlowDto.getProductId())
				.sellSideBrokerageBaseCcy(saveNonFlowDto.getSellSideBrokerageBaseCcy())
				.sellSideBrokerageCcy(saveNonFlowDto.getBuySideBrokerageCcy())
				.sellSideConversionRate(saveNonFlowDto.getSellSideConversionRate())
				.sellSideId(saveNonFlowDto.getSellSideId()).sellSideSpread(saveNonFlowDto.getSellSideSpread())
				.selSideBaseAmount(saveNonFlowDto.getSellSideBaseAmount()).build();

		NonFlow savedRequest = nonFlowRepository.save(buildRequest);

		List<SaveNonFlowDto> listNonFlowRequest = new ArrayList<>();
		SaveNonFlowDto returnNonFlowDto = modelMapper.map(savedRequest, SaveNonFlowDto.class);
		listNonFlowRequest.add(returnNonFlowDto);

		return listNonFlowRequest;

	}

	@Override
	@Transactional
	public List<NonFlowReportDto> getNonFlowRequest(SearchNonFlowDto nonFlowRequest) {
		log.info("Inside get Non Flow Dal Implementation");
		List<NonFlowReportDto> returnList = null;
		String startDate = nonFlowRequest.getStartDate();
		String endDate = nonFlowRequest.getEndDate();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date utilStartDate = null;
		java.util.Date utilEndDate = null;
		java.sql.Date sqlStartDate = null;
		java.sql.Date sqlEndDate = null;
		try {
			utilStartDate = sdf1.parse(startDate);
			utilEndDate = sdf1.parse(endDate);
			sqlStartDate = new java.sql.Date(utilStartDate.getTime());
			sqlEndDate = new java.sql.Date(utilEndDate.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int pageSize = nonFlowRequest.getPageSize().intValue();
		int offSet = nonFlowRequest.getOffset().intValue();
		String sortField = nonFlowRequest.getSortField();
		BigInteger entityId = nonFlowRequest.getEntityId();
		
		// Make Pageable request as per the sort order
		Pageable pageableRequest = null;
		if (nonFlowRequest.getSortOrder().equalsIgnoreCase("asc")) {
			pageableRequest = PageRequest.of(offSet, pageSize, Sort.Direction.ASC, sortField);
		} else {
			pageableRequest = PageRequest.of(offSet, pageSize, Sort.Direction.DESC, sortField);
		}

		Type listType = new TypeToken<List<NonFlowReportDto>>() {
		}.getType();

		List<NonFlowReport> nonFlowList = nonFlowRepositoryRetrieve.findByEntityIdAndCreateAtBetween(entityId.intValue(), sqlStartDate, sqlEndDate,
				pageableRequest);
		log.info("return list of NonFlow requests" + nonFlowList);  
		returnList = modelMapper.map(nonFlowList, listType);

		return returnList;
	}

}
