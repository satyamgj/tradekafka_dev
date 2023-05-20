package com.finmamba.entities.transaction;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

import com.finmamba.entities.dblistener.IciciWorldTableListener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EntityListeners(IciciWorldTableListener.class)
@Setter
@Getter
@AllArgsConstructor
@ToString
@Entity
@Table(name = "iciciworld", schema = "transaction")
@SuperBuilder
public class IciciWorld extends SuperInstitutionalEntity {/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}
