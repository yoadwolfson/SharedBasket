package com.sharedbasket.service;

import com.sharedbasket.dto.CategoryDTO;
import com.sharedbasket.dto.CreateCategoryRequestDTO;
import com.sharedbasket.dto.UpdateCategoryRequestDTO;

import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(Long currentUserId, Long groupId, CreateCategoryRequestDTO request);
    List<CategoryDTO> listCategories(Long currentUserId, Long groupId, boolean includeArchived);
    CategoryDTO updateCategory(Long currentUserId, Long groupId, Long categoryId, UpdateCategoryRequestDTO request);
    void archiveCategory(Long currentUserId, Long groupId, Long categoryId, Long version);
}
