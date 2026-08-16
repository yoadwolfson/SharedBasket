package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.AlertDTO;
import com.sharedbasket.dto.RecommendationDTO;
import com.sharedbasket.dto.ShoppingItemDTO;
import com.sharedbasket.dto.StatisticsDTO;
import com.sharedbasket.util.FrontendDtoAdapter;
import com.sharedbasket.util.JsfRestClientSupport;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
import java.util.List;

@ManagedBean(name = "dashboardController")
@ViewScoped
public class DashboardController implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient RestTemplate restTemplate;

    @ManagedProperty(value = "#{shellController}")
    private ShellController shellController;

    private List<ShoppingItemDTO> shoppingItems;
    private StatisticsDTO statistics;
    private List<RecommendationDTO> recommendations;
    private List<AlertDTO> alerts;
    /**
     * Initializes the dashboard controller by loading initial data and setting up alerts.
     */
    @PostConstruct
    public void init() {
        alerts = new ArrayList<>();
        restTemplate = resolveSpringBean(RestTemplate.class);
        loadDashboardData();
    }
    /**
     * Loads the dashboard data including shopping items, statistics, and recommendations.
     */
    private void loadDashboardData() {
        // Shopping items are still placeholder data until the shopping-item REST API exists.
        shoppingItems = FrontendDtoAdapter.createPlaceholderShoppingItems();

        Long groupId = resolveGroupId();
        if (groupId == null) {
            statistics = null;
            recommendations = Collections.emptyList();
            addErrorAlert("No group available for statistics and recommendations");
            return;
        }

        try {
            statistics = restTemplate.exchange(
                URI.create(JsfRestClientSupport.apiBaseUrl("/api/reports/statistics") + "?groupId=" + groupId),
                HttpMethod.GET,
                new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()),
                StatisticsDTO.class
            ).getBody();
        } catch (Exception ex) {
            statistics = null;
            addErrorAlert("Failed to load statistics: " + ex.getMessage());
        }

        try {
            ResponseEntity<List<RecommendationDTO>> response = restTemplate.exchange(
                URI.create(JsfRestClientSupport.apiBaseUrl("/api/recommendations") + "?groupId=" + groupId),
                HttpMethod.GET,
                new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()),
                new ParameterizedTypeReference<List<RecommendationDTO>>() {}
            );
            recommendations = response.getBody() == null ? Collections.emptyList() : response.getBody();
        } catch (Exception ex) {
            recommendations = Collections.emptyList();
            addErrorAlert("Failed to load recommendations: " + ex.getMessage());
        }
    }

    private Long resolveGroupId() {
        ShellController shell = resolveShellController();
        return shell == null ? null : shell.getCurrentGroupId();
    }

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

    private <T> T resolveSpringBean(Class<T> beanType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
        WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        return applicationContext.getBean(beanType);
    }
    /**
         * Adds a success alert to the dashboard.
        * @param message The success message to display in the alert.
         */
    public void addSuccessAlert(String message) {
        alerts.add(new AlertDTO("success", message));
    }
    /**
     * Adds an error alert to the dashboard.
    * @param message The error message to display in the alert.
    */
    public void addErrorAlert(String message) {
        alerts.add(new AlertDTO("error", message));
    }
    /**
     * Adds a new item to the shopping list and displays a success alert.
     */
    public void addItemToList() {
        addSuccessAlert("Item added to shopping list");
    }
// Navigational methods to redirect to different pages in the application.
    public String goToShoppingList() { return "/shopping-list.xhtml?faces-redirect=true"; }
    public String goToReports() { return "/reports.xhtml?faces-redirect=true"; }
    public String goToRecommendations() { return "/recommendations.xhtml?faces-redirect=true"; }
    public String goToGroups() { return "/groups.xhtml?faces-redirect=true"; }

    // Getters and Setters
    public List<ShoppingItemDTO> getShoppingItems() { return shoppingItems; }
    public void setShoppingItems(List<ShoppingItemDTO> shoppingItems) { this.shoppingItems = shoppingItems; }
    public StatisticsDTO getStatistics() { return statistics; }
    public void setStatistics(StatisticsDTO statistics) { this.statistics = statistics; }
    public List<RecommendationDTO> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendationDTO> recommendations) { this.recommendations = recommendations; }
    public List<AlertDTO> getAlerts() { return alerts; }
    public void setAlerts(List<AlertDTO> alerts) { this.alerts = alerts; }
    public ShellController getShellController() { return shellController; }
    public void setShellController(ShellController shellController) { this.shellController = shellController; }
}
