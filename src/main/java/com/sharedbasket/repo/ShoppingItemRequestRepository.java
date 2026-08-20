package com.sharedbasket.repo;

import com.sharedbasket.domain.ShoppingItemRequest;
import com.sharedbasket.domain.ShoppingItemRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingItemRequestRepository extends JpaRepository<ShoppingItemRequest, Long> {
    List<ShoppingItemRequest> findByShoppingGroup_IdAndStatusOrderByCreatedAtAsc(
        Long groupId, ShoppingItemRequestStatus status);

    List<ShoppingItemRequest> findByShoppingGroup_IdAndRequestedByUser_IdOrderByCreatedAtDesc(
        Long groupId, Long userId);

    Optional<ShoppingItemRequest> findByIdAndShoppingGroup_Id(Long requestId, Long groupId);

    boolean existsByShoppingGroup_IdAndRequestedByUser_IdAndShoppingItem_IdAndTypeAndStatus(
        Long groupId, Long userId, Long itemId, com.sharedbasket.domain.ShoppingItemRequestType type,
        ShoppingItemRequestStatus status);

    List<ShoppingItemRequest> findByShoppingGroup_IdAndRequestedByUser_IdAndTypeAndStatus(
        Long groupId, Long userId, com.sharedbasket.domain.ShoppingItemRequestType type,
        ShoppingItemRequestStatus status);
}
