package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.EntityRole;

@Repository
public interface InstitutionalEntityRoleRepository extends JpaRepository<EntityRole, Long> {
	public EntityRole findByName(String name);
}
