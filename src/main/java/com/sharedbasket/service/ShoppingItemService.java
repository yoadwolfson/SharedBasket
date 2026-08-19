package com.sharedbasket.service;

import com.sharedbasket.dto.CreateShoppingItemRequestDTO;
import com.sharedbasket.dto.ShoppingItemDTO;
import com.sharedbasket.dto.UpdateShoppingItemRequestDTO;

import java.util.List;

public interface ShoppingItemService {
    ShoppingItemDTO createItem(Long currentUserId, Long groupId, CreateShoppingItemRequestDTO request);
    List<ShoppingItemDTO> listItems(Long currentUserId, Long groupId, Boolean purchased);
    ShoppingItemDTO updateItem(Long currentUserId, Long groupId, Long itemId, UpdateShoppingItemRequestDTO request);
    ShoppingItemDTO togglePurchased(Long currentUserId, Long groupId, Long itemId, Long version, boolean purchased);
    void deleteItem(Long currentUserId, Long groupId, Long itemId);
}
