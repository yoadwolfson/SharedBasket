package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.AlertDTO;
import com.sharedbasket.dto.RecommendationDTO;
import com.sharedbasket.dto.ShoppingItemDTO;
import com.sharedbasket.dto.StatisticsDTO;
import com.sharedbasket.util.FrontendDtoAdapter;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "dashboardController")
@ViewScoped
public class DashboardController implements Serializable {

    private static final long serialVersionUID = 1L;

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
        loadDashboardData();
    }
    /**
     * Loads the dashboard data including shopping items, statistics, and recommendations.
     */
    private void loadDashboardData() {
        shoppingItems = FrontendDtoAdapter.createPlaceholderShoppingItems();
        statistics = FrontendDtoAdapter.createPlaceholderStatistics();
        recommendations = FrontendDtoAdapter.createPlaceholderRecommendations();
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
}
