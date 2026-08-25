package com.sharedbasket.controller.rest;

import com.sharedbasket.dto.RebuildRequestDTO;
import com.sharedbasket.dto.RebuildResultDTO;
import com.sharedbasket.dto.RecommendationDTO;
import com.sharedbasket.exception.BadRequestException;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.service.AuthenticationService;
import com.sharedbasket.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final AuthenticationService authenticationService;

    public RecommendationController(RecommendationService recommendationService,
                                     AuthenticationService authenticationService) {
        this.recommendationService = recommendationService;
        this.authenticationService = authenticationService;
    }

    /**
     * Returns history-driven recommendations for the caller's group.
     */
    @GetMapping
    public List<RecommendationDTO> getRecommendations(@RequestParam Long groupId) {
        return recommendationService.getRecommendations(getCurrentUserId(), groupId);
    }

    /**
     * Rebuilds shopping-list items from selected recommendation item names.
     * The request group must be present because this endpoint is not group-path scoped.
     */
    @PostMapping("/rebuild")
    public RebuildResultDTO rebuild(@RequestBody RebuildRequestDTO request) {
        if (request == null || request.getGroupId() == null) {
            throw new BadRequestException("groupId is required in rebuild request");
        }
        return recommendationService.rebuildFromRecommendations(
            getCurrentUserId(), request.getGroupId(), request);
    }

    private Long getCurrentUserId() {
        if (authenticationService == null
                || authenticationService.getCurrentUser() == null
                || authenticationService.getCurrentUser().getId() == null) {
            throw new ForbiddenOperationException("No authenticated user in session");
        }
        return authenticationService.getCurrentUser().getId();
    }
}
