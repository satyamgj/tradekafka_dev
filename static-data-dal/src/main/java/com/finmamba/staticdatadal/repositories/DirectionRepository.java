package com.finmamba.staticdatadal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.Direction;

@Repository
public interface DirectionRepository extends JpaRepository<Direction, Long>{

}
