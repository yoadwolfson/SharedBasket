package com.sharedbasket.controller;

import com.sharedbasket.dto.CreateGroupRequestDTO;
import com.sharedbasket.dto.GroupDTO;
import com.sharedbasket.dto.GroupMemberDTO;
import com.sharedbasket.dto.JoinByCodeRequestDTO;
import com.sharedbasket.dto.UpdateGroupRequestDTO;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.service.AuthenticationService;
import com.sharedbasket.service.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final AuthenticationService authenticationService;

    public GroupController(GroupService groupService, AuthenticationService authenticationService) {
        this.groupService = groupService;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupDTO createGroup(@RequestBody CreateGroupRequestDTO request) {
        return groupService.createGroup(getCurrentUserId(), request);
    }

    @GetMapping
    public List<GroupDTO> listGroups() {
        return groupService.listMyGroups(getCurrentUserId());
    }

    @GetMapping("/{groupId}")
    public GroupDTO getGroup(@PathVariable Long groupId) {
        return groupService.getGroup(getCurrentUserId(), groupId);
    }

    @PutMapping("/{groupId}")
    public GroupDTO updateGroup(@PathVariable Long groupId, @RequestBody UpdateGroupRequestDTO request) {
        return groupService.updateGroup(getCurrentUserId(), groupId, request);
    }

    @PostMapping("/{groupId}/invitation-code/regenerate")
    public GroupDTO regenerateInvitationCode(@PathVariable Long groupId) {
        return groupService.regenerateInvitationCode(getCurrentUserId(), groupId);
    }

    @PostMapping("/join-by-code")
    public GroupMemberDTO joinByCode(@RequestBody JoinByCodeRequestDTO request) {
        return groupService.joinByCode(getCurrentUserId(), request);
    }

    @PostMapping("/{groupId}/join-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupMemberDTO requestToJoin(@PathVariable Long groupId) {
        return groupService.requestToJoin(getCurrentUserId(), groupId);
    }

    @GetMapping("/{groupId}/join-requests")
    public List<GroupMemberDTO> listJoinRequests(@PathVariable Long groupId) {
        return groupService.listJoinRequests(getCurrentUserId(), groupId);
    }

    @PostMapping("/{groupId}/join-requests/{requestId}/approve")
    public GroupMemberDTO approveJoinRequest(@PathVariable Long groupId, @PathVariable Long requestId) {
        return groupService.approveJoinRequest(getCurrentUserId(), groupId, requestId);
    }

    @PostMapping("/{groupId}/join-requests/{requestId}/reject")
    public GroupMemberDTO rejectJoinRequest(@PathVariable Long groupId, @PathVariable Long requestId) {
        return groupService.rejectJoinRequest(getCurrentUserId(), groupId, requestId);
    }

    @DeleteMapping("/{groupId}/members/{memberUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable Long groupId, @PathVariable Long memberUserId) {
        groupService.removeMember(getCurrentUserId(), groupId, memberUserId);
    }

    @PostMapping("/{groupId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(@PathVariable Long groupId) {
        groupService.leaveGroup(getCurrentUserId(), groupId);
    }

    @GetMapping("/{groupId}/members")
    public List<GroupMemberDTO> listMembers(@PathVariable Long groupId) {
        return groupService.listMembers(getCurrentUserId(), groupId);
    }

    private Long getCurrentUserId() {
        if (authenticationService == null || authenticationService.getCurrentUser() == null || authenticationService.getCurrentUser().getId() == null) {
            throw new ForbiddenOperationException("No authenticated user in session");
        }
        return authenticationService.getCurrentUser().getId();
    }
}
