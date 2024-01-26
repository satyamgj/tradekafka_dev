package com.finmamba.entities.ondc;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
@Table(name = "confirmed_transactions", schema = "ondc")
@Builder
public class ConfirmedTransactions implements Serializable{
	/**
	 * This will fetch the following values from the table
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@OneToOne
	@JoinColumn(name="buy_txn_id", referencedColumnName = "id")
	private QuoteRequest buyTxnId;
	
	@OneToOne
	@JoinColumn(name="sell_txn_id", referencedColumnName = "id")
	private QuoteRequest sellTxnId;
	
	@Column(name="rate")
	private BigDecimal rate;
}
