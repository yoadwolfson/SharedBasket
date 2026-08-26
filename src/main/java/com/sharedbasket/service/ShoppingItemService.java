package com.sharedbasket.service;

import com.sharedbasket.dto.CreateShoppingItemRequestDTO;
import com.sharedbasket.dto.ShoppingItemDTO;
import com.sharedbasket.dto.UpdateShoppingItemRequestDTO;

import java.util.List;

public interface ShoppingItemService {
    /**
     * Creates a new shopping item.
     *
     * @param currentUserId authenticated user performing the operation
     * @param groupId group that will own the item
     * @param request item values to validate and save
     * @return the created item
     */
    ShoppingItemDTO createItem(Long currentUserId, Long groupId, CreateShoppingItemRequestDTO request);

    /**
     * Lists a group's shopping items.
     *
     * @param currentUserId authenticated user requesting the list
     * @param groupId group whose items should be returned
     * @param purchased optional purchased-state filter
     * @return matching items
     */
    List<ShoppingItemDTO> listItems(Long currentUserId, Long groupId, Boolean purchased);

    /**
     * Applies a version-checked partial update to an item.
     *
     * @param currentUserId authenticated user performing the operation
     * @param groupId group containing the item
     * @param itemId item to update
     * @param request fields and expected version to apply
     * @return the updated item
     */
    ShoppingItemDTO updateItem(Long currentUserId, Long groupId, Long itemId, UpdateShoppingItemRequestDTO request);

    /**
     * Changes an item's purchased state and maintains purchase history.
     *
     * @param currentUserId authenticated user performing the operation
     * @param groupId group containing the item
     * @param itemId item to change
     * @param version expected item version
     * @param purchased target purchased state
     * @return the updated item
     */
    ShoppingItemDTO togglePurchased(Long currentUserId, Long groupId, Long itemId, Long version, boolean purchased);

    /**
     * Deletes an item from a group.
     *
     * @param currentUserId authenticated user performing the operation
     * @param groupId group containing the item
     * @param itemId item to delete
     */
    void deleteItem(Long currentUserId, Long groupId, Long itemId);
}
