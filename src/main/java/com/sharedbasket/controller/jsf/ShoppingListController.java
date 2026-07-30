package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.AlertDTO;
import com.sharedbasket.dto.RealtimeEventType;
import com.sharedbasket.dto.RealtimeShoppingEventDTO;
import com.sharedbasket.dto.ShoppingItemDTO;
import com.sharedbasket.service.realtime.RealtimeEventSource;
import com.sharedbasket.util.FrontendDtoAdapter;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ManagedBean(name = "shoppingListController")
@ViewScoped
public class ShoppingListController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String STATE_DISCONNECTED = "DISCONNECTED";
    private static final String STATE_CONNECTED = "CONNECTED";
    private static final String STATE_DEGRADED = "DEGRADED";

    private RealtimeEventSource realtimeEventSource;

    @ManagedProperty(value = "#{shellController}")
    private ShellController shellController;

    private List<ShoppingItemDTO> shoppingItems;
    private List<AlertDTO> alerts;
    private String realtimeState;
    private String lastEventMessage;
    private long lastAppliedVersion;
    private Set<String> processedEventIds;
    private String selectedScenario;

    @PostConstruct
    public void init() {
        if (this.realtimeEventSource == null) {
            this.realtimeEventSource = resolveSpringBean(RealtimeEventSource.class);
        }
        this.shoppingItems = new ArrayList<>(FrontendDtoAdapter.createPlaceholderShoppingItems());
        this.alerts = new ArrayList<>();
        this.realtimeState = STATE_DISCONNECTED;
        this.lastEventMessage = "No events yet";
        this.lastAppliedVersion = 0L;
        this.processedEventIds = new HashSet<>();
        this.selectedScenario = "ORDERING_EDGE_CASE";
    }

    public void connectRealtime() {
        alerts.clear();
        try {
            Long userId = getCurrentUserId();
            realtimeEventSource.setScenario(selectedScenario);
            realtimeEventSource.connect(userId, 1L);
            realtimeState = STATE_CONNECTED;
            processedEventIds.clear();
            lastAppliedVersion = 0L;
            addSuccess("Realtime connection started (mock)");
        } catch (Exception ex) {
            realtimeState = STATE_DEGRADED;
            addError(ex.getMessage());
        }
    }

    public void reconnectRealtime() {
        alerts.clear();
        disconnectRealtime();
        connectRealtime();
        addSuccess("Realtime connection re-established");
    }

    public void disconnectRealtime() {
        alerts.clear();
        realtimeEventSource.disconnect();
        realtimeState = STATE_DISCONNECTED;
        addSuccess("Realtime connection stopped");
    }

    public void processNextMockEvent() {
        alerts.clear();
        if (!realtimeEventSource.isConnected()) {
            addWarning("Connect first to receive events");
            return;
        }

        RealtimeShoppingEventDTO event = realtimeEventSource.nextEvent();
        if (event == null) {
            addWarning("No event available");
            return;
        }

        lastEventMessage = event.getMessage() == null ? "Event processed" : event.getMessage();

        if (isDuplicateEvent(event)) {
            addWarning("Duplicate event ignored: " + event.getEventId());
            return;
        }

        if (isOutOfOrderEvent(event)) {
            realtimeState = STATE_DEGRADED;
            addWarning("Out-of-order event ignored. Trigger snapshot refresh.");
            return;
        }

        trackEvent(event);

        if (RealtimeEventType.ITEM_ADDED.equals(event.getType())) {
            shoppingItems.add(new ShoppingItemDTO(event.getItemName(), event.getQuantity(), event.getEstimatedPrice()));
            addSuccess(lastEventMessage);
            return;
        }

        if (RealtimeEventType.ITEM_UPDATED.equals(event.getType())) {
            updateOrInsert(event);
            addSuccess(lastEventMessage);
            return;
        }

        if (RealtimeEventType.ITEM_REMOVED.equals(event.getType())) {
            removeByName(event.getItemName());
            addSuccess(lastEventMessage);
            return;
        }

        if (RealtimeEventType.CONFLICT_DETECTED.equals(event.getType())) {
            realtimeState = STATE_DEGRADED;
            addWarning(lastEventMessage + ". Use refresh snapshot.");
            return;
        }

        if (RealtimeEventType.SNAPSHOT_REQUIRED.equals(event.getType())) {
            refreshSnapshot();
            addWarning(lastEventMessage);
            return;
        }

        addSuccess(lastEventMessage);
    }

    public void refreshSnapshot() {
        shoppingItems = new ArrayList<>(FrontendDtoAdapter.createPlaceholderShoppingItems());
        processedEventIds.clear();
        lastAppliedVersion = 0L;
        if (STATE_DEGRADED.equals(realtimeState)) {
            realtimeState = STATE_CONNECTED;
        }
    }

    public boolean isConnected() {
        return STATE_CONNECTED.equals(realtimeState);
    }

    public boolean isDegraded() {
        return STATE_DEGRADED.equals(realtimeState);
    }

    private void updateOrInsert(RealtimeShoppingEventDTO event) {
        for (ShoppingItemDTO item : shoppingItems) {
            if (item.getName() != null && item.getName().equalsIgnoreCase(event.getItemName())) {
                item.setQuantity(event.getQuantity());
                item.setEstimatedPrice(event.getEstimatedPrice());
                return;
            }
        }

        shoppingItems.add(new ShoppingItemDTO(event.getItemName(), event.getQuantity(), event.getEstimatedPrice()));
    }

    private void removeByName(String itemName) {
        if (itemName == null) {
            return;
        }

        shoppingItems.removeIf(item -> item.getName() != null && item.getName().equalsIgnoreCase(itemName));
    }

    private boolean isDuplicateEvent(RealtimeShoppingEventDTO event) {
        String eventId = event.getEventId();
        return eventId != null && processedEventIds.contains(eventId);
    }

    private boolean isOutOfOrderEvent(RealtimeShoppingEventDTO event) {
        return event.getVersion() > 0 && event.getVersion() < lastAppliedVersion;
    }

    private void trackEvent(RealtimeShoppingEventDTO event) {
        if (event.getEventId() != null) {
            processedEventIds.add(event.getEventId());
        }
        if (event.getVersion() > lastAppliedVersion) {
            lastAppliedVersion = event.getVersion();
        }
    }

    private Long getCurrentUserId() {
        if (shellController == null || shellController.getCurrentUser() == null || shellController.getCurrentUser().getId() == null) {
            throw new IllegalStateException("No authenticated user in session");
        }
        return shellController.getCurrentUser().getId();
    }

    private void addSuccess(String message) {
        alerts.add(new AlertDTO("success", message));
    }

    private void addWarning(String message) {
        alerts.add(new AlertDTO("warning", message));
    }

    private void addError(String message) {
        alerts.add(new AlertDTO("error", message));
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

    public List<ShoppingItemDTO> getShoppingItems() {
        return shoppingItems;
    }

    public List<AlertDTO> getAlerts() {
        return alerts;
    }

    public String getRealtimeState() {
        return realtimeState;
    }

    public String getLastEventMessage() {
        return lastEventMessage;
    }

    public long getLastAppliedVersion() {
        return lastAppliedVersion;
    }

    public String getSelectedScenario() {
        return selectedScenario;
    }

    public void setSelectedScenario(String selectedScenario) {
        this.selectedScenario = selectedScenario;
    }

    public ShellController getShellController() {
        return shellController;
    }

    public void setShellController(ShellController shellController) {
        this.shellController = shellController;
    }

    public RealtimeEventSource getRealtimeEventSource() {
        return realtimeEventSource;
    }

    public void setRealtimeEventSource(RealtimeEventSource realtimeEventSource) {
        this.realtimeEventSource = realtimeEventSource;
    }
}
