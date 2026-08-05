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
    /**
     * Connect to the realtime event source.
     */
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
    /**
     * Reconnect to the realtime event source.
     */
    public void reconnectRealtime() {
        alerts.clear();
        disconnectRealtime();
        connectRealtime();
        addSuccess("Realtime connection re-established");
    }
    /**
     * Disconnect from the realtime event source.
     */
    public void disconnectRealtime() {
        alerts.clear();
        realtimeEventSource.disconnect();
        realtimeState = STATE_DISCONNECTED;
        addSuccess("Realtime connection stopped");
    }
    /**
     * Process the next mock event from the realtime event source.
     */
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
    /**
     * Refresh the shopping list snapshot.
     */
    public void refreshSnapshot() {
        shoppingItems = new ArrayList<>(FrontendDtoAdapter.createPlaceholderShoppingItems());
        processedEventIds.clear();
        lastAppliedVersion = 0L;
        if (STATE_DEGRADED.equals(realtimeState)) {
            realtimeState = STATE_CONNECTED;
        }
    }
    /**
     * Check if the realtime connection is currently active.
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return STATE_CONNECTED.equals(realtimeState);
    }
    /**
     * Check if the realtime connection is currently degraded.
     * @return true if degraded, false otherwise
     */
    public boolean isDegraded() {
        return STATE_DEGRADED.equals(realtimeState);
    }
    /**
     * Process a realtime shopping event and update the shopping list accordingly.
     * @param event The realtime shopping event to process
     */
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
    /**
     * Remove a shopping item from the list by its name.
     * @param itemName The name of the item to remove
     */
    private void removeByName(String itemName) {
        if (itemName == null) {
            return;
        }

        shoppingItems.removeIf(item -> item.getName() != null && item.getName().equalsIgnoreCase(itemName));
    }
    /**
     * Check if the given event has already been processed (duplicate).
     * @param event The realtime shopping event to check
     * @return true if the event is a duplicate, false otherwise
     */
    private boolean isDuplicateEvent(RealtimeShoppingEventDTO event) {
        String eventId = event.getEventId();
        return eventId != null && processedEventIds.contains(eventId);
    }
    /**
     * Check if the given event is out of order based on its version number.
     * @param event The realtime shopping event to check
     * @return true if the event is out of order, false otherwise
     */
    private boolean isOutOfOrderEvent(RealtimeShoppingEventDTO event) {
        return event.getVersion() > 0 && event.getVersion() < lastAppliedVersion;
    }
    /**
     * Track the processed event by storing its ID and updating the last applied version.
     * @param event The realtime shopping event to track
     */
    private void trackEvent(RealtimeShoppingEventDTO event) {
        if (event.getEventId() != null) {
            processedEventIds.add(event.getEventId());
        }
        if (event.getVersion() > lastAppliedVersion) {
            lastAppliedVersion = event.getVersion();
        }
    }


// add alert messages to the list.
    private void addSuccess(String message) {
        alerts.add(new AlertDTO("success", message));
    }

    private void addWarning(String message) {
        alerts.add(new AlertDTO("warning", message));
    }

    private void addError(String message) {
        alerts.add(new AlertDTO("error", message));
    }
// resolve a Spring bean from the application context using the FacesContext and ServletContext.
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
    // getters and
    private Long getCurrentUserId() {
        if (shellController == null || shellController.getCurrentUser() == null || shellController.getCurrentUser().getId() == null) {
            throw new IllegalStateException("No authenticated user in session");
        }
        return shellController.getCurrentUser().getId();
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
