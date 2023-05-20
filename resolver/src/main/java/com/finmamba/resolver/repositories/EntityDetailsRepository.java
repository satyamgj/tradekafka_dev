package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.GeographicalEntity;

@Repository
public interface EntityDetailsRepository extends JpaRepository<GeographicalEntity, Long>{

}
