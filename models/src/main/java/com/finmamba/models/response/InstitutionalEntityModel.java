package com.finmamba.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Swanand Model to contain details of the institutional entity the user
 *         belongs to basis details in {@link UserProfile.class}
 *
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstitutionalEntityModel {
	private Long id;
	private String longName;
	private String shortName;
	private String realmId;
	private String realmName;
	private String groupId;
	private String groupName;
	private String clientId;
	private String clientName;
	/*
	 * Application user id linked to the id provided by the IDP
	 */
	private String appUserId;

}
