package com.finmamba.resolver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.ConfirmedOrderDetails;

@Repository
public interface ConfirmationRepostory extends JpaRepository<ConfirmedOrderDetails, Long>{

	List<ConfirmedOrderDetails> findAllBybuySideId(InstitutionalEntity buySide);
	
	List<ConfirmedOrderDetails> findAllBysellSideId(InstitutionalEntity sellSide);
	
	List<ConfirmedOrderDetails> findAllBybrokerId(InstitutionalEntity brokerId);


	
	
}
