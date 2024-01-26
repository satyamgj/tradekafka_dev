package com.finmamba.entities.ondc;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.transaction.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "quote_requests", schema = "ondc")
@Builder
public class QuoteRequest implements Serializable{

	/**
	 * This will fetch the following values from the table
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@OneToOne
	@JoinColumn(name="buy_user_id", referencedColumnName = "id")
	private User buyUserId;
	
	@OneToOne
	@JoinColumn(name="direction_id", referencedColumnName = "id" )
	private Direction directionId;
	
	@OneToOne
	@JoinColumn(name = "bank_buy_ccy_id", nullable = false)
	private Currency baseCurrency;

	@OneToOne
	@JoinColumn(name = "bank_sell_ccy_id", nullable = false)
	private Currency quoteCurrency;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;
	
	@Column(name = "is_confirmed", nullable = false)
	private Boolean isConfirmed;
	
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;
	
	@Column(name = "is_matched")
	private Boolean isMatched = false;
		
	@Column(name= "payable_amount")
	private BigDecimal payableAmount;
	
	@Column(name="responseYN")
	private String responseYN;
	
	@Column(name="asking_rate")
	private BigDecimal askingRate;
	
	@Column(name="inserted_at", insertable = false)
	private Timestamp insertedAt;
	
	
	
}
