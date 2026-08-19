package com.sharedbasket.controller.rest;

import com.sharedbasket.dto.CategoryDTO;
import com.sharedbasket.dto.CreateCategoryRequestDTO;
import com.sharedbasket.dto.UpdateCategoryRequestDTO;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.service.AuthenticationService;
import com.sharedbasket.service.CategoryService;
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
@RequestMapping("/api/groups/{groupId}/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthenticationService authenticationService;

    public CategoryController(CategoryService categoryService, AuthenticationService authenticationService) {
        this.categoryService = categoryService;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDTO create(@PathVariable Long groupId, @RequestBody CreateCategoryRequestDTO request) {
        return categoryService.createCategory(getCurrentUserId(), groupId, request);
    }

    @GetMapping
    public List<CategoryDTO> list(@PathVariable Long groupId,
                                  @RequestParam(defaultValue = "false") boolean includeArchived) {
        return categoryService.listCategories(getCurrentUserId(), groupId, includeArchived);
    }

    @PutMapping("/{categoryId}")
    public CategoryDTO update(@PathVariable Long groupId,
                              @PathVariable Long categoryId,
                              @RequestBody UpdateCategoryRequestDTO request) {
        return categoryService.updateCategory(getCurrentUserId(), groupId, categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(@PathVariable Long groupId,
                        @PathVariable Long categoryId,
                        @RequestBody(required = false) UpdateCategoryRequestDTO request) {
        categoryService.archiveCategory(getCurrentUserId(), groupId, categoryId,
            request == null ? null : request.getVersion());
    }

    private Long getCurrentUserId() {
        if (authenticationService == null || authenticationService.getCurrentUser() == null
            || authenticationService.getCurrentUser().getId() == null) {
            throw new ForbiddenOperationException("No authenticated user in session");
        }
        return authenticationService.getCurrentUser().getId();
    }
}
