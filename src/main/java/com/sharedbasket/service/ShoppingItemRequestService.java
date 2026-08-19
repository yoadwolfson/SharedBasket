package com.sharedbasket.service;

import com.sharedbasket.dto.CreateShoppingItemRequestDTO;
import com.sharedbasket.dto.ShoppingItemRequestDTO;
import com.sharedbasket.dto.UpdateShoppingItemRequestDTO;

import java.util.List;

public interface ShoppingItemRequestService {
    ShoppingItemRequestDTO requestAdd(Long currentUserId, Long groupId, CreateShoppingItemRequestDTO request);
    ShoppingItemRequestDTO requestEdit(Long currentUserId, Long groupId, Long itemId, UpdateShoppingItemRequestDTO request);
    ShoppingItemRequestDTO requestDelete(Long currentUserId, Long groupId, Long itemId, Long version);
    List<ShoppingItemRequestDTO> listPending(Long currentUserId, Long groupId);
    List<ShoppingItemRequestDTO> listMine(Long currentUserId, Long groupId);
    ShoppingItemRequestDTO approve(Long currentUserId, Long groupId, Long requestId);
    ShoppingItemRequestDTO reject(Long currentUserId, Long groupId, Long requestId, String decisionNote);
}
