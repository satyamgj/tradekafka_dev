package com.tradekafka.ondc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.Direction;

@Repository
public interface DirectionRepository extends JpaRepository<Direction, Long>{

}
