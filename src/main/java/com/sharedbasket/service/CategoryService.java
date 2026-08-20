package com.sharedbasket.service;

import com.sharedbasket.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> listCategories(Long currentUserId, Long groupId, boolean includeArchived);
    void seedDefaultCategories(Long groupId);
}
