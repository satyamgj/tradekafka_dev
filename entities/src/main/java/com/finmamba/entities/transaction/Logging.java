package com.finmamba.entities.transaction;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(
		name = "logging",
		schema = "transaction"
		)
public class Logging {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User userId;
	
	@OneToOne
	@JoinColumn(name = "realm_id", referencedColumnName = "id")
	private InstitutionalEntity realmId;

//	@OneToOne
//	@JoinColumn(name = "transaction_id", referencedColumnName = "id")
//	private KanjiWorld transactionId;
	

	@OneToOne
	@JoinColumn(name = "source_id", referencedColumnName = "id")
	private InstitutionalEntity sourceId;
	
	@OneToOne
	@JoinColumn(name = "destination_id", referencedColumnName = "id")
	private InstitutionalEntity destinationId;
	
	@OneToOne
	@JoinColumn(name = "action_id", referencedColumnName = "id")
	private EventType action;
	
	@Column(name="notification_string")
	private String notificationMessage;
	
	@Column(name = "daddy_transaction_id")
	private String daddyTransactionId;
	
	@Column(name="created_at")
	private Timestamp createdAt;
	
}
