package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.AlertDTO;
import com.sharedbasket.dto.PurchaseHistoryEntryDTO;
import com.sharedbasket.dto.SpendingByCategoryDTO;
import com.sharedbasket.dto.SpendingByProductDTO;
import com.sharedbasket.dto.StatisticsDTO;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ManagedBean(name = "reportsController")
@ViewScoped
public class ReportsController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_LOOKBACK_DAYS = 30;

    private transient RestTemplate restTemplate;

    @ManagedProperty(value = "#{shellController}")
    private ShellController shellController;

    private StatisticsDTO statistics;
    private List<SpendingByCategoryDTO> spendingByCategory;
    private List<SpendingByProductDTO> spendingByProduct;
    private List<PurchaseHistoryEntryDTO> purchaseHistory;
    private List<AlertDTO> alerts;
    private boolean loading;

    @PostConstruct
    public void init() {
        alerts = new ArrayList<>();
        spendingByCategory = Collections.emptyList();
        spendingByProduct = Collections.emptyList();
        purchaseHistory = Collections.emptyList();
        restTemplate = resolveSpringBean(RestTemplate.class);
        reloadReports();
    }

    /**
     * Reloads all report sections for the current group.
     */
    public void reloadReports() {
        alerts.clear();
        loading = true;
        Long groupId = resolveGroupId();
        if (groupId == null) {
            addError("No group available for reporting");
            loading = false;
            return;
        }
        Instant from = Instant.now().minus(DEFAULT_LOOKBACK_DAYS, ChronoUnit.DAYS);
        Instant to = Instant.now();
        try {
            statistics = restTemplate.exchange(
                reportsUri("/statistics", groupId, from, to),
                HttpMethod.GET,
                new HttpEntity<>(buildJsonHeaders()),
                StatisticsDTO.class
            ).getBody();

            ResponseEntity<List<SpendingByCategoryDTO>> categoryResponse = restTemplate.exchange(
                reportsUri("/spending-by-category", groupId, from, to),
                HttpMethod.GET,
                new HttpEntity<>(buildJsonHeaders()),
                new ParameterizedTypeReference<List<SpendingByCategoryDTO>>() {}
            );
            spendingByCategory = categoryResponse.getBody() == null ? Collections.emptyList() : categoryResponse.getBody();

            ResponseEntity<List<SpendingByProductDTO>> productResponse = restTemplate.exchange(
                reportsUri("/spending-by-product", groupId, from, to),
                HttpMethod.GET,
                new HttpEntity<>(buildJsonHeaders()),
                new ParameterizedTypeReference<List<SpendingByProductDTO>>() {}
            );
            spendingByProduct = productResponse.getBody() == null ? Collections.emptyList() : productResponse.getBody();

            ResponseEntity<List<PurchaseHistoryEntryDTO>> historyResponse = restTemplate.exchange(
                reportsUri("/purchase-history", groupId, from, to),
                HttpMethod.GET,
                new HttpEntity<>(buildJsonHeaders()),
                new ParameterizedTypeReference<List<PurchaseHistoryEntryDTO>>() {}
            );
            purchaseHistory = historyResponse.getBody() == null ? Collections.emptyList() : historyResponse.getBody();
        } catch (Exception ex) {
            addError(extractErrorMessage(ex));
            statistics = null;
            spendingByCategory = Collections.emptyList();
            spendingByProduct = Collections.emptyList();
            purchaseHistory = Collections.emptyList();
        } finally {
            loading = false;
        }
    }

    private URI reportsUri(String path, Long groupId, Instant from, Instant to) {
        return URI.create(JsfRestClientSupport.apiBaseUrl("/api/reports") + path
            + "?groupId=" + groupId + "&from=" + from + "&to=" + to);
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

    private void addError(String message) {
        alerts.add(new AlertDTO("error", message));
    }

    private <T> T resolveSpringBean(Class<T> beanType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
        WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        return applicationContext.getBean(beanType);
    }

    // Getters and Setters
    public StatisticsDTO getStatistics() { return statistics; }
    public List<SpendingByCategoryDTO> getSpendingByCategory() { return spendingByCategory; }
    public List<SpendingByProductDTO> getSpendingByProduct() { return spendingByProduct; }
    public List<PurchaseHistoryEntryDTO> getPurchaseHistory() { return purchaseHistory; }
    public List<AlertDTO> getAlerts() { return alerts; }
    public boolean isLoading() { return loading; }
    public ShellController getShellController() { return shellController; }
    public void setShellController(ShellController shellController) { this.shellController = shellController; }
}
