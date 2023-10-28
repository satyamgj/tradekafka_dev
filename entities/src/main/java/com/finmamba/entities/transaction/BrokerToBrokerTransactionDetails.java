package com.finmamba.entities.transaction;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.finmamba.entities.staticdata.Direction;

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
		name = "broker_to_broker_transaction_details"
		)
@Builder
public class BrokerToBrokerTransactionDetails implements Serializable{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cp_transaction_id", nullable = false)
	private CounterpartyTransactionDetails cpTransactionId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transaction_id", nullable = false)
	private TransactionDetail transactionId;
	
	@OneToOne
	@JoinColumn(name="direction_id", nullable = false)
	private Direction directionId;
	
	
	@Column(name="source_broker_id")
	private Long sourceBrokerId;
	
	@Column(name = "broker_id")
	private Long brokerId;
	
	@Column(name = "is_active")
	private boolean isActive;
	
	@Column(name="status_id")
	private Long statusId;
	
	@Column(name = "last_transaction_at")
	private Instant lastTransactionAt;
}
