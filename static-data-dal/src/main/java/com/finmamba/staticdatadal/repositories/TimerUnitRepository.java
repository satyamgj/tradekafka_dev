package com.finmamba.staticdatadal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.TimerUnit;

@Repository
public interface TimerUnitRepository extends JpaRepository<TimerUnit, Long>{

}
