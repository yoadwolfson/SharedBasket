package com.sharedbasket.repo;

import com.sharedbasket.domain.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
	List<ShoppingItem> findByShoppingGroup_Id(Long groupId);
	List<ShoppingItem> findByShoppingGroup_IdAndPurchasedFalse(Long groupId);
	List<ShoppingItem> findByCategory_Id(Long categoryId);
}