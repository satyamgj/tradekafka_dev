package com.finmamba.entities.transaction;

import java.io.Serializable;

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
@Table(name = "entity_communication_map", schema = "transaction")

public class InstitutionalEntityAssociation implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
	
	@OneToOne
	@JoinColumn(name = "source_entity_id")
	private InstitutionalEntity sourceEntity;
	
	@OneToOne
	@JoinColumn(name = "destination_entity_id")
	private InstitutionalEntity destinationEntity;
	
	@Column(name = "willing_to_buy")
	private boolean willingToBuy;
	
	@Column(name = "willing_to_sell")
	private boolean willingToSell;
}
