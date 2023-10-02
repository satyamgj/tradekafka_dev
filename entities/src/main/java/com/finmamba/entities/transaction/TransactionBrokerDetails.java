package com.finmamba.entities.transaction;

import java.io.Serializable;
import java.sql.Timestamp;
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
@Table(name = "transaction_broker_details", schema = "transaction")
@Builder
public class TransactionBrokerDetails implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transaction_id", nullable = false)
	private TransactionDetail transactionId;
	
	@OneToOne
	@JoinColumn(name="direction_id", nullable = false)
	private Direction directionId;
	
	@Column(name = "broker_id")
	private Long brokerId;
	
	@Column(name = "is_active")
	private boolean isActive;
	
	@Column(name = "last_transaction_at")
	private Instant lastTransactionAt;
	

}
