package com.sharedbasket.controller.rest;

import com.sharedbasket.dto.CreateShoppingItemRequestDTO;
import com.sharedbasket.dto.DecisionNoteRequestDTO;
import com.sharedbasket.dto.ShoppingItemRequestDTO;
import com.sharedbasket.dto.UpdateShoppingItemRequestDTO;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.service.AuthenticationService;
import com.sharedbasket.service.ShoppingItemRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/item-requests")
public class ShoppingItemRequestController {

    private final ShoppingItemRequestService requestService;
    private final AuthenticationService authenticationService;

    public ShoppingItemRequestController(ShoppingItemRequestService requestService,
                                          AuthenticationService authenticationService) {
        this.requestService = requestService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingItemRequestDTO requestAdd(@PathVariable Long groupId,
                                             @RequestBody CreateShoppingItemRequestDTO request) {
        return requestService.requestAdd(getCurrentUserId(), groupId, request);
    }

    @PostMapping("/items/{itemId}/edit")
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingItemRequestDTO requestEdit(@PathVariable Long groupId,
                                              @PathVariable Long itemId,
                                              @RequestBody UpdateShoppingItemRequestDTO request) {
        return requestService.requestEdit(getCurrentUserId(), groupId, itemId, request);
    }

    @PostMapping("/items/{itemId}/delete")
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingItemRequestDTO requestDelete(@PathVariable Long groupId,
                                                @PathVariable Long itemId,
                                                @RequestBody UpdateShoppingItemRequestDTO request) {
        return requestService.requestDelete(getCurrentUserId(), groupId, itemId,
            request == null ? null : request.getVersion());
    }

    @GetMapping("/pending")
    public List<ShoppingItemRequestDTO> listPending(@PathVariable Long groupId) {
        return requestService.listPending(getCurrentUserId(), groupId);
    }

    @GetMapping("/mine")
    public List<ShoppingItemRequestDTO> listMine(@PathVariable Long groupId) {
        return requestService.listMine(getCurrentUserId(), groupId);
    }

    @PostMapping("/{requestId}/approve")
    public ShoppingItemRequestDTO approve(@PathVariable Long groupId, @PathVariable Long requestId) {
        return requestService.approve(getCurrentUserId(), groupId, requestId);
    }

    @PostMapping("/{requestId}/reject")
    public ShoppingItemRequestDTO reject(@PathVariable Long groupId,
                                         @PathVariable Long requestId,
                                         @RequestBody(required = false) DecisionNoteRequestDTO request) {
        return requestService.reject(getCurrentUserId(), groupId, requestId,
            request == null ? null : request.getDecisionNote());
    }

    private Long getCurrentUserId() {
        if (authenticationService == null || authenticationService.getCurrentUser() == null
            || authenticationService.getCurrentUser().getId() == null) {
            throw new ForbiddenOperationException("No authenticated user in session");
        }
        return authenticationService.getCurrentUser().getId();
    }
}
