package com.finmamba.staticdatadal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.InstitutionalEntity;

@Repository
public interface InstitutionalEntityRepository extends JpaRepository<InstitutionalEntity, Long>{
	//List<InstitutionalEntity> findByEntityRole(EntityRole entityRole);
}
