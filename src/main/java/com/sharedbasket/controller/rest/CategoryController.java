package com.sharedbasket.controller.rest;

import com.sharedbasket.dto.CategoryDTO;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.service.AuthenticationService;
import com.sharedbasket.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthenticationService authenticationService;

    public CategoryController(CategoryService categoryService, AuthenticationService authenticationService) {
        this.categoryService = categoryService;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public List<CategoryDTO> list(@PathVariable Long groupId,
                                  @RequestParam(defaultValue = "false") boolean includeArchived) {
        return categoryService.listCategories(getCurrentUserId(), groupId, includeArchived);
    }

    private Long getCurrentUserId() {
        if (authenticationService == null || authenticationService.getCurrentUser() == null
            || authenticationService.getCurrentUser().getId() == null) {
            throw new ForbiddenOperationException("No authenticated user in session");
        }
        return authenticationService.getCurrentUser().getId();
    }
}
