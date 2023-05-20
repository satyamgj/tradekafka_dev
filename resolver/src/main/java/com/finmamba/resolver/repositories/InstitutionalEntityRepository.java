package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.InstitutionalEntity;

@Repository
public interface InstitutionalEntityRepository extends JpaRepository<InstitutionalEntity, Long> {

	public InstitutionalEntity findByRealmNameAndClientRoleNameAndClientName(String realm, String clientRoleName,
			String clientName);

	public InstitutionalEntity findByRealmNameAndClientRoleNameAndClientNameAndGroupName(String realm,
			String clientRoleName, String clientName, String groupName);
	
	public InstitutionalEntity findByRealmName(String realm);
}
