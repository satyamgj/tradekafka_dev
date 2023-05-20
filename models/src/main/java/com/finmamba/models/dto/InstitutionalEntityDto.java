package com.finmamba.models.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionalEntityDto implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;
	
	private String longName;

	private String shortName;

	private String realmId;

	private String realmName;

	private String groupId;

	private String groupName;

	private String clientId;

	private String clientName;

	private String clientRoleName;

	private String clientRoleId;
}
