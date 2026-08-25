package com.sharedbasket.repo;

import com.sharedbasket.domain.PurchaseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PurchaseRecordRepository extends JpaRepository<PurchaseRecord, Long> {
	List<PurchaseRecord> findByShoppingGroup_IdOrderByPurchasedAtDesc(Long groupId);
	List<PurchaseRecord> findByPurchasedByUser_Id(Long userId);

	List<PurchaseRecord> findByShoppingGroup_IdAndPurchasedAtBetweenOrderByPurchasedAtDesc(
		Long groupId, Instant from, Instant to);

	void deleteByShoppingItem_IdAndPurchasedAt(Long shoppingItemId, Instant purchasedAt);

	@Query("select coalesce(sum(pr.pricePaid), 0) from PurchaseRecord pr " +
		"where pr.shoppingGroup.id = :groupId and pr.purchasedAt between :from and :to")
	java.math.BigDecimal sumSpendByGroupAndDateRange(
		@Param("groupId") Long groupId, @Param("from") Instant from, @Param("to") Instant to);

	@Query("select count(pr) from PurchaseRecord pr " +
		"where pr.shoppingGroup.id = :groupId and pr.purchasedAt between :from and :to")
	long countByGroupAndDateRange(
		@Param("groupId") Long groupId, @Param("from") Instant from, @Param("to") Instant to);

	@Query("select coalesce(c.name, 'Uncategorized') as categoryName, " +
		"coalesce(sum(pr.pricePaid), 0) as amount, coalesce(sum(pr.quantity), 0) as quantity " +
		"from PurchaseRecord pr left join pr.category c " +
		"where pr.shoppingGroup.id = :groupId and pr.purchasedAt between :from and :to " +
		"group by coalesce(c.name, 'Uncategorized')")
	List<Object[]> sumSpendByCategory(
		@Param("groupId") Long groupId, @Param("from") Instant from, @Param("to") Instant to);

	@Query("select pr.itemName as itemName, coalesce(sum(pr.pricePaid), 0) as amount, " +
		"coalesce(sum(pr.quantity), 0) as quantity " +
		"from PurchaseRecord pr where pr.shoppingGroup.id = :groupId and pr.purchasedAt between :from and :to " +
		"group by pr.itemName")
	List<Object[]> sumSpendByProduct(
		@Param("groupId") Long groupId, @Param("from") Instant from, @Param("to") Instant to);

	@Query("select pr.itemName as itemName, count(pr) as frequency, " +
		"coalesce(avg(pr.pricePaid), 0) as avgPrice, max(pr.purchasedAt) as lastPurchasedAt, " +
		"coalesce(c.name, 'Uncategorized') as categoryName " +
		"from PurchaseRecord pr left join pr.category c " +
		"where pr.shoppingGroup.id = :groupId and pr.purchasedAt >= :since " +
		"group by pr.itemName, coalesce(c.name, 'Uncategorized') order by count(pr) desc")
	List<Object[]> findFrequentItemsSince(@Param("groupId") Long groupId, @Param("since") Instant since);
}