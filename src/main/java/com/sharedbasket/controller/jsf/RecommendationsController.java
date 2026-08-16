package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.AlertDTO;
import com.sharedbasket.dto.RebuildRequestDTO;
import com.sharedbasket.dto.RebuildResultDTO;
import com.sharedbasket.dto.RecommendationDTO;
import com.sharedbasket.util.JsfRestClientSupport;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import javax.servlet.ServletContext;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ManagedBean(name = "recommendationsController")
@ViewScoped
public class RecommendationsController implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient RestTemplate restTemplate;

    @ManagedProperty(value = "#{shellController}")
    private ShellController shellController;

    private List<RecommendationDTO> recommendations;
    private Map<String, Boolean> selectedMap;
    private List<AlertDTO> alerts;
    private boolean loading;

    @PostConstruct
    public void init() {
        alerts = new ArrayList<>();
        recommendations = Collections.emptyList();
        selectedMap = new HashMap<>();
        restTemplate = resolveSpringBean(RestTemplate.class);
        reloadRecommendations();
    }

    /**
     * Reloads recommendations for the current group.
     */
    public void reloadRecommendations() {
        alerts.clear();
        loading = true;
        Long groupId = resolveGroupId();
        if (groupId == null) {
            addError("No group available for recommendations");
            recommendations = Collections.emptyList();
            loading = false;
            return;
        }
        try {
            ResponseEntity<List<RecommendationDTO>> response = restTemplate.exchange(
                URI.create(JsfRestClientSupport.apiBaseUrl("/api/recommendations") + "?groupId=" + groupId),
                HttpMethod.GET,
                new HttpEntity<>(buildJsonHeaders()),
                new ParameterizedTypeReference<List<RecommendationDTO>>() {}
            );
            recommendations = response.getBody() == null ? Collections.emptyList() : response.getBody();
        } catch (Exception ex) {
            addError(extractErrorMessage(ex));
            recommendations = Collections.emptyList();
        } finally {
            selectedMap.keySet().retainAll(
                recommendations.stream().map(RecommendationDTO::getItemName).collect(Collectors.toSet()));
            loading = false;
        }
    }

    public Map<String, Boolean> getSelectedMap() { return selectedMap; }

    /**
     * Rebuilds the shopping list from the currently selected recommendations if needed.
     */
    public void rebuildSelected() {
        alerts.clear();
        Long groupId = resolveGroupId();
        if (groupId == null) {
            addError("No group available for rebuild");
            return;
        }
        List<String> selectedItemNames = selectedMap.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        if (selectedItemNames.isEmpty()) {
            addError("Select at least one recommendation to rebuild");
            return;
        }
        try {
            RebuildRequestDTO request = new RebuildRequestDTO();
            request.setGroupId(groupId);
            request.setItemNames(selectedItemNames);

            ResponseEntity<RebuildResultDTO> response = restTemplate.exchange(
                URI.create(JsfRestClientSupport.apiBaseUrl("/api/recommendations") + "/rebuild"),
                HttpMethod.POST,
                new HttpEntity<>(request, buildJsonHeaders()),
                RebuildResultDTO.class
            );
            RebuildResultDTO result = response.getBody();
            if (result != null) {
                addSuccess("Added " + result.getCreatedItemCount() + " item(s) to the shopping list"
                    + (result.getSkippedItemCount() > 0 ? "; skipped " + result.getSkippedItemCount() + " already on the list" : ""));
            }
            selectedMap.clear();
            reloadRecommendations();
        } catch (Exception ex) {
            addError(extractErrorMessage(ex));
        }
    }
    /**
     * Resolves the current group ID from the ShellController.
     * @return the current group ID or null if not available
     */
    private Long resolveGroupId() {
        ShellController shell = resolveShellController();
        return shell == null ? null : shell.getCurrentGroupId();
    }
    /**
     * Resolves the ShellController bean from the JSF context.
     * @return the ShellController instance or null if not available
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
     * Builds HTTP headers for JSON requests.
     * @return HttpHeaders with Content-Type set to application/json
     */
    private HttpHeaders buildJsonHeaders() {
        return JsfRestClientSupport.buildJsonHeaders();
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

    private void addSuccess(String message) {
        alerts.add(new AlertDTO("success", message));
    }

    private void addError(String message) {
        alerts.add(new AlertDTO("error", message));
    }

    private <T> T resolveSpringBean(Class<T> beanType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
        WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        return applicationContext.getBean(beanType);
    }

    // Getters and setters
    public List<RecommendationDTO> getRecommendations() { return recommendations; }
    public List<AlertDTO> getAlerts() { return alerts; }
    public boolean isLoading() { return loading; }
    public ShellController getShellController() { return shellController; }
    public void setShellController(ShellController shellController) { this.shellController = shellController; }
}
