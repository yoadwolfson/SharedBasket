package com.sharedbasket.service.impl;

import com.sharedbasket.domain.Category;
import com.sharedbasket.domain.DefaultCategory;
import com.sharedbasket.domain.GroupMember;
import com.sharedbasket.domain.SbGroup;
import com.sharedbasket.dto.CategoryDTO;
import com.sharedbasket.exception.BadRequestException;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.exception.ResourceNotFoundException;
import com.sharedbasket.repo.CategoryRepository;
import com.sharedbasket.repo.GroupMemberRepository;
import com.sharedbasket.repo.SbGroupRepository;
import com.sharedbasket.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final String ACCEPTED = "ACCEPTED";

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
    @Transactional(readOnly = true)
    public List<CategoryDTO> listCategories(Long currentUserId, Long groupId, boolean includeArchived) {
        // Archived categories stay hidden by default so old data does not appear in new item forms.
        requireAcceptedMember(currentUserId, groupId);
        List<Category> categories = includeArchived
            ? categoryRepository.findByShoppingGroup_IdOrderByNameAsc(groupId)
            : categoryRepository.findByShoppingGroup_IdAndArchivedFalseOrderByNameAsc(groupId);
        return categories.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void seedDefaultCategories(Long groupId) {
        // Seeding is idempotent: groups can be initialized more than once without duplicate rows.
        SbGroup group = getGroup(groupId);
        Set<String> existing = categoryRepository.findByShoppingGroup_Id(groupId).stream()
            .filter(category -> category.getName() != null)
            .map(category -> category.getName().trim().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
        for (DefaultCategory preset : DefaultCategory.all()) {
            if (existing.contains(preset.getDisplayName().toLowerCase(Locale.ROOT))) {
                continue;
            }
            Category category = new Category();
            category.setShoppingGroup(group);
            category.setName(preset.getDisplayName());
            category.setColor(preset.getColor());
            category.setEmoji(preset.getEmoji());
            category.setArchived(Boolean.FALSE);
            category.setCreatedAt(Instant.now());
            category.setUpdatedAt(Instant.now());
            categoryRepository.save(category);
        }
    }

    private GroupMember requireAcceptedMember(Long userId, Long groupId) {
        // Keep membership enforcement in the service so REST and JSF callers follow the same rule.
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

    private SbGroup getGroup(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group id is required");
        }
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }

    private CategoryDTO toDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setGroupId(category.getShoppingGroup().getId());
        dto.setName(category.getName());
        dto.setColor(category.getColor());
        dto.setEmoji(category.getEmoji());
        dto.setArchived(category.getArchived());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setVersion(category.getVersion());
        return dto;
    }
}
