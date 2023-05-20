package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.EventType;

@Repository
public interface EventTypeRepository extends JpaRepository<EventType, Long>{

}
