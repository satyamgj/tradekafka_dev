package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.RecordStatus;

@Repository
public interface RecordStatusRepository extends JpaRepository<RecordStatus, Long>{

}
