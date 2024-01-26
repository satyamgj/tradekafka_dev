package com.finmamba.entities.ondc;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
@Table(name = "request_orders", schema = "ondc")
@Builder
public class RequestedOrders implements Serializable{
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
	private User userId;
	
	@OneToOne
	@JoinColumn(name = "quote_request_id", referencedColumnName = "id")
	private QuoteRequest quoteRequestId;
}
