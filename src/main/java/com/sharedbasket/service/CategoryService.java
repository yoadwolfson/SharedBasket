package com.sharedbasket.service;

import com.sharedbasket.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {
    /**
     * Lists categories available to an accepted group member.
     *
     * @param currentUserId authenticated user requesting the categories
     * @param groupId group whose categories should be returned
     * @param includeArchived whether archived categories should be included
     * @return categories ordered by name
     */
    List<CategoryDTO> listCategories(Long currentUserId, Long groupId, boolean includeArchived);

    /**
     * Adds the built-in categories that are missing from a group.
     *
     * @param groupId group to initialize
     */
    void seedDefaultCategories(Long groupId);
}
