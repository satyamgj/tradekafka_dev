package com.finmamba.resolver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.InstitutionalEntityAssociation;

@Repository
public interface InstitutionalEntityAssociationRepository extends JpaRepository<InstitutionalEntityAssociation, Long> {
	public List<InstitutionalEntityAssociation> findDestinationEntityByWillingToBuyAndSourceEntityAndDestinationEntityNot(boolean willingToBuy,
			InstitutionalEntity sourceEntity, InstitutionalEntity destinationEntity);
	
	//for fetching broker mapping
	public List<InstitutionalEntityAssociation>findDestinationEntityByWillingToBuyAndSourceEntityAndDestinationEntityNotAndEntityType(boolean willingToBuy,
			InstitutionalEntity sourceEntity, InstitutionalEntity destinationEntity, long entityType);
	
	public List<InstitutionalEntityAssociation> findDestinationEntityByWillingToBuyAndSourceEntity(boolean willingToBuy,
			InstitutionalEntity sourceEntity);
	

}
