package com.sharedbasket.service.impl;

import com.sharedbasket.domain.GroupMember;
import com.sharedbasket.domain.SbGroup;
import com.sharedbasket.domain.User;
import com.sharedbasket.dto.CreateGroupRequestDTO;
import com.sharedbasket.dto.GroupDTO;
import com.sharedbasket.dto.GroupMemberDTO;
import com.sharedbasket.dto.JoinByCodeRequestDTO;
import com.sharedbasket.dto.UpdateGroupRequestDTO;
import com.sharedbasket.exception.BadRequestException;
import com.sharedbasket.exception.ConflictOperationException;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.exception.ResourceNotFoundException;
import com.sharedbasket.repo.GroupMemberRepository;
import com.sharedbasket.repo.SbGroupRepository;
import com.sharedbasket.repo.UserRepository;
import com.sharedbasket.service.CategoryService;
import com.sharedbasket.service.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupServiceImpl implements GroupService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_REMOVED = "REMOVED";

    private static final String INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_LEN = 10;

    private final SbGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final SecureRandom secureRandom = new SecureRandom();

    public GroupServiceImpl(
        SbGroupRepository groupRepository,
        GroupMemberRepository groupMemberRepository,
        UserRepository userRepository,
        CategoryService categoryService
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }

    @Override
    public GroupDTO createGroup(Long currentUserId, CreateGroupRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        validateGroupName(request != null ? request.getName() : null);
        User owner = getUser(currentUserId);

        SbGroup group = new SbGroup();
        group.setName(request.getName().trim());
        group.setOwnerUser(owner);
        group.setInviteCode(generateUniqueInviteCode());
        group.setCreatedAt(Instant.now());
        group.setUpdatedAt(Instant.now());
        group = groupRepository.save(group);

        GroupMember ownerMembership = new GroupMember();
        ownerMembership.setShoppingGroup(group);
        ownerMembership.setUser(owner);
        ownerMembership.setRole(ROLE_ADMIN);
        ownerMembership.setStatus(STATUS_ACCEPTED);
        ownerMembership.setJoinedAt(Instant.now());
        ownerMembership.setDecidedAt(Instant.now());
        groupMemberRepository.save(ownerMembership);
        categoryService.seedDefaultCategories(group.getId());

        return toGroupDTO(group, ownerMembership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupDTO> listMyGroups(Long currentUserId) {
        return groupMemberRepository.findByUser_IdAndStatus(currentUserId, STATUS_ACCEPTED)
            .stream()
            // Stable order is required: the JSF page re-fetches this list on every poll and
            // matches rows by index, so an unordered DB result can make "Open" pick the wrong group.
            .sorted(Comparator.comparing(member -> member.getShoppingGroup().getId()))
            .map(member -> toGroupDTO(member.getShoppingGroup(), member))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GroupDTO getGroup(Long currentUserId, Long groupId) {
        GroupMember membership = getMembership(groupId, currentUserId);
        if (!STATUS_ACCEPTED.equals(membership.getStatus()) && !STATUS_PENDING.equals(membership.getStatus())) {
            throw new ForbiddenOperationException("You are not allowed to access this group");
        }
        return toGroupDTO(membership.getShoppingGroup(), membership);
    }

    @Override
    public GroupDTO updateGroup(Long currentUserId, Long groupId, UpdateGroupRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        validateGroupName(request != null ? request.getName() : null);
        GroupMember adminMembership = requireAcceptedAdmin(groupId, currentUserId);

        SbGroup group = adminMembership.getShoppingGroup();
        group.setName(request.getName().trim());
        group.setUpdatedAt(Instant.now());
        group = groupRepository.save(group);

        return toGroupDTO(group, adminMembership);
    }

    @Override
    public GroupDTO regenerateInvitationCode(Long currentUserId, Long groupId) {
        GroupMember adminMembership = requireAcceptedAdmin(groupId, currentUserId);

        SbGroup group = adminMembership.getShoppingGroup();
        group.setInviteCode(generateUniqueInviteCode());
        group.setUpdatedAt(Instant.now());
        group = groupRepository.save(group);

        return toGroupDTO(group, adminMembership);
    }

    @Override
    public GroupMemberDTO joinByCode(Long currentUserId, JoinByCodeRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        String inviteCode = normalizeInviteCode(request != null ? request.getInviteCode() : null);
        if (inviteCode == null) {
            throw new BadRequestException("Invitation code is required");
        }

        SbGroup group = groupRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found for invite code"));

        GroupMember membership = groupMemberRepository
            .findByShoppingGroup_IdAndUser_Id(group.getId(), currentUserId)
            .orElse(null);

        if (membership == null) {
            membership = new GroupMember();
            membership.setShoppingGroup(group);
            membership.setUser(getUser(currentUserId));
            membership.setRole(ROLE_MEMBER);
            membership.setStatus(STATUS_ACCEPTED);
            membership.setJoinedAt(Instant.now());
            membership.setDecidedAt(Instant.now());
            membership = groupMemberRepository.save(membership);
            return toMemberDTO(membership);
        }

        if (STATUS_ACCEPTED.equals(membership.getStatus())) {
            throw new ConflictOperationException("User is already a group member");
        }

        membership.setStatus(STATUS_ACCEPTED);
        membership.setRole(ROLE_MEMBER);
        membership.setDecidedAt(Instant.now());
        membership = groupMemberRepository.save(membership);
        return toMemberDTO(membership);
    }

    @Override
    public GroupMemberDTO requestToJoin(Long currentUserId, Long groupId) {
        SbGroup group = getGroupById(groupId);

        GroupMember existing = groupMemberRepository
            .findByShoppingGroup_IdAndUser_Id(groupId, currentUserId)
            .orElse(null);

        if (existing != null) {
            if (STATUS_PENDING.equals(existing.getStatus())) {
                throw new ConflictOperationException("Join request is already pending");
            }
            if (STATUS_ACCEPTED.equals(existing.getStatus())) {
                throw new ConflictOperationException("User is already a group member");
            }
            existing.setStatus(STATUS_PENDING);
            existing.setRole(ROLE_MEMBER);
            existing.setJoinedAt(Instant.now());
            existing.setDecidedAt(null);
            return toMemberDTO(groupMemberRepository.save(existing));
        }

        GroupMember request = new GroupMember();
        request.setShoppingGroup(group);
        request.setUser(getUser(currentUserId));
        request.setRole(ROLE_MEMBER);
        request.setStatus(STATUS_PENDING);
        request.setJoinedAt(Instant.now());
        request.setDecidedAt(null);
        return toMemberDTO(groupMemberRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberDTO> listJoinRequests(Long currentUserId, Long groupId) {
        requireAcceptedAdmin(groupId, currentUserId);
        return groupMemberRepository.findByShoppingGroup_IdAndStatus(groupId, STATUS_PENDING)
            .stream()
            .sorted(Comparator.comparing(GroupMember::getId))
            .map(this::toMemberDTO)
            .collect(Collectors.toList());
    }

    @Override
    public GroupMemberDTO approveJoinRequest(Long currentUserId, Long groupId, Long requestId) {
        requireAcceptedAdmin(groupId, currentUserId);
        GroupMember request = getMembershipById(requestId);
        ensureMembershipBelongsToGroup(groupId, request);
        if (!STATUS_PENDING.equals(request.getStatus())) {
            throw new ConflictOperationException("Only pending requests can be approved");
        }

        request.setStatus(STATUS_ACCEPTED);
        request.setRole(ROLE_MEMBER);
        request.setDecidedAt(Instant.now());
        return toMemberDTO(groupMemberRepository.save(request));
    }

    @Override
    public GroupMemberDTO rejectJoinRequest(Long currentUserId, Long groupId, Long requestId) {
        requireAcceptedAdmin(groupId, currentUserId);
        GroupMember request = getMembershipById(requestId);
        ensureMembershipBelongsToGroup(groupId, request);
        if (!STATUS_PENDING.equals(request.getStatus())) {
            throw new ConflictOperationException("Only pending requests can be rejected");
        }

        request.setStatus(STATUS_REJECTED);
        request.setDecidedAt(Instant.now());
        return toMemberDTO(groupMemberRepository.save(request));
    }

    @Override
    public void removeMember(Long currentUserId, Long groupId, Long memberUserId) {
        GroupMember adminMembership = requireAcceptedAdmin(groupId, currentUserId);
        GroupMember target = getMembership(groupId, memberUserId);

        if (Objects.equals(target.getUser().getId(), adminMembership.getUser().getId())) {
            throw new BadRequestException("Use leave endpoint for your own membership");
        }

        if (ROLE_ADMIN.equals(target.getRole()) && STATUS_ACCEPTED.equals(target.getStatus())) {
            long adminCount = groupMemberRepository.countByShoppingGroup_IdAndRoleAndStatus(groupId, ROLE_ADMIN, STATUS_ACCEPTED);
            if (adminCount <= 1) {
                throw new ConflictOperationException("Cannot remove the last admin from the group");
            }
        }

        target.setStatus(STATUS_REMOVED);
        target.setDecidedAt(Instant.now());
        groupMemberRepository.save(target);
    }

    @Override
    public void leaveGroup(Long currentUserId, Long groupId) {
        GroupMember membership = getMembership(groupId, currentUserId);
        if (!STATUS_ACCEPTED.equals(membership.getStatus())) {
            throw new ConflictOperationException("Only accepted members can leave a group");
        }

        List<GroupMember> acceptedMembers = groupMemberRepository.findByShoppingGroup_IdAndStatus(groupId, STATUS_ACCEPTED);
        if (acceptedMembers.size() == 1 && Objects.equals(acceptedMembers.get(0).getUser().getId(), currentUserId)) {
            groupRepository.delete(membership.getShoppingGroup());
            return;
        }

        if (ROLE_ADMIN.equals(membership.getRole())) {
            long adminCount = groupMemberRepository.countByShoppingGroup_IdAndRoleAndStatus(groupId, ROLE_ADMIN, STATUS_ACCEPTED);
            if (adminCount <= 1) {
                List<GroupMember> promotionCandidates = acceptedMembers.stream()
                    .filter(member -> !Objects.equals(member.getUser().getId(), currentUserId))
                    .collect(Collectors.toList());

                if (promotionCandidates.isEmpty()) {
                    throw new ConflictOperationException("No eligible member found to promote as admin");
                }

                GroupMember promotedAdmin = promotionCandidates.get(secureRandom.nextInt(promotionCandidates.size()));
                promotedAdmin.setRole(ROLE_ADMIN);
                groupMemberRepository.save(promotedAdmin);

                SbGroup group = membership.getShoppingGroup();
                group.setOwnerUser(promotedAdmin.getUser());
                group.setUpdatedAt(Instant.now());
                groupRepository.save(group);
            }
        }

        membership.setStatus(STATUS_REMOVED);
        membership.setDecidedAt(Instant.now());
        groupMemberRepository.save(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberDTO> listMembers(Long currentUserId, Long groupId) {
        GroupMember requester = getMembership(groupId, currentUserId);
        if (!STATUS_ACCEPTED.equals(requester.getStatus()) && !STATUS_PENDING.equals(requester.getStatus())) {
            throw new ForbiddenOperationException("You are not allowed to view members");
        }

        return groupMemberRepository.findByShoppingGroup_IdAndStatus(groupId, STATUS_ACCEPTED)
            .stream()
            .sorted(Comparator.comparing(GroupMember::getId))
            .map(this::toMemberDTO)
            .collect(Collectors.toList());
    }

    private void validateGroupName(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new BadRequestException("Group name is required");
        }
        if (groupName.trim().length() < 3 || groupName.trim().length() > 100) {
            throw new BadRequestException("Group name must be between 3 and 100 characters");
        }
    }

    private GroupMember requireAcceptedAdmin(Long groupId, Long userId) {
        GroupMember membership = getMembership(groupId, userId);
        if (!STATUS_ACCEPTED.equals(membership.getStatus()) || !ROLE_ADMIN.equals(membership.getRole())) {
            throw new ForbiddenOperationException("Admin access is required for this action");
        }
        return membership;
    }

    private GroupMember getMembership(Long groupId, Long userId) {
        return groupMemberRepository.findByShoppingGroup_IdAndUser_Id(groupId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));
    }

    private GroupMember getMembershipById(Long requestId) {
        if (requestId == null) {
            throw new BadRequestException("Request id is required");
        }
        return groupMemberRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Join request not found"));
    }

    private SbGroup getGroupById(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group id is required");
        }
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }

    private User getUser(Long userId) {
        if (userId == null) {
            throw new BadRequestException("User id is required");
        }
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void ensureMembershipBelongsToGroup(Long groupId, GroupMember member) {
        if (!Objects.equals(groupId, member.getShoppingGroup().getId())) {
            throw new ResourceNotFoundException("Join request not found in this group");
        }
    }

    private String generateUniqueInviteCode() {
        for (int i = 0; i < 20; i++) {
            String candidate = randomInviteCode();
            if (!groupRepository.findByInviteCode(candidate).isPresent()) {
                return candidate;
            }
        }
        throw new ConflictOperationException("Failed to generate a unique invitation code");
    }

    private String randomInviteCode() {
        StringBuilder code = new StringBuilder(INVITE_LEN);
        for (int i = 0; i < INVITE_LEN; i++) {
            int idx = secureRandom.nextInt(INVITE_ALPHABET.length());
            code.append(INVITE_ALPHABET.charAt(idx));
        }
        return code.toString();
    }

    private String normalizeInviteCode(String inviteCode) {
        if (inviteCode == null) {
            return null;
        }
        String normalized = inviteCode.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private GroupDTO toGroupDTO(SbGroup group, GroupMember myMembership) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setInviteCode(group.getInviteCode());
        dto.setOwnerUserId(group.getOwnerUser().getId());
        dto.setMyRole(myMembership.getRole());
        dto.setMyStatus(myMembership.getStatus());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());
        return dto;
    }

    private GroupMemberDTO toMemberDTO(GroupMember member) {
        GroupMemberDTO dto = new GroupMemberDTO();
        dto.setId(member.getId());
        dto.setGroupId(member.getShoppingGroup().getId());
        dto.setUserId(member.getUser().getId());
        dto.setUserEmail(member.getUser().getEmail());
        dto.setRole(member.getRole());
        dto.setStatus(member.getStatus());
        dto.setJoinedAt(member.getJoinedAt());
        dto.setDecidedAt(member.getDecidedAt());
        return dto;
    }
}
