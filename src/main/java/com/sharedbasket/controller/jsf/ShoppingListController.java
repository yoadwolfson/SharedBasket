package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.AlertDTO;
import com.sharedbasket.dto.CategoryColorOption;
import com.sharedbasket.dto.CategoryDTO;
import com.sharedbasket.dto.CreateCategoryRequestDTO;
import com.sharedbasket.dto.CreateShoppingItemRequestDTO;
import com.sharedbasket.dto.GroupDTO;
import com.sharedbasket.dto.ShoppingItemDTO;
import com.sharedbasket.dto.UpdateShoppingItemRequestDTO;
import com.sharedbasket.dto.UpdateCategoryRequestDTO;
import com.sharedbasket.util.JsfRestClientSupport;
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

@ManagedBean(name = "shoppingListController")
@ViewScoped
public class ShoppingListController implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient RestTemplate restTemplate;

    @ManagedProperty(value = "#{shellController}")
    private ShellController shellController;

    private List<ShoppingItemDTO> shoppingItems;
    private List<AlertDTO> alerts;
    private List<CategoryDTO> categories;
    private List<GroupDTO> groups;
    private Long selectedGroupId;
    private String selectedGroupName;
    private String selectedGroupRole;
    private String itemName;
    private BigDecimal itemQuantity = BigDecimal.ONE;
    private String itemUnit;
    private BigDecimal itemEstimatedPrice;
    private Long itemCategoryId;
    private String itemNotes;
    private Long editingItemId;
    private Long editingItemVersion;
    private String categoryName;
    private String categoryColor = CategoryColorOption.defaultValue();
    private Integer categorySortOrder;
    private Long editingCategoryId;
    private Long editingCategoryVersion;

    @PostConstruct
    public void init() {
        this.restTemplate = resolveSpringBean(RestTemplate.class);
        this.shoppingItems = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.alerts = new ArrayList<>();
        loadGroups();
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

    public void selectGroup(Long groupId) {
        GroupDTO selected = null;
        for (GroupDTO group : groups) {
            if (groupId != null && groupId.equals(group.getId())) {
                selected = group;
                break;
            }
        }
        if (selected == null) {
            addError("Select a group you belong to");
            return;
        }
        selectedGroupId = selected.getId();
        selectedGroupName = selected.getName();
        selectedGroupRole = selected.getMyRole();
        startNewItem();
        clearCategoryForm();
        loadData();
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
            addWarning("Select a group before loading the shopping list");
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
        itemUnit = null;
        itemEstimatedPrice = null;
        itemCategoryId = null;
        itemNotes = null;
    }

    public void editItem(ShoppingItemDTO item) {
        if (item == null) return;
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
        Long groupId = getCurrentGroupId();
        if (groupId == null) {
            addError("A group is required");
            return;
        }
        try {
            if (!isSelectedGroupAdmin()) {
                boolean editingRequest = editingItemId != null;
                UpdateShoppingItemRequestDTO request = new UpdateShoppingItemRequestDTO();
                request.setName(itemName);
                request.setQuantity(itemQuantity);
                request.setUnit(itemUnit);
                request.setEstimatedPrice(itemEstimatedPrice);
                request.setCategoryId(itemCategoryId);
                request.setNotes(itemNotes);
                request.setVersion(editingItemVersion);
                String url = editingItemId == null
                    ? requestUrl(groupId) + "/add"
                    : requestUrl(groupId) + "/items/" + editingItemId + "/edit";
                restTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()), Object.class);
                startNewItem();
                addSuccess(editingRequest ? "Edit request submitted" : "Add request submitted");
                return;
            }
            if (editingItemId == null) {
                CreateShoppingItemRequestDTO request = new CreateShoppingItemRequestDTO();
                request.setName(itemName);
                request.setQuantity(itemQuantity);
                request.setUnit(itemUnit);
                request.setEstimatedPrice(itemEstimatedPrice);
                request.setCategoryId(itemCategoryId);
                request.setNotes(itemNotes);
                restTemplate.exchange(
                    itemUrl(groupId), HttpMethod.POST,
                    new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()), ShoppingItemDTO.class);
                loadData();
                addSuccess("Item added");
            } else {
                UpdateShoppingItemRequestDTO request = new UpdateShoppingItemRequestDTO();
                request.setName(itemName);
                request.setQuantity(itemQuantity);
                request.setUnit(itemUnit);
                request.setEstimatedPrice(itemEstimatedPrice);
                request.setCategoryId(itemCategoryId);
                request.setNotes(itemNotes);
                request.setVersion(editingItemVersion);
                restTemplate.exchange(
                    itemUrl(groupId) + "/" + editingItemId, HttpMethod.PUT,
                    new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()), ShoppingItemDTO.class);
                loadData();
                addSuccess("Item updated");
            }
            startNewItem();
        } catch (Exception ex) {
            addError(errorMessage(ex));
            if (isConflict(ex)) loadData();
        }
    }

    public void deleteItem(ShoppingItemDTO item) {
        if (item == null || item.getId() == null) return;
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
            loadData();
            addSuccess("Item deleted");
        } catch (Exception ex) {
            addError(errorMessage(ex));
        }
    }

    public void togglePurchased(ShoppingItemDTO item) {
        if (item == null || item.getId() == null) return;
        try {
            UpdateShoppingItemRequestDTO request = new UpdateShoppingItemRequestDTO();
            request.setVersion(item.getVersion());
            String action = Boolean.TRUE.equals(item.getPurchased()) ? "mark-unpurchased" : "mark-purchased";
            restTemplate.exchange(itemUrl(getCurrentGroupId()) + "/" + item.getId() + "/" + action,
                HttpMethod.POST, new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()), ShoppingItemDTO.class);
            loadData();
            addSuccess(Boolean.TRUE.equals(item.getPurchased()) ? "Item marked as pending" : "Item marked as purchased");
        } catch (Exception ex) {
            addError(errorMessage(ex));
            if (isConflict(ex)) loadData();
        }
    }

    public void createCategory() {
        if (getCurrentGroupId() == null) return;
        try {
            CreateCategoryRequestDTO request = new CreateCategoryRequestDTO();
            request.setName(categoryName);
            request.setColor(categoryColor);
            restTemplate.exchange(categoryUrl(getCurrentGroupId()), HttpMethod.POST,
                new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()), CategoryDTO.class);
            categoryName = null;
            categoryColor = CategoryColorOption.defaultValue();
            loadData();
            addSuccess("Category added");
        } catch (Exception ex) {
            addError(errorMessage(ex));
        }
    }

    public void editCategory(CategoryDTO category) {
        if (category == null) return;
        editingCategoryId = category.getId();
        editingCategoryVersion = category.getVersion();
        categoryName = category.getName();
        categoryColor = category.getColor() == null ? CategoryColorOption.defaultValue() : category.getColor();
        categorySortOrder = category.getSortOrder();
    }

    public void saveCategory() {
        if (editingCategoryId == null) {
            createCategory();
            return;
        }
        try {
            UpdateCategoryRequestDTO request = new UpdateCategoryRequestDTO();
            request.setName(categoryName);
            request.setColor(categoryColor);
            request.setSortOrder(categorySortOrder);
            request.setVersion(editingCategoryVersion);
            restTemplate.exchange(categoryUrl(getCurrentGroupId()) + "/" + editingCategoryId,
                HttpMethod.PUT, new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()), CategoryDTO.class);
            clearCategoryForm();
            loadData();
            addSuccess("Category updated");
        } catch (Exception ex) {
            addError(errorMessage(ex));
            if (isConflict(ex)) loadData();
        }
    }

    public void clearCategoryForm() {
        editingCategoryId = null;
        editingCategoryVersion = null;
        categoryName = null;
        categoryColor = CategoryColorOption.defaultValue();
        categorySortOrder = null;
    }

    public void archiveCategory(CategoryDTO category) {
        if (category == null || category.getId() == null) return;
        try {
            UpdateCategoryRequestDTO request = new UpdateCategoryRequestDTO();
            request.setVersion(category.getVersion());
            restTemplate.exchange(categoryUrl(getCurrentGroupId()) + "/" + category.getId(), HttpMethod.DELETE,
                new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()), Void.class);
            loadData();
            addSuccess("Category archived");
        } catch (Exception ex) {
            addError(errorMessage(ex));
            if (isConflict(ex)) loadData();
        }
    }
    /**
     * Reload the shopping list snapshot from the server.
     */
    public void refreshSnapshot() {
        loadData();
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
        return message == null || message.trim().isEmpty() ? "Request failed" : message;
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
    public List<GroupDTO> getGroups() { return groups == null ? Collections.emptyList() : groups; }
    public Long getSelectedGroupId() { return selectedGroupId; }
    public String getSelectedGroupName() { return selectedGroupName; }
    public String getSelectedGroupRole() { return selectedGroupRole; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getItemQuantity() { return itemQuantity; }
    public void setItemQuantity(BigDecimal itemQuantity) { this.itemQuantity = itemQuantity; }
    public String getItemUnit() { return itemUnit; }
    public void setItemUnit(String itemUnit) { this.itemUnit = itemUnit; }
    public BigDecimal getItemEstimatedPrice() { return itemEstimatedPrice; }
    public void setItemEstimatedPrice(BigDecimal itemEstimatedPrice) { this.itemEstimatedPrice = itemEstimatedPrice; }
    public Long getItemCategoryId() { return itemCategoryId; }
    public void setItemCategoryId(Long itemCategoryId) { this.itemCategoryId = itemCategoryId; }
    public String getItemNotes() { return itemNotes; }
    public void setItemNotes(String itemNotes) { this.itemNotes = itemNotes; }
    public boolean isEditing() { return editingItemId != null; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }
    public String getCategoryColorKey() {
        String color = categoryColor == null || categoryColor.trim().isEmpty()
            ? CategoryColorOption.defaultValue() : categoryColor;
        return color.replace("#", "").toLowerCase();
    }
    public List<CategoryColorOption> getCategoryColorOptions() { return CategoryColorOption.options(); }
    public Integer getCategorySortOrder() { return categorySortOrder; }
    public void setCategorySortOrder(Integer categorySortOrder) { this.categorySortOrder = categorySortOrder; }
    public boolean isEditingCategory() { return editingCategoryId != null; }

    public ShellController getShellController() {
        return shellController;
    }

    public void setShellController(ShellController shellController) {
        this.shellController = shellController;
    }
}
