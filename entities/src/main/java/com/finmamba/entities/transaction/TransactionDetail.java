package com.finmamba.entities.transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

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
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.staticdata.Product;
import com.finmamba.entities.staticdata.Tenor;

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
@Table(name = "transaction_details", schema = "transaction")
@Builder
public class TransactionDetail implements Serializable, Cloneable {
	/** 
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	
	@OneToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;


	@OneToOne
	@JoinColumn(name = "direction_id", nullable = false)
	private Direction buySellDirection;

	@OneToOne
	@JoinColumn(name = "bank_buy_ccy_id", nullable = false)
	private Currency baseCurrency;

	@OneToOne
	@JoinColumn(name = "bank_sell_ccy_id", nullable = false)
	private Currency quoteCurrency;

	@Column(name = "bank_buy_amount", nullable = true)
	private BigDecimal buyAmount;

	@Column(name = "bank_sell_amount", nullable = true)
	private BigDecimal sellAmount;

	
	@Column(name = "bank_outright", nullable = true)
	private BigDecimal sourceOutright;
	
	
	@Column(name = "spread_outright", nullable = true)
	private BigDecimal spreadOutright;
	
	
	@Column(name = "client_outright", nullable = true)
	private BigDecimal destinationOutright;

	@OneToOne
	@JoinColumn(name = "tenor_unit_id", nullable = false)
	private Tenor tenorUnit;


	@OneToOne
	@JoinColumn(name = "source_id", nullable = false)
	private InstitutionalEntity source;

	@Column(name = "is_confirmed", nullable = true)
	private boolean isConfirmed;
	
	@Column(name = "transaction_id", nullable = false)
	private String transactionId;
	
	@OneToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	
//	@PostLoad
//	private void postLoad() {
//		if (this.quoteValidityCalculated != null) {
//			this.timeToLive = (int) LocalDateTime.now().until(this.quoteValidityCalculated.toLocalDateTime(),
//					ChronoUnit.SECONDS);
//		}
//
//	}
	
	@Column(name="is_partial")
	private Boolean isPartial;
	
	@OneToOne
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;
	
	@Column(name = "created_at", nullable = false, insertable = false)
	private Instant createdAt;
}
