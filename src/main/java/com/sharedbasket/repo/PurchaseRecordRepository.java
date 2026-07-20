package com.sharedbasket.repo;

import com.sharedbasket.domain.PurchaseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRecordRepository extends JpaRepository<PurchaseRecord, Long> {
	List<PurchaseRecord> findByShoppingGroup_IdOrderByPurchasedAtDesc(Long groupId);
	List<PurchaseRecord> findByPurchasedByUser_Id(Long userId);
}