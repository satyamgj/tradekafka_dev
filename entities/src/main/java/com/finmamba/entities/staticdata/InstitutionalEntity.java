package com.finmamba.entities.staticdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "institutional_entities", schema = "static")
@Entity
public class InstitutionalEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "long_name")
	private String longName;

	@Column(name = "short_name")
	private String shortName;

	@Column(name = "realm_id")
	private String realmId;

	@Column(name = "realm_name")
	private String realmName;

	@Column(name = "group_id")
	private String groupId;

	@Column(name = "group_name")
	private String groupName;
	
	@Column(name = "client_id")
	private String clientId;

	@Column(name = "client_name")
	private String clientName;
	
	@Column(name = "client_role_name")
	private String clientRoleName;
	
	@Column(name = "client_role_id")
	private String clientRoleId;
	
	@OneToOne
	@JoinColumn(name = "entity_type")
	private EntityType entityType; 

}
