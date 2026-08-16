package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.AlertDTO;
import com.sharedbasket.dto.CreateGroupRequestDTO;
import com.sharedbasket.dto.GroupDTO;
import com.sharedbasket.dto.GroupMemberDTO;
import com.sharedbasket.dto.JoinByCodeRequestDTO;
import com.sharedbasket.util.JsfRestClientSupport;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ManagedBean(name = "groupsController")
@ViewScoped
public class GroupsController implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient RestTemplate restTemplate;

    @ManagedProperty(value = "#{shellController}")
    private ShellController shellController;

    private List<GroupDTO> groups;
    private List<GroupMemberDTO> selectedMembers;
    private List<GroupMemberDTO> selectedPendingRequests;
    private Long selectedGroupId;

    private String createGroupName;
    private String joinInviteCode;
    private String requestGroupId;

    private String lastPollSignature;
    private String pollRenderTargets = "";

    private List<AlertDTO> alerts;
    /**
     * Initializes the groups controller by setting up initial data and loading groups.
     */
    @PostConstruct
    public void init() {
        alerts = new ArrayList<>();
        groups = new ArrayList<>();
        selectedMembers = new ArrayList<>();
        selectedPendingRequests = new ArrayList<>();
        restTemplate = resolveSpringBean(RestTemplate.class);
        reloadGroups();
    }
    /**
     * Creates a new group with the specified name.
     */
    public void createGroup() {
        alerts.clear();
        try {
            CreateGroupRequestDTO request = new CreateGroupRequestDTO(createGroupName);
            restTemplate.exchange(
                groupsApiBaseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request, buildJsonHeaders()),
                GroupDTO.class
            );
            createGroupName = null;
            addSuccess("Group created successfully");
            reloadGroups();
        } catch (Exception ex) {
            addError(extractErrorMessage(ex));
        }
    }
    /**
     * Joins a group using an invite code.
     */
    public void joinByCode() {
        alerts.clear();
        try {
            JoinByCodeRequestDTO request = new JoinByCodeRequestDTO(joinInviteCode);
            restTemplate.exchange(
                groupsApiBaseUrl() + "/join-by-code",
                HttpMethod.POST,
                new HttpEntity<>(request, buildJsonHeaders()),
                GroupMemberDTO.class
            );
            joinInviteCode = null;
            addSuccess("Joined group successfully");
            reloadGroups();
        } catch (Exception ex) {
            addError(extractErrorMessage(ex));
        }
    }
    /**
     * Requests to join a group.
     */
    public void requestToJoin() {
        alerts.clear();
        try {
            Long groupId = parseId(requestGroupId);
            restTemplate.exchange(
                groupsApiBaseUrl() + "/" + groupId + "/join-requests",
                HttpMethod.POST,
                new HttpEntity<>(buildJsonHeaders()),
                GroupMemberDTO.class
            );
            requestGroupId = null;
            addSuccess("Join request submitted");
        } catch (Exception ex) {
            addError(extractErrorMessage(ex));
        }
    }
    /**
     * Selects a group by its ID and reloads its data.
     * @param groupId The ID of the group to select.
     */
    public void selectGroup(Long groupId) {
        alerts.clear();
        this.selectedGroupId = groupId;
        reloadSelectedGroupData();
    }
    /**
     * Approves a join request for the selected group.
     * @param requestId The ID of the join request to approve.
     */
    public void approveRequest(Long requestId) {
        if (selectedGroupId == null) {
            addError("Select a group first");
            return;
        }
        try {
            restTemplate.exchange(
                groupsApiBaseUrl() + "/" + selectedGroupId + "/join-requests/" + requestId + "/approve",
                HttpMethod.POST,
                new HttpEntity<>(buildJsonHeaders()),
                GroupMemberDTO.class
            );
            addSuccess("Join request approved");
            reloadSelectedGroupData();
        } catch (Exception ex) {
            addError(extractErrorMessage(ex));
        }
    }

    /**
     * Rejects a join request for the selected group.
     * @param requestId The ID of the join request to reject.
     */
    public void rejectRequest(Long requestId) {
        if (selectedGroupId == null) {
            addError("Select a group first");
            return;
        }
        try {
            restTemplate.exchange(
                groupsApiBaseUrl() + "/" + selectedGroupId + "/join-requests/" + requestId + "/reject",
                HttpMethod.POST,
                new HttpEntity<>(buildJsonHeaders()),
                GroupMemberDTO.class
            );
            addSuccess("Join request rejected");
            reloadSelectedGroupData();
        } catch (Exception ex) {
            addError(extractErrorMessage(ex));
        }
    }
    /**
     * Removes a member from the selected group.
     * @param memberUserId The user ID of the member to remove.
     */
    public void removeMember(Long memberUserId) {
        if (selectedGroupId == null) {
            addError("Select a group first");
            return;
        }
        try {
            restTemplate.exchange(
                groupsApiBaseUrl() + "/" + selectedGroupId + "/members/" + memberUserId,
                HttpMethod.DELETE,
                new HttpEntity<>(buildJsonHeaders()),
                Void.class
            );
            addSuccess("Member removed from group");
            reloadSelectedGroupData();
        } catch (Exception ex) {
            addError(extractErrorMessage(ex));
        }
    }
    /**
     * Leaves the selected group.
     */
    public void leaveSelectedGroup() {
        alerts.clear();
        if (selectedGroupId == null) {
            addError("Select a group first");
            return;
        }
        try {
            restTemplate.exchange(
                groupsApiBaseUrl() + "/" + selectedGroupId + "/leave",
                HttpMethod.POST,
                new HttpEntity<>(buildJsonHeaders()),
                Void.class
            );
            selectedGroupId = null;
            selectedMembers = Collections.emptyList();
            selectedPendingRequests = Collections.emptyList();
            addSuccess("You left the group");
            reloadGroups();
        } catch (Exception ex) {
            addError(extractErrorMessage(ex));
        }
    }
    /**
     * Regenerates the invitation code for the selected group.
     */
    public void regenerateInviteCode() {
        alerts.clear();
        if (selectedGroupId == null) {
            addError("Select a group first");
            return;
        }
        try {
            restTemplate.exchange(
                groupsApiBaseUrl() + "/" + selectedGroupId + "/invitation-code/regenerate",
                HttpMethod.POST,
                new HttpEntity<>(buildJsonHeaders()),
                GroupDTO.class
            );
            addSuccess("Invitation code regenerated");
            reloadGroups();
            reloadSelectedGroupData();
        } catch (Exception ex) {
            addError(extractErrorMessage(ex));
        }
    }
    /**
     * Checks if the currently selected group has the user as an admin with accepted status.
     * @return true if the user is an admin of the selected group and has accepted status, false otherwise.
     */
    public boolean isSelectedGroupAdmin() {
        if (selectedGroupId == null) {
            return false;
        }
        GroupDTO selected = getSelectedGroup();
        return selected != null && "ADMIN".equals(selected.getMyRole()) && "ACCEPTED".equals(selected.getMyStatus());
    }
    /**
     * Checks whether the given member user ID belongs to the currently logged-in user.
     * @param memberUserId The user ID of the member to check.
     * @return true if the member is the current user, false otherwise.
     */
    public boolean isCurrentUser(Long memberUserId) {
        ShellController shell = resolveShellController();
        return shell != null
            && shell.getCurrentUser() != null
            && shell.getCurrentUser().getId() != null
            && shell.getCurrentUser().getId().equals(memberUserId);
    }
    /**
     * Resolves the session-scoped ShellController bean directly via EL, since @ManagedProperty
     * injection is not populated by this app's JSF managed-bean setup.
     */
    private ShellController resolveShellController() {
        if (shellController != null) {
            return shellController;
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }
        return facesContext.getApplication()
            .evaluateExpressionGet(facesContext, "#{shellController}", ShellController.class);
    }
    /**
     * Gets the currently selected group based on the selectedGroupId.
     * @return The GroupDTO of the selected group, or null if no group is selected or found.
     */
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
    /**
     * Silently refreshes groups and the selected group's data for background ajax polling.
     */
    public void pollRefresh() {
        reloadGroups();
        String signature = pollSignature();
        // Only ask the client to replace the DOM when something actually changed, otherwise a
        // periodic no-op render can swallow a user's click on a button mid-swap.
        pollRenderTargets = signature.equals(lastPollSignature) ? "" : ":alertsPanel :groupsListPanel :groupDetailPanel";
        lastPollSignature = signature;
    }

    /**
     * Builds a lightweight signature of the currently loaded groups/members/requests/alerts,
     * used to detect whether a background poll actually changed anything worth rendering.
     */
    private String pollSignature() {
        StringBuilder sb = new StringBuilder();
        for (GroupDTO group : groups) {
            sb.append("g:").append(group.getId()).append(':').append(group.getMyRole())
                .append(':').append(group.getMyStatus()).append(':').append(group.getInviteCode()).append(';');
        }
        for (GroupMemberDTO member : selectedMembers) {
            sb.append("m:").append(member.getId()).append(':').append(member.getRole())
                .append(':').append(member.getStatus()).append(';');
        }
        for (GroupMemberDTO request : selectedPendingRequests) {
            sb.append("r:").append(request.getId()).append(':').append(request.getStatus()).append(';');
        }
        sb.append("a:").append(alerts.size());
        return sb.toString();
    }
    /**
     * Reloads the list of groups from the API.
     */
    private void reloadGroups() {
        try {
            ResponseEntity<List<GroupDTO>> response = restTemplate.exchange(
                groupsApiBaseUrl(),
                HttpMethod.GET,
                new HttpEntity<>(buildJsonHeaders()),
                new ParameterizedTypeReference<List<GroupDTO>>() {}
            );
            groups = response.getBody() == null ? new ArrayList<>() : response.getBody();
            if (selectedGroupId != null) {
                reloadSelectedGroupData();
            }
        } catch (Exception ex) {
            addError(extractErrorMessage(ex));
            groups = new ArrayList<>();
        }
    }
    /**
     * Reloads the data for the currently selected group from the API.
     */
    private void reloadSelectedGroupData() {
        if (selectedGroupId == null) {
            selectedMembers = Collections.emptyList();
            selectedPendingRequests = Collections.emptyList();
            return;
        }
        try {
            ResponseEntity<List<GroupMemberDTO>> membersResponse = restTemplate.exchange(
                groupsApiBaseUrl() + "/" + selectedGroupId + "/members",
                HttpMethod.GET,
                new HttpEntity<>(buildJsonHeaders()),
                new ParameterizedTypeReference<List<GroupMemberDTO>>() {}
            );
            selectedMembers = membersResponse.getBody() == null ? Collections.emptyList() : membersResponse.getBody();
            if (isSelectedGroupAdmin()) {
                ResponseEntity<List<GroupMemberDTO>> requestsResponse = restTemplate.exchange(
                    groupsApiBaseUrl() + "/" + selectedGroupId + "/join-requests",
                    HttpMethod.GET,
                    new HttpEntity<>(buildJsonHeaders()),
                    new ParameterizedTypeReference<List<GroupMemberDTO>>() {}
                );
                selectedPendingRequests = requestsResponse.getBody() == null
                    ? Collections.emptyList()
                    : requestsResponse.getBody();
            } else {
                selectedPendingRequests = Collections.emptyList();
            }
        } catch (Exception ex) {
            if (isMembershipLost(ex)) {
                selectedGroupId = null;
                selectedMembers = Collections.emptyList();
                selectedPendingRequests = Collections.emptyList();
                reloadGroups();
                return;
            }
            addError(extractErrorMessage(ex));
            selectedMembers = Collections.emptyList();
            selectedPendingRequests = Collections.emptyList();
        }
    }

    private boolean isMembershipLost(Exception ex) {
        if (!(ex instanceof HttpStatusCodeException)) {
            return false;
        }
        int statusCode = ((HttpStatusCodeException) ex).getRawStatusCode();
        return statusCode == 403 || statusCode == 404;
    }
    /**
     * Builds HTTP headers for JSON requests, including content type, accept type, and cookies from the current request.
     * @return HttpHeaders configured for JSON requests.
     */
    private HttpHeaders buildJsonHeaders() {
        return JsfRestClientSupport.buildJsonHeaders();
    }
    /**
     * Constructs the base URL for the groups API based on the current request's scheme, host, port, and context path.
     * @return The base URL for the groups API.
     */
    private String groupsApiBaseUrl() {
        return JsfRestClientSupport.apiBaseUrl("/api/groups");
    }
    /**
     * Extracts a user-friendly error message from an exception, particularly handling HttpStatusCodeException to retrieve the response body if available.
     * @param ex The exception from which to extract the error message.
     * @return A string containing the error message, either from the response body or the exception's message.
     */
    private String extractErrorMessage(Exception ex) {
        if (ex instanceof HttpStatusCodeException) {
            String body = ((HttpStatusCodeException) ex).getResponseBodyAsString();
            if (body != null && !body.trim().isEmpty()) {
                return body;
            }
        }
        return ex.getMessage();
    }
    /**
     * Parses a string to a Long ID, throwing an IllegalArgumentException if the input is null, empty, or not a valid number.
     * @param raw The raw string to parse as a Long ID.
     * @return The parsed Long ID.
     * @throws IllegalArgumentException if the input is null, empty, or not a valid number.
     */
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
    /**
     * Adds a success alert message to the alerts list.
     * @param message The success message to add.
     */
    private void addSuccess(String message) {
        alerts.add(new AlertDTO("success", message));
    }
    /**
     * Adds an error alert message to the alerts list.
     * @param message The error message to add.
     */
    private void addError(String message) {
        alerts.add(new AlertDTO("error", message));
    }
 //   Getters and Setters
    public List<GroupDTO> getGroups() {return groups;}

    public String getPollRenderTargets() {return pollRenderTargets;}

    public List<GroupMemberDTO> getSelectedMembers() {return selectedMembers;}

    public List<GroupMemberDTO> getSelectedPendingRequests() {return selectedPendingRequests;}

    public Long getSelectedGroupId() {return selectedGroupId;}

    public void setSelectedGroupId(Long selectedGroupId) {this.selectedGroupId = selectedGroupId;}

    public String getCreateGroupName() {return createGroupName;}

    public void setCreateGroupName(String createGroupName) {this.createGroupName = createGroupName;}

    public String getJoinInviteCode() {return joinInviteCode;}

    public void setJoinInviteCode(String joinInviteCode) {this.joinInviteCode = joinInviteCode;}

    public String getRequestGroupId() {return requestGroupId;}

    public void setRequestGroupId(String requestGroupId) {this.requestGroupId = requestGroupId;}

    public List<AlertDTO> getAlerts() {return alerts;}

    public ShellController getShellController() {return shellController;}

    public void setShellController(ShellController shellController) {this.shellController = shellController;}

    /**
     * Resolves a Spring bean of the specified type from the current web application context.
     * @param <T> The type of the bean to resolve.
     * @param beanType The class type of the bean to resolve.
     * @return An instance of the requested Spring bean.
     */
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
