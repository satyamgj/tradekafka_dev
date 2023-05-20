package com.finmamba.resolver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.RequestQuote;
import com.finmamba.entities.transaction.User;

@Repository
public interface RequestQuoteRepository extends JpaRepository<RequestQuote, Long> {
	public List<RequestQuote> findByUser(User user);

	public List<RequestQuote> findByDestination(InstitutionalEntity institutionalEntity);

	public List<RequestQuote> findByDestinationAndIsRecordProcessed(InstitutionalEntity institutionalEntity,
			boolean recordProcessedStatus);

	public List<RequestQuote> findBySource(InstitutionalEntity institutionalEntity);

	public List<RequestQuote> findByUserAndIsRequestActive(User user, Boolean isRequestActive);

	@Query(value = "select  EXTRACT(SECOND FROM (qr.quote_validity_calculated - now())) as quote_valid_till,* from transaction.quote_request qr "
			+ "where qr.request_is_active = :isRequestActive and qr.users_id = :user "
			+ "order by id desc", nativeQuery = true)
	public List<RequestQuote> findByUserAndIsRequestActiveWithValidity(@Param("user") long userId,
			@Param("isRequestActive") Boolean isRequestActive);

	// Gandesh
	@Query(value = "select  EXTRACT(SECOND FROM (qr.quote_validity_calculated - now())) as quote_valid_till,* from transaction.quote_request qr "
			+ "where qr.request_is_active = :isRequestActive and qr.users_id = :user "
			+ "order by id desc", nativeQuery = true)
	public List<RequestQuote> findByBrokerAndIsRequestActiveWithValidity(@Param("user") long userId,
			@Param("isRequestActive") Boolean isRequestActive);

	// ACTIVE
	@Query(value = "select distinct d.transaction_id, d.source_id  from transaction.daddy"
			+ " right outer join transaction.kotakworld kw on kw.transaction_id = d.transaction_id"
			+ " order by id desc", nativeQuery = true)
	public List<RequestQuote> findActiveRequestForInstitutionsWithValidity(@Param("institutionId") long institutionId,
			@Param("recordStatus") long recordStatus);

	// EXPIRED
	@Query(value = "select  EXTRACT(SECOND FROM (qr.quote_validity_calculated - now())) as quote_valid_till,* from transaction.quote_request qr "
			+ "where qr.destination_id = :institutionId and qr.record_status = :recordStatus"
			+ " order by id desc", nativeQuery = true)
	public List<RequestQuote> findExpiredRequestForInstitutionsWithValidity(@Param("institutionId") long institutionId,
			@Param("recordStatus") long recordStatus);

	// INTRANSIT For Source
	@Query(value = "select  EXTRACT(SECOND FROM (qr.quote_validity_calculated - now())) as quote_valid_till,* from transaction.quote_request qr "
			+ "where qr.source_id = :institutionId" + " order by id desc", nativeQuery = true)
	public List<RequestQuote> findInTransitRequestForSourceInstitutionsWithValidity(
			@Param("institutionId") long institutionId);

	// INTRANSIT For destination
	@Query(value = "select  EXTRACT(SECOND FROM (qr.quote_validity_calculated - now())) as quote_valid_till,* from transaction.quote_request qr "
			+ "where qr.destination_id = :institutionId and qr.record_status = :recordStatus"
			+ " order by id desc", nativeQuery = true)
	public List<RequestQuote> findInTransitRequestForDestinationInstitutionsWithValidity(
			@Param("institutionId") long institutionId, @Param("recordStatus") long recordStatus);

	// CONFIRMED
	@Query(value = "select  EXTRACT(SECOND FROM (qr.quote_validity_calculated - now())) as quote_valid_till,* from transaction.quote_request qr "
			+ "where (qr.source_id = :institutionId or qr.destination_id = :institutionId) and qr.record_status = :recordStatus"
			+ " order by id desc", nativeQuery = true)
	public List<RequestQuote> findConfirmedRequestForInstitutionsWithValidity(
			@Param("institutionId") long institutionId, @Param("recordStatus") long recordStatus);

	// INTRANSIT For Source and Transaction
	@Query(value = "select  EXTRACT(SECOND FROM (qr.quote_validity_calculated - now())) as quote_valid_till,* from transaction.quote_request qr "
			+ "where qr.source_id = :institutionId and qr.transaction_id = :transactionId"
			+ " order by id desc", nativeQuery = true)
	public List<RequestQuote> findInTransitRequestForSourceInstitutionsAndTransactionWithValidity(
			@Param("institutionId") long institutionId, @Param("transactionId") String transactionId);

	// INTRANSIT For destination and Transaction
	@Query(value = "select  EXTRACT(SECOND FROM (qr.quote_validity_calculated - now())) as quote_valid_till,* from transaction.quote_request qr "
			+ "where qr.destination_id = :institutionId and qr.record_status = :recordStatus and qr.transaction_id = :transactionId"
			+ " order by id desc", nativeQuery = true)
	public List<RequestQuote> findInTransitRequestForDestinationAndTransactionInstitutionsWithValidity(
			@Param("institutionId") long institutionId
			, @Param("recordStatus") long recordStatus, 
			@Param("transactionId") String transactionId
			);

	public List<RequestQuote> findByTransactionId(String transactionId);

	public RequestQuote findByTransactionIdAndParentId(String transactionId, long parentId);
}
