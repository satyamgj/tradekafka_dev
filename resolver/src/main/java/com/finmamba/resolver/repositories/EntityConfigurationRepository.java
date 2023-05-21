package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.configuration.EntityConfigurationSettings;

@Repository
public interface EntityConfigurationRepository extends JpaRepository<EntityConfigurationSettings, Long>{

	EntityConfigurationSettings findByEntityId(Long entityId);
	
}

