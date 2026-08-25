package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.AlertDTO;
import com.sharedbasket.domain.ItemUnit;
import com.sharedbasket.dto.CategoryDTO;
import com.sharedbasket.dto.CreateShoppingItemRequestDTO;
import com.sharedbasket.dto.GroupDTO;
import com.sharedbasket.dto.ShoppingItemDTO;
import com.sharedbasket.dto.ShoppingItemRequestDTO;
import com.sharedbasket.dto.DecisionNoteRequestDTO;
import com.sharedbasket.dto.UpdateShoppingItemRequestDTO;
import com.sharedbasket.util.JsfRestClientSupport;
import com.sharedbasket.util.QuantityNormalizer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ManagedBean(name = "shoppingListController")
@ViewScoped
public class ShoppingListController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SELECTED_GROUP_SESSION_KEY = "sharedbasket.shoppingList.selectedGroupId";

    private transient RestTemplate restTemplate;

    @ManagedProperty(value = "#{shellController}")
    private ShellController shellController;

    private List<ShoppingItemDTO> shoppingItems;
    private List<AlertDTO> alerts;
    private List<CategoryDTO> categories;
    private List<ShoppingItemRequestDTO> pendingRequests;
    private List<ShoppingItemRequestDTO> myRequests;
    private List<GroupDTO> groups;
    private Long selectedGroupId;
    private String selectedGroupName;
    private String selectedGroupRole;
    private String itemName;
    private BigDecimal itemQuantity = BigDecimal.ONE;
    private ItemUnit itemUnit = ItemUnit.PIECE;
    private BigDecimal itemEstimatedPrice;
    private Long itemCategoryId;
    private String itemNotes;
    private Long editingItemId;
    private Long editingItemVersion;
    private boolean addFormVisible = false;

    private String lastPollSignature;
    private String pollRenderTargets = "";

    @PostConstruct
    public void init() {
        this.restTemplate = resolveSpringBean(RestTemplate.class);
        this.shoppingItems = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.pendingRequests = new ArrayList<>();
        this.myRequests = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.alerts = new ArrayList<>();
        loadGroups();
        restoreSelectedGroup();
    }

    /** The bean is view-scoped, so a group the user picked earlier in this session is re-applied on every page load. */
    void restoreSelectedGroup() {
        if (selectedGroupId != null || groups.isEmpty()) {
            return;
        }
        Long rememberedId = readRememberedGroupId();
        if (rememberedId == null) {
            return;
        }
        GroupDTO target = findGroup(rememberedId);
        if (target == null) {
            forgetGroupId();
            return;
        }
        applySelectedGroup(target);
        loadData();
        loadRequests();
    }

    private GroupDTO findGroup(Long groupId) {
        if (groupId == null) {
            return null;
        }
        for (GroupDTO group : groups) {
            if (groupId.equals(group.getId())) {
                return group;
            }
        }
        return null;
    }

    Long readRememberedGroupId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }
        Object stored = facesContext.getExternalContext().getSessionMap().get(SELECTED_GROUP_SESSION_KEY);
        return stored instanceof Long ? (Long) stored : null;
    }

    void rememberGroupId(Long groupId) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            facesContext.getExternalContext().getSessionMap().put(SELECTED_GROUP_SESSION_KEY, groupId);
        }
    }

    void forgetGroupId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            facesContext.getExternalContext().getSessionMap().remove(SELECTED_GROUP_SESSION_KEY);
        }
    }

    public void loadGroups() {
        try {
            GroupDTO[] loadedGroups = restTemplate.exchange(
                JsfRestClientSupport.apiBaseUrl("/api/groups"),
                HttpMethod.GET,
                new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()),
                GroupDTO[].class
            ).getBody();
            groups = loadedGroups == null ? new ArrayList<>() : new ArrayList<>(java.util.Arrays.asList(loadedGroups));
        } catch (Exception ex) {
            groups = new ArrayList<>();
            addError(errorMessage(ex));
        }
    }

    /** Bound to the group switcher; an empty selection is the explicit deselect path. */
    public void onGroupChanged() {
        clearAlerts();
        Long requestedId = selectedGroupId;
        if (requestedId == null) {
            clearSelectedGroup();
            return;
        }
        GroupDTO selected = findGroup(requestedId);
        if (selected == null) {
            clearSelectedGroup();
            addError("Select a group you belong to");
            return;
        }
        applySelectedGroup(selected);
        pendingRequests.clear();
        myRequests.clear();
        loadData();
        loadRequests();
    }

    private void clearSelectedGroup() {
        selectedGroupId = null;
        selectedGroupName = null;
        selectedGroupRole = null;
        shoppingItems.clear();
        categories.clear();
        pendingRequests.clear();
        myRequests.clear();
        startNewItem();
        addFormVisible = false;
        forgetGroupId();
    }

    private void applySelectedGroup(GroupDTO selected) {
        selectedGroupId = selected.getId();
        selectedGroupName = selected.getName();
        selectedGroupRole = selected.getMyRole();
        rememberGroupId(selected.getId());
        if (shellController != null) {
            shellController.setCurrentGroup(selected.getId(), selected.getName());
        }
        startNewItem();
        addFormVisible = false;
    }

    public void loadRequests() {
        if (selectedGroupId == null) return;
        try {
            if (isSelectedGroupAdmin()) {
                pendingRequests = restTemplate.exchange(
                    requestUrl(selectedGroupId) + "/pending", HttpMethod.GET,
                    new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()),
                    new ParameterizedTypeReference<List<ShoppingItemRequestDTO>>() {}).getBody();
                if (pendingRequests == null) pendingRequests = new ArrayList<>();
                myRequests = new ArrayList<>();
            } else {
                myRequests = restTemplate.exchange(
                    requestUrl(selectedGroupId) + "/mine", HttpMethod.GET,
                    new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()),
                    new ParameterizedTypeReference<List<ShoppingItemRequestDTO>>() {}).getBody();
                if (myRequests == null) myRequests = new ArrayList<>();
                pendingRequests = new ArrayList<>();
            }
        } catch (Exception ex) {
            addError(errorMessage(ex));
        }
    }

    public void approveRequest(ShoppingItemRequestDTO request) {
        if (request == null || request.getId() == null || selectedGroupId == null) return;
        clearAlerts();
        try {
            restTemplate.exchange(requestUrl(selectedGroupId) + "/" + request.getId() + "/approve",
                HttpMethod.POST, new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()), ShoppingItemRequestDTO.class);
            loadData();
            loadRequests();
            addSuccess("Request approved");
        } catch (Exception ex) {
            addError(errorMessage(ex));
            if (isConflict(ex)) {
                loadData();
                loadRequests();
            }
        }
    }

    public void rejectRequest(ShoppingItemRequestDTO request) {
        if (request == null || request.getId() == null || selectedGroupId == null) return;
        clearAlerts();
        try {
            DecisionNoteRequestDTO decision = new DecisionNoteRequestDTO();
            restTemplate.exchange(requestUrl(selectedGroupId) + "/" + request.getId() + "/reject",
                HttpMethod.POST, new HttpEntity<>(decision, JsfRestClientSupport.buildJsonHeaders()), ShoppingItemRequestDTO.class);
            loadRequests();
            addSuccess("Request rejected");
        } catch (Exception ex) {
            addError(errorMessage(ex));
        }
    }

    public boolean isGroupSelected() {
        return selectedGroupId != null;
    }

    public boolean getGroupSelected() {
        return selectedGroupId != null;
    }

    public boolean isSelectedGroupAdmin() {
        return "ADMIN".equalsIgnoreCase(selectedGroupRole);
    }

    public boolean getSelectedGroupAdmin() {
        return isSelectedGroupAdmin();
    }

    public void loadData() {
        Long groupId = getCurrentGroupId();
        if (groupId == null) {
            if (alerts.isEmpty()) {
                addWarning("Select a group before loading the shopping list");
            }
            return;
        }
        try {
            categories = restTemplate.exchange(
                JsfRestClientSupport.apiBaseUrl("/api/groups/" + groupId + "/categories"),
                HttpMethod.GET,
                new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()),
                new ParameterizedTypeReference<List<CategoryDTO>>() {}
            ).getBody();
            if (categories == null) categories = new ArrayList<>();

            shoppingItems = restTemplate.exchange(
                JsfRestClientSupport.apiBaseUrl("/api/groups/" + groupId + "/items"),
                HttpMethod.GET,
                new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()),
                new ParameterizedTypeReference<List<ShoppingItemDTO>>() {}
            ).getBody();
            if (shoppingItems == null) shoppingItems = new ArrayList<>();
        } catch (Exception ex) {
            addError(errorMessage(ex));
        }
    }

    public void startNewItem() {
        editingItemId = null;
        editingItemVersion = null;
        itemName = null;
        itemQuantity = BigDecimal.ONE;
        itemUnit = ItemUnit.PIECE;
        itemEstimatedPrice = null;
        itemCategoryId = null;
        itemNotes = null;
    }

    /** Opens the Add/Edit form in blank "new item" state; bound to the "+ Add Item" trigger button. */
    public void openAddForm() {
        startNewItem();
        addFormVisible = true;
    }

    /** Collapses the Add/Edit form and resets its fields; bound to the "Cancel" button. */
    public void closeAddForm() {
        startNewItem();
        addFormVisible = false;
    }

    /** Re-normalizes the entered quantity whenever the unit dropdown changes. */
    public void onUnitChanged() {
        if (itemUnit == null) {
            itemUnit = ItemUnit.PIECE;
        }
        itemQuantity = QuantityNormalizer.normalize(itemQuantity, itemUnit);
    }

    public void editItem(ShoppingItemDTO item) {
        if (item == null) return;
        addFormVisible = true;
        editingItemId = item.getId();
        editingItemVersion = item.getVersion();
        itemName = item.getName();
        itemQuantity = item.getQuantity();
        itemUnit = item.getUnit();
        itemEstimatedPrice = item.getEstimatedPrice();
        itemCategoryId = item.getCategoryId();
        itemNotes = item.getNotes();
    }

    public void saveItem() {
        clearAlerts();
        if (itemUnit == null) {
            itemUnit = ItemUnit.PIECE;
        }
        Long groupId = getCurrentGroupId();
        if (groupId == null) {
            addError("Select a group before adding an item");
            addFormVisible = true;
            return;
        }
        if (!validateItemInput()) {
            return;
        }
        if (isDuplicateItemName(itemName)) {
            addError("\"" + itemName.trim() + "\" is already on this list");
            addFormVisible = true;
            return;
        }
        if (itemCategoryId != null && !categoryExists(itemCategoryId)) {
            addError("That category is no longer available - pick another one");
            itemCategoryId = null;
            addFormVisible = true;
            return;
        }
        try {
            if (!isSelectedGroupAdmin()) {
                if (editingItemId == null) {
                    restTemplate.exchange(requestUrl(groupId) + "/add", HttpMethod.POST,
                        new HttpEntity<>(buildCreateRequest(), JsfRestClientSupport.buildJsonHeaders()), Object.class);
                    startNewItem();
                    addFormVisible = false;
                    addSuccess("Add request submitted");
                    return;
                }
                UpdateShoppingItemRequestDTO request = buildUpdateRequest();
                restTemplate.exchange(requestUrl(groupId) + "/items/" + editingItemId + "/edit", HttpMethod.POST,
                    new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()), Object.class);
                startNewItem();
                addFormVisible = false;
                addSuccess("Edit request submitted");
                return;
            }
            if (editingItemId == null) {
                restTemplate.exchange(
                    itemUrl(groupId), HttpMethod.POST,
                    new HttpEntity<>(buildCreateRequest(), JsfRestClientSupport.buildJsonHeaders()), ShoppingItemDTO.class);
                refreshShoppingState();
                addSuccess("Item added");
            } else {
                restTemplate.exchange(
                    itemUrl(groupId) + "/" + editingItemId, HttpMethod.PUT,
                    new HttpEntity<>(buildUpdateRequest(), JsfRestClientSupport.buildJsonHeaders()), ShoppingItemDTO.class);
                refreshShoppingState();
                addSuccess("Item updated");
            }
            startNewItem();
            addFormVisible = false;
        } catch (Exception ex) {
            if (isConflict(ex)) {
                loadData();
            }
            addFormVisible = true;
            addError(saveErrorMessage(ex));
        }
    }

    private boolean validateItemInput() {
        String normalizedName = itemName == null ? null : itemName.trim();
        if (normalizedName == null || normalizedName.isEmpty()) {
            addError("Item name is required");
            addFormVisible = true;
            return false;
        }
        if (normalizedName.length() < 2) {
            addError("Item name must be at least 2 characters");
            addFormVisible = true;
            return false;
        }
        if (normalizedName.length() > 200) {
            addError("Item name cannot exceed 200 characters");
            addFormVisible = true;
            return false;
        }
        if (itemQuantity == null) {
            addError("Quantity is required");
            addFormVisible = true;
            return false;
        }
        if (itemQuantity.signum() <= 0) {
            addError("Quantity must be greater than 0");
            addFormVisible = true;
            return false;
        }
        if (itemQuantity.compareTo(new BigDecimal("9999")) > 0) {
            addError("Quantity cannot exceed 9999");
            addFormVisible = true;
            return false;
        }
        if (itemUnit == ItemUnit.PIECE && itemQuantity.stripTrailingZeros().scale() > 0) {
            addError("Quantity must be a whole number for pieces");
            addFormVisible = true;
            return false;
        }
        if (itemEstimatedPrice != null && itemEstimatedPrice.signum() < 0) {
            addError("Estimated price cannot be negative");
            addFormVisible = true;
            return false;
        }
        if (itemEstimatedPrice != null && itemEstimatedPrice.compareTo(new BigDecimal("999999.99")) > 0) {
            addError("Estimated price cannot exceed 999,999.99");
            addFormVisible = true;
            return false;
        }
        if (itemEstimatedPrice != null && itemEstimatedPrice.stripTrailingZeros().scale() > 2) {
            addError("Estimated price can have at most 2 decimal places");
            addFormVisible = true;
            return false;
        }
        if (itemNotes != null && itemNotes.trim().length() > 500) {
            addError("Notes cannot exceed 500 characters");
            addFormVisible = true;
            return false;
        }
        return true;
    }

    private boolean isDuplicateItemName(String candidate) {
        if (candidate == null || shoppingItems == null) {
            return false;
        }
        String normalized = candidate.trim();
        if (normalized.isEmpty()) {
            return false;
        }
        for (ShoppingItemDTO item : shoppingItems) {
            if (item == null || Boolean.TRUE.equals(item.getPurchased())) {
                continue;
            }
            if (editingItemId != null && Objects.equals(editingItemId, item.getId())) {
                continue;
            }
            if (item.getName() != null && item.getName().equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }

    private boolean categoryExists(Long categoryId) {
        if (categoryId == null) {
            return true;
        }
        List<CategoryDTO> categoryList = categories == null ? Collections.<CategoryDTO>emptyList() : categories;
        for (CategoryDTO category : categoryList) {
            if (category != null && Objects.equals(category.getId(), categoryId)) {
                return true;
            }
        }
        return false;
    }

    private String saveErrorMessage(Exception ex) {
        if (ex instanceof HttpStatusCodeException) {
            int status = ((HttpStatusCodeException) ex).getStatusCode().value();
            if (status == 409) {
                return "This item was changed by someone else. The list has been refreshed - review and try again.";
            }
            if (status == 404) {
                return "This item no longer exists. It may have been removed by another member.";
            }
            if (status == 403) {
                return "You do not have permission to change items in this group";
            }
            if (status == 400) {
                String fallback = errorMessage(ex);
                return fallback == null || fallback.trim().isEmpty() ? "Could not save the item right now. Please try again." : fallback;
            }
            return "Could not save the item right now. Please try again.";
        }
        if (ex instanceof org.springframework.web.client.ResourceAccessException) {
            return "Could not save the item right now. Please try again.";
        }
        return "Could not save the item right now. Please try again.";
    }

    private CreateShoppingItemRequestDTO buildCreateRequest() {
        CreateShoppingItemRequestDTO request = new CreateShoppingItemRequestDTO();
        request.setName(itemName);
        request.setQuantity(itemQuantity);
        request.setUnit(itemUnit);
        request.setEstimatedPrice(itemEstimatedPrice);
        request.setCategoryId(itemCategoryId);
        request.setNotes(itemNotes);
        return request;
    }

    private UpdateShoppingItemRequestDTO buildUpdateRequest() {
        UpdateShoppingItemRequestDTO request = new UpdateShoppingItemRequestDTO();
        request.setName(itemName);
        request.setQuantity(itemQuantity);
        request.setUnit(itemUnit);
        request.setEstimatedPrice(itemEstimatedPrice);
        request.setCategoryId(itemCategoryId);
        request.setNotes(itemNotes);
        request.setVersion(editingItemVersion);
        return request;
    }

    public void deleteItem(ShoppingItemDTO item) {
        if (item == null || item.getId() == null) return;
        clearAlerts();
        try {
            if (!isSelectedGroupAdmin()) {
                UpdateShoppingItemRequestDTO request = new UpdateShoppingItemRequestDTO();
                request.setVersion(item.getVersion());
                restTemplate.exchange(requestUrl(getCurrentGroupId()) + "/items/" + item.getId() + "/delete",
                    HttpMethod.POST, new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()), Object.class);
                addSuccess("Delete request submitted");
                return;
            }
            restTemplate.exchange(itemUrl(getCurrentGroupId()) + "/" + item.getId(), HttpMethod.DELETE,
                new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()), Void.class);
            refreshShoppingState();
            addSuccess("Item deleted");
        } catch (Exception ex) {
            addError(errorMessage(ex));
        }
    }

    public void togglePurchased(ShoppingItemDTO item) {
        if (item == null || item.getId() == null) return;
        clearAlerts();
        try {
            UpdateShoppingItemRequestDTO request = new UpdateShoppingItemRequestDTO();
            request.setVersion(item.getVersion());
            String action = Boolean.TRUE.equals(item.getPurchased()) ? "mark-unpurchased" : "mark-purchased";
            restTemplate.exchange(itemUrl(getCurrentGroupId()) + "/" + item.getId() + "/" + action,
                HttpMethod.POST, new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()), ShoppingItemDTO.class);
            refreshShoppingState();
            addSuccess(Boolean.TRUE.equals(item.getPurchased()) ? "Item marked as pending" : "Item marked as purchased");
        } catch (Exception ex) {
            addError(errorMessage(ex));
            if (isConflict(ex)) refreshShoppingState();
        }
    }

    /**
     * Reload the shopping list snapshot from the server.
     */
    public void refreshSnapshot() {
        refreshShoppingState();
    }

    private void refreshShoppingState() {
        loadData();
        if (selectedGroupId != null) {
            loadRequests();
        }
    }

    /**
     * Silently refreshes the shopping list and requests for background ajax polling, so other
     * members' changes appear without a manual reload.
     */
    public void pollRefresh() {
        if (selectedGroupId == null) {
            pollRenderTargets = "";
            return;
        }
        refreshShoppingState();
        String signature = pollSignature();
        // Only ask the client to replace the DOM when something actually changed, otherwise a
        // periodic no-op render can swallow a user's click on a button mid-swap.
        pollRenderTargets = signature.equals(lastPollSignature) ? "" : ":shoppingAlerts :shoppingItemsTable";
        lastPollSignature = signature;
    }

    /**
     * Builds a lightweight signature of the currently loaded items/requests/alerts, used to
     * detect whether a background poll actually changed anything worth rendering.
     */
    private String pollSignature() {
        StringBuilder sb = new StringBuilder();
        for (ShoppingItemDTO item : shoppingItems) {
            sb.append("i:").append(item.getId()).append(':').append(item.getVersion())
                .append(':').append(item.getPurchased()).append(';');
        }
        for (ShoppingItemRequestDTO request : pendingRequests) {
            sb.append("p:").append(request.getId()).append(':').append(request.getStatus()).append(';');
        }
        for (ShoppingItemRequestDTO request : myRequests) {
            sb.append("m:").append(request.getId()).append(':').append(request.getStatus()).append(';');
        }
        sb.append("a:").append(alerts.size());
        return sb.toString();
    }

    public String getPollRenderTargets() {
        return pollRenderTargets;
    }

// add alert messages to the list.
    private void clearAlerts() {
        alerts.clear();
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

    private Long getCurrentGroupId() {
        return selectedGroupId;
    }

    private String categoryUrl(Long groupId) {
        return JsfRestClientSupport.apiBaseUrl("/api/groups/" + groupId + "/categories");
    }

    private String itemUrl(Long groupId) {
        return JsfRestClientSupport.apiBaseUrl("/api/groups/" + groupId + "/items");
    }

    private String requestUrl(Long groupId) {
        return JsfRestClientSupport.apiBaseUrl("/api/groups/" + groupId + "/item-requests");
    }

    private String errorMessage(Exception ex) {
        String message = JsfRestClientSupport.extractErrorMessage(ex);
        return message == null || message.trim().isEmpty() ? "Could not save the item right now. Please try again." : message;
    }

    private boolean isConflict(Exception ex) {
        return ex instanceof HttpStatusCodeException
            && ((HttpStatusCodeException) ex).getStatusCode().value() == 409;
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
    public List<ShoppingItemDTO> getShoppingItems() {
        return shoppingItems;
    }

    public List<AlertDTO> getAlerts() {
        return alerts;
    }

    public List<CategoryDTO> getCategories() { return categories == null ? Collections.emptyList() : categories; }
    public List<ShoppingItemRequestDTO> getPendingRequests() { return pendingRequests == null ? Collections.emptyList() : pendingRequests; }
    public List<ShoppingItemRequestDTO> getMyRequests() { return myRequests == null ? Collections.emptyList() : myRequests; }
    public List<GroupDTO> getGroups() { return groups == null ? Collections.emptyList() : groups; }
    public Long getSelectedGroupId() { return selectedGroupId; }
    public void setSelectedGroupId(Long selectedGroupId) { this.selectedGroupId = selectedGroupId; }
    public boolean isHasGroups() { return groups != null && !groups.isEmpty(); }
    public boolean getHasGroups() { return isHasGroups(); }
    public String getSelectedGroupName() { return selectedGroupName; }
    public String getSelectedGroupRole() { return selectedGroupRole; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getItemQuantity() { return itemQuantity; }
    public void setItemQuantity(BigDecimal itemQuantity) { this.itemQuantity = itemQuantity; }
    public ItemUnit getItemUnit() { return itemUnit; }
    public void setItemUnit(ItemUnit itemUnit) { this.itemUnit = itemUnit == null ? ItemUnit.PIECE : itemUnit; }
    public List<ItemUnit> getItemUnitOptions() { return ItemUnit.options(); }
    public String getQuantityStep() { return itemUnit == ItemUnit.KILOGRAM ? "any" : "1"; }
    public String getQuantityMin() { return itemUnit == ItemUnit.KILOGRAM ? "0.001" : "1"; }
    public BigDecimal getItemEstimatedPrice() { return itemEstimatedPrice; }
    public void setItemEstimatedPrice(BigDecimal itemEstimatedPrice) { this.itemEstimatedPrice = itemEstimatedPrice; }
    public Long getItemCategoryId() { return itemCategoryId; }
    public void setItemCategoryId(Long itemCategoryId) { this.itemCategoryId = itemCategoryId; }
    public String getItemNotes() { return itemNotes; }
    public void setItemNotes(String itemNotes) { this.itemNotes = itemNotes; }
    public boolean isEditing() { return editingItemId != null; }
    public boolean isAddFormVisible() { return addFormVisible; }
    public boolean getAddFormVisible() { return addFormVisible; }

    public ShellController getShellController() {
        return shellController;
    }

    public void setShellController(ShellController shellController) {
        this.shellController = shellController;
    }
}
