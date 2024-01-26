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

import com.finmamba.entities.staticdata.Currency;
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
@Table(name = "seller_catalogue", schema = "ondc")
@Builder
public class SellerCatalogue implements Serializable {
	/**
	 * This will fetch the following values from the table
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@OneToOne
	@JoinColumn(name="user_id", referencedColumnName = "id")
	private User userId;
	
	@OneToOne
	@JoinColumn(name = "bank_buy_ccy_id", nullable = false)
	private Currency baseCurrency;

	@OneToOne
	@JoinColumn(name = "bank_sell_ccy_id", nullable = false)
	private Currency quoteCurrency;

}
