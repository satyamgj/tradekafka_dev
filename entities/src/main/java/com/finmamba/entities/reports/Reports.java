package com.finmamba.entities.reports;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(schema = "transaction")
@Builder
public class Reports implements Serializable {

	/**
	 * This will fetch the following values from the table
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "trn_id")
	private Integer transactionId;

	@Column(name = "broker_name")
	private String brokerName;

	@Column(name = "source_name")
	private String sourceName;

	@Column(name = "counterparty_transaction_id")
	private Integer counterpartyTransactionId;

	@Column(name = "name")
	private String name;

	@Column(name = "created_at")
	private Date createdAt;

	@Column(name = "spread")
	private Double spread;

	@Column(name = "direction")
	private String direction;

	@Column(name = "brokerage_ccy")
	private String brokerageCcy;

	@Column(name = "amount")
	private Double amount;

	@Column(name = "brokerage_base_ccy")
	private String brokerageBaseCcy;

	@Column(name = "base_amount")
	private Double baseAmount;

	@Column(name = "commission")
	private Double commission;

	@Column(name = "conversion_rate")
	private Double conversionRate;

	@Column(name = "bank_buy_ccy_id")
	private String bankBuyCcyId;

	@Column(name = "bank_sell_ccy_id")
	private String bankSellCcyId;

	@Column(name = "client_outright_cr")
	private Double clientOutright;

	@Column(name = "broker_commission_mode_ispip_is")
	private Boolean brokerCommissionModeIsPip;

	@Column(name = "instrument_type")
	private String instrumentType;

}
