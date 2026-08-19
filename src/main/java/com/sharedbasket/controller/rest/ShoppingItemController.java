package com.sharedbasket.controller.rest;

import com.sharedbasket.dto.CreateShoppingItemRequestDTO;
import com.sharedbasket.dto.ShoppingItemDTO;
import com.sharedbasket.dto.UpdateShoppingItemRequestDTO;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.service.AuthenticationService;
import com.sharedbasket.service.ShoppingItemService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/items")
public class ShoppingItemController {

    private final ShoppingItemService shoppingItemService;
    private final AuthenticationService authenticationService;

    public ShoppingItemController(ShoppingItemService shoppingItemService, AuthenticationService authenticationService) {
        this.shoppingItemService = shoppingItemService;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingItemDTO create(@PathVariable Long groupId, @RequestBody CreateShoppingItemRequestDTO request) {
        return shoppingItemService.createItem(getCurrentUserId(), groupId, request);
    }

    @GetMapping
    public List<ShoppingItemDTO> list(@PathVariable Long groupId,
                                      @RequestParam(required = false) Boolean purchased) {
        return shoppingItemService.listItems(getCurrentUserId(), groupId, purchased);
    }

    @PutMapping("/{itemId}")
    public ShoppingItemDTO update(@PathVariable Long groupId,
                                  @PathVariable Long itemId,
                                  @RequestBody UpdateShoppingItemRequestDTO request) {
        return shoppingItemService.updateItem(getCurrentUserId(), groupId, itemId, request);
    }

    @PostMapping("/{itemId}/mark-purchased")
    public ShoppingItemDTO markPurchased(@PathVariable Long groupId,
                                          @PathVariable Long itemId,
                                          @RequestBody UpdateShoppingItemRequestDTO request) {
        return shoppingItemService.togglePurchased(getCurrentUserId(), groupId, itemId,
            request == null ? null : request.getVersion(), true);
    }

    @PostMapping("/{itemId}/mark-unpurchased")
    public ShoppingItemDTO markUnpurchased(@PathVariable Long groupId,
                                            @PathVariable Long itemId,
                                            @RequestBody UpdateShoppingItemRequestDTO request) {
        return shoppingItemService.togglePurchased(getCurrentUserId(), groupId, itemId,
            request == null ? null : request.getVersion(), false);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long groupId, @PathVariable Long itemId) {
        shoppingItemService.deleteItem(getCurrentUserId(), groupId, itemId);
    }

    private Long getCurrentUserId() {
        if (authenticationService == null || authenticationService.getCurrentUser() == null
            || authenticationService.getCurrentUser().getId() == null) {
            throw new ForbiddenOperationException("No authenticated user in session");
        }
        return authenticationService.getCurrentUser().getId();
    }
}
