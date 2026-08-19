package com.sharedbasket.service.impl;

import com.sharedbasket.domain.Category;
import com.sharedbasket.domain.GroupMember;
import com.sharedbasket.domain.SbGroup;
import com.sharedbasket.dto.CategoryDTO;
import com.sharedbasket.dto.CategoryColorOption;
import com.sharedbasket.dto.CreateCategoryRequestDTO;
import com.sharedbasket.dto.UpdateCategoryRequestDTO;
import com.sharedbasket.exception.BadRequestException;
import com.sharedbasket.exception.ConflictOperationException;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.exception.ResourceNotFoundException;
import com.sharedbasket.repo.CategoryRepository;
import com.sharedbasket.repo.GroupMemberRepository;
import com.sharedbasket.repo.SbGroupRepository;
import com.sharedbasket.service.CategoryService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final String ACCEPTED = "ACCEPTED";
    private static final String ADMIN = "ADMIN";

    private final CategoryRepository categoryRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SbGroupRepository groupRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               GroupMemberRepository groupMemberRepository,
                               SbGroupRepository groupRepository) {
        this.categoryRepository = categoryRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public CategoryDTO createCategory(Long currentUserId, Long groupId, CreateCategoryRequestDTO request) {
        requireAdmin(currentUserId, groupId);
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        String name = normalizeName(request.getName());
        ensureUniqueName(groupId, name, null);
        Category category = new Category();
        category.setShoppingGroup(getGroup(groupId));
        category.setName(name);
        category.setColor(normalizeColor(request.getColor()));
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        validateSortOrder(category.getSortOrder());
        category.setArchived(Boolean.FALSE);
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());
        try {
            return toDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictOperationException("Category name already exists in this group");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> listCategories(Long currentUserId, Long groupId, boolean includeArchived) {
        requireAcceptedMember(currentUserId, groupId);
        List<Category> categories = includeArchived
            ? categoryRepository.findByShoppingGroup_IdOrderBySortOrderAscNameAsc(groupId)
            : categoryRepository.findByShoppingGroup_IdAndArchivedFalseOrderBySortOrderAscNameAsc(groupId);
        return categories.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDTO updateCategory(Long currentUserId, Long groupId, Long categoryId, UpdateCategoryRequestDTO request) {
        requireAdmin(currentUserId, groupId);
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        Category category = getCategory(groupId, categoryId);
        requireVersion(category.getVersion(), request.getVersion());
        if (request.getName() != null) {
            String name = normalizeName(request.getName());
            ensureUniqueName(groupId, name, categoryId);
            category.setName(name);
        }
        if (request.getColor() != null) {
            category.setColor(normalizeColor(request.getColor()));
        }
        if (request.getSortOrder() != null) {
            validateSortOrder(request.getSortOrder());
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getArchived() != null) {
            category.setArchived(request.getArchived());
        }
        category.setUpdatedAt(Instant.now());
        try {
            return toDto(categoryRepository.saveAndFlush(category));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictOperationException("Category update conflicts with an existing category");
        }
    }

    @Override
    public void archiveCategory(Long currentUserId, Long groupId, Long categoryId, Long version) {
        requireAdmin(currentUserId, groupId);
        Category category = getCategory(groupId, categoryId);
        requireVersion(category.getVersion(), version);
        category.setArchived(Boolean.TRUE);
        category.setUpdatedAt(Instant.now());
        try {
            categoryRepository.saveAndFlush(category);
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException ex) {
            throw new ConflictOperationException("Category was modified by another user. Please refresh and retry.");
        }
    }

    private GroupMember requireAcceptedMember(Long userId, Long groupId) {
        if (userId == null || groupId == null) {
            throw new BadRequestException("User id and group id are required");
        }
        GroupMember membership = groupMemberRepository.findByShoppingGroup_IdAndUser_Id(groupId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));
        if (!ACCEPTED.equals(membership.getStatus())) {
            throw new ForbiddenOperationException("Accepted group membership is required");
        }
        return membership;
    }

    private void requireAdmin(Long userId, Long groupId) {
        GroupMember membership = requireAcceptedMember(userId, groupId);
        if (!ADMIN.equals(membership.getRole())) {
            throw new ForbiddenOperationException("Admin access is required for this action");
        }
    }

    private Category getCategory(Long groupId, Long categoryId) {
        if (categoryId == null) {
            throw new BadRequestException("Category id is required");
        }
        return categoryRepository.findByIdAndShoppingGroup_Id(categoryId, groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private SbGroup getGroup(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group id is required");
        }
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }

    private void ensureUniqueName(Long groupId, String name, Long ignoredId) {
        categoryRepository.findByShoppingGroup_Id(groupId).stream()
            .filter(category -> !Objects.equals(category.getId(), ignoredId))
            .filter(category -> category.getName() != null && category.getName().trim().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT)))
            .findFirst()
            .ifPresent(category -> { throw new ConflictOperationException("Category name already exists in this group"); });
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Category name is required");
        }
        String normalized = name.trim();
        if (normalized.length() > 100) {
            throw new BadRequestException("Category name must be at most 100 characters");
        }
        return normalized;
    }

    private String normalizeColor(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!CategoryColorOption.isAllowed(normalized)) {
            throw new BadRequestException("Category color must be selected from the approved palette");
        }
        return normalized;
    }

    private void validateSortOrder(Integer sortOrder) {
        if (sortOrder != null && sortOrder < 0) {
            throw new BadRequestException("Sort order cannot be negative");
        }
    }

    private void requireVersion(Long actual, Long expected) {
        if (expected == null) {
            throw new BadRequestException("Version is required for updates");
        }
        if (!Objects.equals(actual, expected)) {
            throw new ConflictOperationException("Category was modified by another user. Please refresh and retry.");
        }
    }

    private CategoryDTO toDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setGroupId(category.getShoppingGroup().getId());
        dto.setName(category.getName());
        dto.setColor(category.getColor());
        dto.setSortOrder(category.getSortOrder());
        dto.setArchived(category.getArchived());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setVersion(category.getVersion());
        return dto;
    }
}
