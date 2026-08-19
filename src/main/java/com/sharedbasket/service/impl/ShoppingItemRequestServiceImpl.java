package com.sharedbasket.service.impl;

import com.sharedbasket.domain.Category;
import com.sharedbasket.domain.GroupMember;
import com.sharedbasket.domain.SbGroup;
import com.sharedbasket.domain.ShoppingItem;
import com.sharedbasket.domain.ShoppingItemRequest;
import com.sharedbasket.domain.ShoppingItemRequestStatus;
import com.sharedbasket.domain.ShoppingItemRequestType;
import com.sharedbasket.domain.User;
import com.sharedbasket.dto.CreateShoppingItemRequestDTO;
import com.sharedbasket.dto.ShoppingItemRequestDTO;
import com.sharedbasket.dto.UpdateShoppingItemRequestDTO;
import com.sharedbasket.exception.BadRequestException;
import com.sharedbasket.exception.ConflictOperationException;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.exception.ResourceNotFoundException;
import com.sharedbasket.repo.CategoryRepository;
import com.sharedbasket.repo.GroupMemberRepository;
import com.sharedbasket.repo.SbGroupRepository;
import com.sharedbasket.repo.ShoppingItemRepository;
import com.sharedbasket.repo.ShoppingItemRequestRepository;
import com.sharedbasket.repo.UserRepository;
import com.sharedbasket.service.ShoppingItemRequestService;
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
public class ShoppingItemRequestServiceImpl implements ShoppingItemRequestService {

    private static final String ACCEPTED = "ACCEPTED";
    private static final String ADMIN = "ADMIN";

    private final ShoppingItemRequestRepository requestRepository;
    private final ShoppingItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SbGroupRepository groupRepository;
    private final UserRepository userRepository;

    public ShoppingItemRequestServiceImpl(ShoppingItemRequestRepository requestRepository,
                                          ShoppingItemRepository itemRepository,
                                          CategoryRepository categoryRepository,
                                          GroupMemberRepository groupMemberRepository,
                                          SbGroupRepository groupRepository,
                                          UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ShoppingItemRequestDTO requestAdd(Long currentUserId, Long groupId, CreateShoppingItemRequestDTO request) {
        GroupMember member = requireMember(currentUserId, groupId);
        if (ADMIN.equals(member.getRole())) {
            throw new BadRequestException("Admins should add items directly");
        }
        if (request == null) throw new BadRequestException("Request body is required");
        ShoppingItemRequest itemRequest = newRequest(groupId, currentUserId, ShoppingItemRequestType.ADD);
        copyCreateFields(itemRequest, groupId, request);
        return toDto(requestRepository.save(itemRequest));
    }

    @Override
    public ShoppingItemRequestDTO requestEdit(Long currentUserId, Long groupId, Long itemId, UpdateShoppingItemRequestDTO request) {
        GroupMember member = requireMember(currentUserId, groupId);
        if (ADMIN.equals(member.getRole())) throw new BadRequestException("Admins should edit items directly");
        if (request == null) throw new BadRequestException("Request body is required");
        ShoppingItem item = getItem(groupId, itemId);
        requireVersion(item.getVersion(), request.getVersion());
        rejectDuplicate(currentUserId, groupId, itemId, ShoppingItemRequestType.EDIT);
        ShoppingItemRequest itemRequest = newRequest(groupId, currentUserId, ShoppingItemRequestType.EDIT);
        itemRequest.setShoppingItem(item);
        itemRequest.setItemVersion(item.getVersion());
        itemRequest.setName(request.getName() == null ? item.getName() : validateName(request.getName()));
        itemRequest.setQuantity(request.getQuantity() == null ? item.getQuantity() : request.getQuantity());
        itemRequest.setUnit(request.getUnit() == null ? item.getUnit() : normalize(request.getUnit()));
        itemRequest.setEstimatedPrice(request.getEstimatedPrice() == null ? item.getEstimatedPrice() : request.getEstimatedPrice());
        itemRequest.setCategory(request.getCategoryId() == null ? item.getCategory() : resolveCategory(groupId, request.getCategoryId()));
        itemRequest.setNotes(request.getNotes() == null ? item.getNotes() : normalize(request.getNotes()));
        validateNumbers(itemRequest.getQuantity(), itemRequest.getEstimatedPrice());
        return toDto(requestRepository.save(itemRequest));
    }

    @Override
    public ShoppingItemRequestDTO requestDelete(Long currentUserId, Long groupId, Long itemId, Long version) {
        GroupMember member = requireMember(currentUserId, groupId);
        if (ADMIN.equals(member.getRole())) throw new BadRequestException("Admins should delete items directly");
        ShoppingItem item = getItem(groupId, itemId);
        requireVersion(item.getVersion(), version);
        rejectDuplicate(currentUserId, groupId, itemId, ShoppingItemRequestType.DELETE);
        ShoppingItemRequest itemRequest = newRequest(groupId, currentUserId, ShoppingItemRequestType.DELETE);
        itemRequest.setShoppingItem(item);
        itemRequest.setItemVersion(item.getVersion());
        copyItemFields(itemRequest, item);
        return toDto(requestRepository.save(itemRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShoppingItemRequestDTO> listPending(Long currentUserId, Long groupId) {
        requireAdmin(currentUserId, groupId);
        return requestRepository.findByShoppingGroup_IdAndStatusOrderByCreatedAtAsc(groupId, ShoppingItemRequestStatus.PENDING)
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShoppingItemRequestDTO> listMine(Long currentUserId, Long groupId) {
        requireMember(currentUserId, groupId);
        return requestRepository.findByShoppingGroup_IdAndRequestedByUser_IdOrderByCreatedAtDesc(groupId, currentUserId)
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ShoppingItemRequestDTO approve(Long currentUserId, Long groupId, Long requestId) {
        User admin = requireAdmin(currentUserId, groupId);
        ShoppingItemRequest itemRequest = getRequest(groupId, requestId);
        requirePending(itemRequest);
        try {
            if (itemRequest.getType() == ShoppingItemRequestType.ADD) {
                ShoppingItem item = new ShoppingItem();
                item.setShoppingGroup(itemRequest.getShoppingGroup());
                item.setCreatedByUser(itemRequest.getRequestedByUser());
                copyRequestToItem(itemRequest, item);
                item.setCreatedAt(Instant.now());
                item.setUpdatedAt(Instant.now());
                itemRepository.save(item);
            } else {
                ShoppingItem item = itemRequest.getShoppingItem();
                if (item == null) throw new ResourceNotFoundException("Requested item no longer exists");
                requireVersion(item.getVersion(), itemRequest.getItemVersion());
                if (itemRequest.getType() == ShoppingItemRequestType.DELETE) {
                    itemRepository.delete(item);
                } else {
                    copyRequestToItem(itemRequest, item);
                    item.setUpdatedAt(Instant.now());
                    itemRepository.saveAndFlush(item);
                }
            }
            decide(itemRequest, admin, ShoppingItemRequestStatus.APPROVED, null);
            return toDto(requestRepository.save(itemRequest));
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException ex) {
            throw new ConflictOperationException("Item changed while this request was pending. Refresh and review again.");
        }
    }

    @Override
    public ShoppingItemRequestDTO reject(Long currentUserId, Long groupId, Long requestId, String decisionNote) {
        User admin = requireAdmin(currentUserId, groupId);
        ShoppingItemRequest itemRequest = getRequest(groupId, requestId);
        requirePending(itemRequest);
        decide(itemRequest, admin, ShoppingItemRequestStatus.REJECTED, normalize(decisionNote));
        return toDto(requestRepository.save(itemRequest));
    }

    private ShoppingItemRequest newRequest(Long groupId, Long userId, ShoppingItemRequestType type) {
        ShoppingItemRequest itemRequest = new ShoppingItemRequest();
        itemRequest.setShoppingGroup(getGroup(groupId));
        itemRequest.setRequestedByUser(getUser(userId));
        itemRequest.setType(type);
        itemRequest.setStatus(ShoppingItemRequestStatus.PENDING);
        itemRequest.setCreatedAt(Instant.now());
        return itemRequest;
    }

    private void copyCreateFields(ShoppingItemRequest target, Long groupId, CreateShoppingItemRequestDTO source) {
        target.setName(validateName(source.getName()));
        target.setQuantity(source.getQuantity() == null ? BigDecimal.ONE : source.getQuantity());
        target.setUnit(normalize(source.getUnit()));
        target.setEstimatedPrice(source.getEstimatedPrice());
        target.setCategory(resolveCategory(groupId, source.getCategoryId()));
        target.setNotes(normalize(source.getNotes()));
        validateNumbers(target.getQuantity(), target.getEstimatedPrice());
    }

    private void copyItemFields(ShoppingItemRequest target, ShoppingItem item) {
        target.setName(item.getName());
        target.setQuantity(item.getQuantity());
        target.setUnit(item.getUnit());
        target.setEstimatedPrice(item.getEstimatedPrice());
        target.setCategory(item.getCategory());
        target.setNotes(item.getNotes());
    }

    private void copyRequestToItem(ShoppingItemRequest source, ShoppingItem target) {
        target.setName(source.getName());
        target.setQuantity(source.getQuantity());
        target.setUnit(source.getUnit());
        target.setEstimatedPrice(source.getEstimatedPrice());
        target.setCategory(source.getCategory());
        target.setNotes(source.getNotes());
        target.setPurchased(Boolean.FALSE);
    }

    private void decide(ShoppingItemRequest request, User admin, ShoppingItemRequestStatus status, String note) {
        request.setStatus(status);
        request.setDecidedByUser(admin);
        request.setDecidedAt(Instant.now());
        request.setDecisionNote(note);
    }

    private void rejectDuplicate(Long userId, Long groupId, Long itemId, ShoppingItemRequestType type) {
        if (requestRepository.existsByShoppingGroup_IdAndRequestedByUser_IdAndShoppingItem_IdAndTypeAndStatus(
            groupId, userId, itemId, type, ShoppingItemRequestStatus.PENDING)) {
            throw new ConflictOperationException("A matching request is already pending");
        }
    }

    private void requirePending(ShoppingItemRequest request) {
        if (request.getStatus() != ShoppingItemRequestStatus.PENDING) {
            throw new ConflictOperationException("Only pending requests can be decided");
        }
    }

    private GroupMember requireMember(Long userId, Long groupId) {
        if (userId == null || groupId == null) throw new BadRequestException("User id and group id are required");
        GroupMember membership = groupMemberRepository.findByShoppingGroup_IdAndUser_Id(groupId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));
        if (!ACCEPTED.equals(membership.getStatus())) throw new ForbiddenOperationException("Accepted group membership is required");
        return membership;
    }

    private User requireAdmin(Long userId, Long groupId) {
        GroupMember member = requireMember(userId, groupId);
        if (!ADMIN.equals(member.getRole())) throw new ForbiddenOperationException("Admin access is required for this action");
        return getUser(userId);
    }

    private ShoppingItemRequest getRequest(Long groupId, Long requestId) {
        if (requestId == null) throw new BadRequestException("Request id is required");
        return requestRepository.findByIdAndShoppingGroup_Id(requestId, groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Shopping item request not found"));
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
        if (Boolean.TRUE.equals(category.getArchived())) throw new ConflictOperationException("Archived categories cannot be assigned");
        return category;
    }

    private SbGroup getGroup(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String validateName(String value) {
        if (value == null || value.trim().isEmpty()) throw new BadRequestException("Item name is required");
        String name = value.trim();
        if (name.length() > 200) throw new BadRequestException("Item name must be at most 200 characters");
        return name;
    }

    private void validateNumbers(BigDecimal quantity, BigDecimal price) {
        if (quantity == null || quantity.signum() <= 0) throw new BadRequestException("Quantity must be greater than zero");
        if (price != null && price.signum() < 0) throw new BadRequestException("Estimated price cannot be negative");
    }

    private String normalize(String value) { return value == null || value.trim().isEmpty() ? null : value.trim(); }

    private void requireVersion(Long actual, Long expected) {
        if (expected == null) throw new BadRequestException("Version is required");
        if (!Objects.equals(actual, expected)) throw new ConflictOperationException("Item changed while this request was pending");
    }

    private ShoppingItemRequestDTO toDto(ShoppingItemRequest request) {
        ShoppingItemRequestDTO dto = new ShoppingItemRequestDTO();
        dto.setId(request.getId());
        dto.setGroupId(request.getShoppingGroup().getId());
        dto.setRequestedByUserId(request.getRequestedByUser().getId());
        dto.setRequestedByUserName(request.getRequestedByUser().getUsername());
        dto.setItemId(request.getShoppingItem() == null ? null : request.getShoppingItem().getId());
        dto.setType(request.getType());
        dto.setStatus(request.getStatus());
        dto.setItemVersion(request.getItemVersion());
        dto.setName(request.getName());
        dto.setQuantity(request.getQuantity());
        dto.setUnit(request.getUnit());
        dto.setEstimatedPrice(request.getEstimatedPrice());
        if (request.getCategory() != null) {
            dto.setCategoryId(request.getCategory().getId());
            dto.setCategoryName(request.getCategory().getName());
        }
        dto.setNotes(request.getNotes());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setDecidedAt(request.getDecidedAt());
        if (request.getDecidedByUser() != null) dto.setDecidedByUserId(request.getDecidedByUser().getId());
        dto.setDecisionNote(request.getDecisionNote());
        return dto;
    }
}
