package com.sharedbasket.repo;

import com.sharedbasket.domain.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
	List<ShoppingItem> findByShoppingGroup_IdAndPurchasedFalse(Long groupId);
	List<ShoppingItem> findByShoppingGroup_IdOrderByPurchasedAscNameAsc(Long groupId);
	List<ShoppingItem> findByShoppingGroup_IdAndPurchasedFalseOrderByNameAsc(Long groupId);
	List<ShoppingItem> findByShoppingGroup_IdAndPurchasedTrueOrderByNameAsc(Long groupId);
	List<ShoppingItem> findByCategory_Id(Long categoryId);
	Optional<ShoppingItem> findByIdAndShoppingGroup_Id(Long itemId, Long groupId);
}