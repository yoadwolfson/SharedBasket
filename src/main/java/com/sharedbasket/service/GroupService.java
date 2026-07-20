package com.sharedbasket.service;

import com.sharedbasket.dto.CreateGroupRequestDTO;
import com.sharedbasket.dto.GroupDTO;
import com.sharedbasket.dto.GroupMemberDTO;
import com.sharedbasket.dto.JoinByCodeRequestDTO;
import com.sharedbasket.dto.UpdateGroupRequestDTO;

import java.util.List;

public interface GroupService {

    GroupDTO createGroup(Long currentUserId, CreateGroupRequestDTO request);

    List<GroupDTO> listMyGroups(Long currentUserId);

    GroupDTO getGroup(Long currentUserId, Long groupId);

    GroupDTO updateGroup(Long currentUserId, Long groupId, UpdateGroupRequestDTO request);

    GroupDTO regenerateInvitationCode(Long currentUserId, Long groupId);

    GroupMemberDTO joinByCode(Long currentUserId, JoinByCodeRequestDTO request);

    GroupMemberDTO requestToJoin(Long currentUserId, Long groupId);

    List<GroupMemberDTO> listJoinRequests(Long currentUserId, Long groupId);

    GroupMemberDTO approveJoinRequest(Long currentUserId, Long groupId, Long requestId);

    GroupMemberDTO rejectJoinRequest(Long currentUserId, Long groupId, Long requestId);

    void removeMember(Long currentUserId, Long groupId, Long memberUserId);

    void leaveGroup(Long currentUserId, Long groupId);

    List<GroupMemberDTO> listMembers(Long currentUserId, Long groupId);
}
