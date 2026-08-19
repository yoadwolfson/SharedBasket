package com.sharedbasket.service.impl;

import com.sharedbasket.domain.Category;
import com.sharedbasket.domain.GroupMember;
import com.sharedbasket.domain.SbGroup;
import com.sharedbasket.domain.ShoppingItem;
import com.sharedbasket.domain.User;
import com.sharedbasket.dto.CreateShoppingItemRequestDTO;
import com.sharedbasket.dto.ShoppingItemDTO;
import com.sharedbasket.dto.UpdateShoppingItemRequestDTO;
import com.sharedbasket.exception.BadRequestException;
import com.sharedbasket.exception.ConflictOperationException;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.exception.ResourceNotFoundException;
import com.sharedbasket.repo.CategoryRepository;
import com.sharedbasket.repo.GroupMemberRepository;
import com.sharedbasket.repo.SbGroupRepository;
import com.sharedbasket.repo.ShoppingItemRepository;
import com.sharedbasket.repo.UserRepository;
import com.sharedbasket.service.ShoppingItemService;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShoppingItemServiceImpl implements ShoppingItemService {

    private static final String ACCEPTED = "ACCEPTED";
    private static final String ADMIN = "ADMIN";

    private final ShoppingItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SbGroupRepository groupRepository;
    private final UserRepository userRepository;

    public ShoppingItemServiceImpl(ShoppingItemRepository itemRepository,
                                   CategoryRepository categoryRepository,
                                   GroupMemberRepository groupMemberRepository,
                                   SbGroupRepository groupRepository,
                                   UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ShoppingItemDTO createItem(Long currentUserId, Long groupId, CreateShoppingItemRequestDTO request) {
        requireAdmin(currentUserId, groupId);
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        String name = validateName(request.getName());
        BigDecimal quantity = request.getQuantity() == null ? BigDecimal.ONE : request.getQuantity();
        validateNumbers(quantity, request.getEstimatedPrice());
        ShoppingItem item = new ShoppingItem();
        item.setShoppingGroup(getGroup(groupId));
        item.setCreatedByUser(getUser(currentUserId));
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnit(normalize(request.getUnit()));
        item.setEstimatedPrice(request.getEstimatedPrice());
        item.setCategory(resolveCategory(groupId, request.getCategoryId()));
        item.setNotes(normalize(request.getNotes()));
        item.setPurchased(Boolean.FALSE);
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
        return toDto(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShoppingItemDTO> listItems(Long currentUserId, Long groupId, Boolean purchased) {
        requireMember(currentUserId, groupId);
        List<ShoppingItem> items;
        if (Boolean.TRUE.equals(purchased)) {
            items = itemRepository.findByShoppingGroup_IdAndPurchasedTrueOrderByNameAsc(groupId);
        } else if (Boolean.FALSE.equals(purchased)) {
            items = itemRepository.findByShoppingGroup_IdAndPurchasedFalseOrderByNameAsc(groupId);
        } else {
            items = itemRepository.findByShoppingGroup_IdOrderByPurchasedAscNameAsc(groupId);
        }
        return items.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ShoppingItemDTO updateItem(Long currentUserId, Long groupId, Long itemId, UpdateShoppingItemRequestDTO request) {
        requireAdmin(currentUserId, groupId);
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        ShoppingItem item = getItem(groupId, itemId);
        requireVersion(item.getVersion(), request.getVersion());
        if (request.getName() != null) item.setName(validateName(request.getName()));
        if (request.getQuantity() != null) {
            validateNumbers(request.getQuantity(), request.getEstimatedPrice());
            item.setQuantity(request.getQuantity());
        }
        if (request.getEstimatedPrice() != null) {
            validateNumbers(item.getQuantity(), request.getEstimatedPrice());
            item.setEstimatedPrice(request.getEstimatedPrice());
        }
        if (request.getUnit() != null) item.setUnit(normalize(request.getUnit()));
        if (request.getCategoryId() != null) item.setCategory(resolveCategory(groupId, request.getCategoryId()));
        if (request.getNotes() != null) item.setNotes(normalize(request.getNotes()));
        if (request.getPurchased() != null && !Objects.equals(item.getPurchased(), request.getPurchased())) {
            applyPurchaseState(item, currentUserId, request.getPurchased());
        }
        item.setUpdatedAt(Instant.now());
        return saveWithConflict(item);
    }

    @Override
    public ShoppingItemDTO togglePurchased(Long currentUserId, Long groupId, Long itemId, Long version, boolean purchased) {
        requireMember(currentUserId, groupId);
        ShoppingItem item = getItem(groupId, itemId);
        requireVersion(item.getVersion(), version);
        if (!Objects.equals(item.getPurchased(), purchased)) {
            applyPurchaseState(item, currentUserId, purchased);
            item.setUpdatedAt(Instant.now());
            return saveWithConflict(item);
        }
        return toDto(item);
    }

    @Override
    public void deleteItem(Long currentUserId, Long groupId, Long itemId) {
        requireAdmin(currentUserId, groupId);
        itemRepository.delete(getItem(groupId, itemId));
    }

    private void applyPurchaseState(ShoppingItem item, Long userId, boolean purchased) {
        item.setPurchased(purchased);
        if (purchased) {
            item.setPurchasedAt(Instant.now());
            item.setPurchasedByUser(getUser(userId));
        } else {
            item.setPurchasedAt(null);
            item.setPurchasedByUser(null);
        }
    }

    private ShoppingItemDTO saveWithConflict(ShoppingItem item) {
        try {
            return toDto(itemRepository.saveAndFlush(item));
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException ex) {
            throw new ConflictOperationException("Item was modified by another user. Please refresh and retry.");
        }
    }

    private GroupMember requireMember(Long userId, Long groupId) {
        if (userId == null || groupId == null) throw new BadRequestException("User id and group id are required");
        GroupMember membership = groupMemberRepository.findByShoppingGroup_IdAndUser_Id(groupId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));
        if (!ACCEPTED.equals(membership.getStatus())) throw new ForbiddenOperationException("Accepted group membership is required");
        return membership;
    }

    private void requireAdmin(Long userId, Long groupId) {
        GroupMember membership = requireMember(userId, groupId);
        if (!ADMIN.equals(membership.getRole())) {
            throw new ForbiddenOperationException("Admin access is required for direct item changes");
        }
    }

    private ShoppingItem getItem(Long groupId, Long itemId) {
        if (itemId == null) throw new BadRequestException("Item id is required");
        return itemRepository.findByIdAndShoppingGroup_Id(itemId, groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Shopping item not found"));
    }

    private Category resolveCategory(Long groupId, Long categoryId) {
        if (categoryId == null) return null;
        Category category = categoryRepository.findByIdAndShoppingGroup_Id(categoryId, groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found in this group"));
        if (Boolean.TRUE.equals(category.getArchived())) throw new ConflictOperationException("Archived categories cannot be assigned to new items");
        return category;
    }

    private SbGroup getGroup(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) throw new BadRequestException("Item name is required");
        String normalized = name.trim();
        if (normalized.length() > 200) throw new BadRequestException("Item name must be at most 200 characters");
        return normalized;
    }

    private void validateNumbers(BigDecimal quantity, BigDecimal price) {
        if (quantity == null || quantity.signum() <= 0) throw new BadRequestException("Quantity must be greater than zero");
        if (price != null && price.signum() < 0) throw new BadRequestException("Estimated price cannot be negative");
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().isEmpty() ? null : value.trim();
    }

    private void requireVersion(Long actual, Long expected) {
        if (expected == null) throw new BadRequestException("Version is required for updates");
        if (!Objects.equals(actual, expected)) throw new ConflictOperationException("Item was modified by another user. Please refresh and retry.");
    }

    private ShoppingItemDTO toDto(ShoppingItem item) {
        ShoppingItemDTO dto = new ShoppingItemDTO();
        dto.setId(item.getId());
        dto.setGroupId(item.getShoppingGroup().getId());
        dto.setName(item.getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnit(item.getUnit());
        dto.setEstimatedPrice(item.getEstimatedPrice());
        dto.setPurchased(item.getPurchased());
        dto.setPurchasedAt(item.getPurchasedAt());
        if (item.getPurchasedByUser() != null) {
            dto.setPurchasedByUserId(item.getPurchasedByUser().getId());
            dto.setPurchasedByUserName(item.getPurchasedByUser().getUsername());
        }
        if (item.getCategory() != null) {
            dto.setCategoryId(item.getCategory().getId());
            dto.setCategoryName(item.getCategory().getName());
        }
        dto.setNotes(item.getNotes());
        dto.setCreatedByUserId(item.getCreatedByUser().getId());
        dto.setCreatedByUserName(item.getCreatedByUser().getUsername());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        dto.setVersion(item.getVersion());
        return dto;
    }
}
