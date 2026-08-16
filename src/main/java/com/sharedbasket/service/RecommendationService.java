package com.sharedbasket.service;

import com.sharedbasket.dto.RebuildRequestDTO;
import com.sharedbasket.dto.RebuildResultDTO;
import com.sharedbasket.dto.RecommendationDTO;

import java.util.List;

public interface RecommendationService {
    /**
     * Fetches recommendations for a given group and user.
     * @param currentUserId The ID of the current user making the request
     * @param groupId The ID of the group for which recommendations are being requested
     * @return A list of RecommendationDTO
     */
    List<RecommendationDTO> getRecommendations(Long currentUserId, Long groupId);
    /**
     * Rebuilds recommendations for a given group based on the provided rebuild request.
     * @param currentUserId The ID of the current user making the request
     * @param groupId The ID of the group for which recommendations are being rebuilt
     * @param request The rebuild request containing necessary parameters
     * @return A RebuildResultDTO 
     */
    RebuildResultDTO rebuildFromRecommendations(Long currentUserId, Long groupId, RebuildRequestDTO request);
}
