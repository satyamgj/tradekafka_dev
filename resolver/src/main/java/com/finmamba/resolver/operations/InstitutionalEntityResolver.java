package com.finmamba.resolver.operations;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.entities.transaction.SuperInstitutionalEntity;

@Service
public class InstitutionalEntityResolver {

	@Autowired
	KotakWorldOperations kotakWorldOperations;

	@Autowired
	IciciWorldOperations iciciWorldOperations;

	@Autowired
	KanjiWorldOperations kanjiWorldOperations;
	
	@Autowired
	AxisWorldOperations axisWorldOperations;

	@Autowired
	SanjayForexWorldOperations sanjayForexWorldOperations;

	private final Map<String, Supplier<SuperInstitutionalEntity>> entityMap = new HashMap<>();
	private final Map<String, Supplier<InstitutionalEntityOperations>> entityOperationsMap = new HashMap<>();

	/**
	 * @param realmName - This parameter value should match with key in the
	 *                  entityResolverMap
	 * @return object of institutional entity subclass
	 * 
	 * Not used at the moment
	 */
	public SuperInstitutionalEntity getInstitutionalEntityHandle(String realmName) {

		entityMap.put("kotakworld", () -> kotakWorldOperations.getInstitutionalEntityInstance());
		entityMap.put("iciciworld", () -> iciciWorldOperations.getInstitutionalEntityInstance());
		entityMap.put("kanjiworld", () -> kanjiWorldOperations.getInstitutionalEntityInstance());
		entityMap.put("sanjayforexworld", () -> sanjayForexWorldOperations.getInstitutionalEntityInstance());
		entityMap.put("axisworld", () -> axisWorldOperations.getInstitutionalEntityInstance());
		
		return entityMap.get(realmName.toLowerCase()).get();
	}

	public InstitutionalEntityOperations getInstitutionalEntityOperationsHandle(String realmName) {
		entityOperationsMap.put("kotakworld", () -> kotakWorldOperations.getInstitutionalEntityOperationsInstance());
		entityOperationsMap.put("iciciworld", () -> iciciWorldOperations.getInstitutionalEntityOperationsInstance());
		entityOperationsMap.put("kanjiworld", () -> kanjiWorldOperations.getInstitutionalEntityOperationsInstance());
		entityOperationsMap.put("sanjayforexworld",
				() -> sanjayForexWorldOperations.getInstitutionalEntityOperationsInstance());
		entityOperationsMap.put("axisworld",
				() -> axisWorldOperations.getInstitutionalEntityOperationsInstance());

		return entityOperationsMap.get(realmName.toLowerCase()).get();
	}

}
