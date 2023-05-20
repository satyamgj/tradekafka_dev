package com.finmamba.resolver.operations;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.finmamba.entities.transaction.SuperInstitutionalEntity;

@Component
public class EntityResolverMap {

	@Autowired
	static KotakWorldOperations kotakWorldOperations;
	
	@Autowired
	static IciciWorldOperations iciciWorldOperations;
	
	//private static final Map<String, Supplier<SuperInstitutionalEntity>> entityMap = new HashMap<>();

	//private static final Map<String, Supplier<InstitutionalEntityOperations>> entityOperationsMap = new HashMap<>();

	private EntityResolverMap() {

	}

	public static Map<String, Supplier<SuperInstitutionalEntity>> getEntityMapInstance() {
		Map<String, Supplier<SuperInstitutionalEntity>> entityMap = new HashMap<>();
		entityMap.put("kotakworld", () -> kotakWorldOperations.getInstitutionalEntityInstance());
		return entityMap;
	}

	public static Map<String, Supplier<InstitutionalEntityOperations>> getEntityOperationMapInstance() {
		Map<String, Supplier<InstitutionalEntityOperations>> entityOperationsMap = new HashMap<>();
		entityOperationsMap.put("kotakworld", () -> kotakWorldOperations.getInstitutionalEntityOperationsInstance());
		return entityOperationsMap;
	}

	public static void intitalizeEntityResolverMaps() {
		// KotakWorld
//		entityMap.put("kotakworld", () -> kotakWorldOperations.getInstitutionalEntityInstance());
//		entityOperationsMap.put("kotakworld", () -> kotakWorldOperations.getInstitutionalEntityOperationsInstance());

		// IciciWorld
//		entityMap.put("iciciworld", () -> iciciWorldOperations.getInstitutionalEntityInstance());
//		entityOperationsMap.put("iciciworld", () -> iciciWorldOperations.getInstitutionalEntityOperationsInstance());
	}
}
