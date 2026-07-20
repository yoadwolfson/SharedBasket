package com.sharedbasket.controller;

import com.sharedbasket.dto.AlertDTO;
import com.sharedbasket.dto.CreateGroupRequestDTO;
import com.sharedbasket.dto.GroupDTO;
import com.sharedbasket.dto.GroupMemberDTO;
import com.sharedbasket.dto.JoinByCodeRequestDTO;
import com.sharedbasket.service.GroupService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ManagedBean(name = "groupsController")
@ViewScoped
public class GroupsController implements Serializable {

    private static final long serialVersionUID = 1L;

    private GroupService groupService;

    @ManagedProperty(value = "#{shellController}")
    private ShellController shellController;

    private List<GroupDTO> groups;
    private List<GroupMemberDTO> selectedMembers;
    private List<GroupMemberDTO> selectedPendingRequests;
    private Long selectedGroupId;

    private String createGroupName;
    private String joinInviteCode;
    private String requestGroupId;

    private List<AlertDTO> alerts;

    @PostConstruct
    public void init() {
        alerts = new ArrayList<>();
        groups = new ArrayList<>();
        selectedMembers = new ArrayList<>();
        selectedPendingRequests = new ArrayList<>();
        groupService = resolveSpringBean(GroupService.class);
        reloadGroups();
    }

    public void createGroup() {
        alerts.clear();
        try {
            Long currentUserId = getCurrentUserId();
            groupService.createGroup(currentUserId, new CreateGroupRequestDTO(createGroupName));
            createGroupName = null;
            addSuccess("Group created successfully");
            reloadGroups();
        } catch (Exception ex) {
            addError(ex.getMessage());
        }
    }

    public void joinByCode() {
        alerts.clear();
        try {
            Long currentUserId = getCurrentUserId();
            groupService.joinByCode(currentUserId, new JoinByCodeRequestDTO(joinInviteCode));
            joinInviteCode = null;
            addSuccess("Joined group successfully");
            reloadGroups();
        } catch (Exception ex) {
            addError(ex.getMessage());
        }
    }

    public void requestToJoin() {
        alerts.clear();
        try {
            Long groupId = parseId(requestGroupId);
            Long currentUserId = getCurrentUserId();
            groupService.requestToJoin(currentUserId, groupId);
            requestGroupId = null;
            addSuccess("Join request submitted");
        } catch (Exception ex) {
            addError(ex.getMessage());
        }
    }

    public void selectGroup(Long groupId) {
        alerts.clear();
        this.selectedGroupId = groupId;
        reloadSelectedGroupData();
    }

    public void approveRequest(Long requestId) {
        if (selectedGroupId == null) {
            addError("Select a group first");
            return;
        }
        try {
            groupService.approveJoinRequest(getCurrentUserId(), selectedGroupId, requestId);
            addSuccess("Join request approved");
            reloadSelectedGroupData();
        } catch (Exception ex) {
            addError(ex.getMessage());
        }
    }

    public void rejectRequest(Long requestId) {
        if (selectedGroupId == null) {
            addError("Select a group first");
            return;
        }
        try {
            groupService.rejectJoinRequest(getCurrentUserId(), selectedGroupId, requestId);
            addSuccess("Join request rejected");
            reloadSelectedGroupData();
        } catch (Exception ex) {
            addError(ex.getMessage());
        }
    }

    public void removeMember(Long memberUserId) {
        if (selectedGroupId == null) {
            addError("Select a group first");
            return;
        }
        try {
            groupService.removeMember(getCurrentUserId(), selectedGroupId, memberUserId);
            addSuccess("Member removed from group");
            reloadSelectedGroupData();
        } catch (Exception ex) {
            addError(ex.getMessage());
        }
    }

    public void leaveSelectedGroup() {
        alerts.clear();
        if (selectedGroupId == null) {
            addError("Select a group first");
            return;
        }
        try {
            groupService.leaveGroup(getCurrentUserId(), selectedGroupId);
            selectedGroupId = null;
            selectedMembers = Collections.emptyList();
            selectedPendingRequests = Collections.emptyList();
            addSuccess("You left the group");
            reloadGroups();
        } catch (Exception ex) {
            addError(ex.getMessage());
        }
    }

    public void regenerateInviteCode() {
        alerts.clear();
        if (selectedGroupId == null) {
            addError("Select a group first");
            return;
        }
        try {
            groupService.regenerateInvitationCode(getCurrentUserId(), selectedGroupId);
            addSuccess("Invitation code regenerated");
            reloadGroups();
            reloadSelectedGroupData();
        } catch (Exception ex) {
            addError(ex.getMessage());
        }
    }

    public boolean isSelectedGroupAdmin() {
        if (selectedGroupId == null) {
            return false;
        }
        GroupDTO selected = getSelectedGroup();
        return selected != null && "ADMIN".equals(selected.getMyRole()) && "ACCEPTED".equals(selected.getMyStatus());
    }

    public GroupDTO getSelectedGroup() {
        if (selectedGroupId == null) {
            return null;
        }
        for (GroupDTO group : groups) {
            if (selectedGroupId.equals(group.getId())) {
                return group;
            }
        }
        return null;
    }

    private void reloadGroups() {
        try {
            groups = groupService.listMyGroups(getCurrentUserId());
            if (selectedGroupId != null) {
                reloadSelectedGroupData();
            }
        } catch (Exception ex) {
            addError(ex.getMessage());
            groups = new ArrayList<>();
        }
    }

    private void reloadSelectedGroupData() {
        if (selectedGroupId == null) {
            selectedMembers = Collections.emptyList();
            selectedPendingRequests = Collections.emptyList();
            return;
        }
        try {
            selectedMembers = groupService.listMembers(getCurrentUserId(), selectedGroupId);
            if (isSelectedGroupAdmin()) {
                selectedPendingRequests = groupService.listJoinRequests(getCurrentUserId(), selectedGroupId);
            } else {
                selectedPendingRequests = Collections.emptyList();
            }
        } catch (Exception ex) {
            addError(ex.getMessage());
            selectedMembers = Collections.emptyList();
            selectedPendingRequests = Collections.emptyList();
        }
    }

    private Long getCurrentUserId() {
        if (shellController == null || shellController.getCurrentUser() == null || shellController.getCurrentUser().getId() == null) {
            throw new IllegalStateException("No authenticated user in session");
        }
        return shellController.getCurrentUser().getId();
    }

    private Long parseId(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Group ID is required");
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Group ID must be a number");
        }
    }

    private void addSuccess(String message) {
        alerts.add(new AlertDTO("success", message));
    }

    private void addError(String message) {
        alerts.add(new AlertDTO("error", message));
    }

    public List<GroupDTO> getGroups() {
        return groups;
    }

    public List<GroupMemberDTO> getSelectedMembers() {
        return selectedMembers;
    }

    public List<GroupMemberDTO> getSelectedPendingRequests() {
        return selectedPendingRequests;
    }

    public Long getSelectedGroupId() {
        return selectedGroupId;
    }

    public void setSelectedGroupId(Long selectedGroupId) {
        this.selectedGroupId = selectedGroupId;
    }

    public String getCreateGroupName() {
        return createGroupName;
    }

    public void setCreateGroupName(String createGroupName) {
        this.createGroupName = createGroupName;
    }

    public String getJoinInviteCode() {
        return joinInviteCode;
    }

    public void setJoinInviteCode(String joinInviteCode) {
        this.joinInviteCode = joinInviteCode;
    }

    public String getRequestGroupId() {
        return requestGroupId;
    }

    public void setRequestGroupId(String requestGroupId) {
        this.requestGroupId = requestGroupId;
    }

    public List<AlertDTO> getAlerts() {
        return alerts;
    }

    public ShellController getShellController() {
        return shellController;
    }

    public void setShellController(ShellController shellController) {
        this.shellController = shellController;
    }

    private <T> T resolveSpringBean(Class<T> beanType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            throw new IllegalStateException("FacesContext is not available");
        }

        ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
        WebApplicationContext applicationContext =
            WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        return applicationContext.getBean(beanType);
    }
}
