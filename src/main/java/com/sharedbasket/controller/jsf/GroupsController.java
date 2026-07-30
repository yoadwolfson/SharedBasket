package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.AlertDTO;
import com.sharedbasket.dto.CreateGroupRequestDTO;
import com.sharedbasket.dto.GroupDTO;
import com.sharedbasket.dto.GroupMemberDTO;
import com.sharedbasket.dto.JoinByCodeRequestDTO;
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

    private List<AlertDTO> alerts;

    @PostConstruct
    public void init() {
        alerts = new ArrayList<>();
        groups = new ArrayList<>();
        selectedMembers = new ArrayList<>();
        selectedPendingRequests = new ArrayList<>();
        restTemplate = resolveSpringBean(RestTemplate.class);
        reloadGroups();
    }

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
            addError(extractErrorMessage(ex));
            selectedMembers = Collections.emptyList();
            selectedPendingRequests = Collections.emptyList();
        }
    }

    private HttpHeaders buildJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
            .getExternalContext()
            .getRequest();
        String cookie = request.getHeader("Cookie");
        if (cookie != null && !cookie.trim().isEmpty()) {
            headers.add(HttpHeaders.COOKIE, cookie);
        }
        return headers;
    }

    private String groupsApiBaseUrl() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
            .getExternalContext()
            .getRequest();

        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();

        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
            || ("https".equalsIgnoreCase(scheme) && port == 443);
        String base = scheme + "://" + host + (defaultPort ? "" : ":" + port) + contextPath;
        return base + "/api/groups";
    }

    private String extractErrorMessage(Exception ex) {
        if (ex instanceof HttpStatusCodeException) {
            String body = ((HttpStatusCodeException) ex).getResponseBodyAsString();
            if (body != null && !body.trim().isEmpty()) {
                return body;
            }
        }
        return ex.getMessage();
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
