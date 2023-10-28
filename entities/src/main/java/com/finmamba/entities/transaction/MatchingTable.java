package com.finmamba.entities.transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.finmamba.models.dto.CurrencyDto;
import com.finmamba.models.dto.DirectionDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(
		schema = "transaction",
		name = "matching_table"
		)
@Builder
public class MatchingTable implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name="order_id")
	private Long orderId;
	
	@Column(name="source_id")
	private Long sourceId;
	
	@Column(name="destination_id")
	private Long destinationId;
	
	@Column(name="buy_currency_id")
	private Long buyCurrencyId;
	
	@Column(name="sell_currency_id")
	private Long sellCurrencyId;
	
	@Column(name="is_partial")
	private Boolean isPartial;
	
	@Column(name="direction")
	private Long direction;
	
	@Column(name="asking_rate")
	private BigDecimal askingRate;
	
	@Column(name="buy_amount")
	private BigDecimal buyAmount;
	
	@Column(name="sell_amount")
	private BigDecimal sellAmount;
	
	@Column(name="insertion_time")
	private Timestamp insertionTime;

	@Column(name="event_type")
	private Long eventType;
	
	@Column(name="transaction_id")
	private Long transactionId;
	
	@Column(name="for_transaction_id")
	private Long forTransactionId;


}
