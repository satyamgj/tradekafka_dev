package com.finmamba.staticdatadal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.GeographicalEntity;

@Repository
public interface GeographicalEntityRepository extends JpaRepository<GeographicalEntity, Long>{

}
