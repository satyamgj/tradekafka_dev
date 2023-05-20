package com.finmamba.entities.transaction;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.finmamba.entities.staticdata.InstitutionalEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(
		schema = "transaction",
		name = "confirmed_order_details"
		)
public class ConfirmedOrderDetails implements Serializable {
private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	
	@OneToOne
	@JoinColumn(name = "broker_id", nullable = false)
	private InstitutionalEntity brokerId;
	
	
	@OneToOne
	@JoinColumn(name = "buy_side_counterparty_transaction_id", nullable = false)
	private CounterpartyTransactionDetails buySideCounterpartyTransactionId;

	@OneToOne
	@JoinColumn(name = "sell_side_counterparty_transaction_id", nullable = false)
	private CounterpartyTransactionDetails sellSideCounterpartyTransactionId;
	
		
	@OneToOne
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;
	
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	
	
}
