package com.sharedbasket.service;

import com.sharedbasket.dto.PurchaseHistoryEntryDTO;
import com.sharedbasket.dto.SpendingByCategoryDTO;
import com.sharedbasket.dto.SpendingByProductDTO;
import com.sharedbasket.dto.StatisticsDTO;

import java.time.Instant;
import java.util.List;

public interface ReportService {
    /**
     * Fetches statistics for a given group within the specified time range.
     * @param currentUserId The ID of the current user making the request
     * @param groupId The ID of the group for which statistics are being requested
     * @param from The start of the time range for the statistics
     * @param to The end of the time range for the statistics
     * @return A StatisticsDTO
     */
    StatisticsDTO getStatistics(Long currentUserId, Long groupId, Instant from, Instant to);
    /**
     * Fetches spending data grouped by category for a given group within the specified time range.
     * @param currentUserId The ID of the current user making the request
     * @param groupId The ID of the group for which spending data is being requested
     * @param from The start of the time range for the spending data
     * @param to The end of the time range for the spending data
     * @return A list of SpendingByCategoryDTO
     */
    List<SpendingByCategoryDTO> getSpendingByCategory(Long currentUserId, Long groupId, Instant from, Instant to);
    /**
     * Fetches spending data grouped by product for a given group within the specified time range.
     * @param currentUserId The ID of the current user making the request
     * @param groupId  The ID of the group for which spending data is being requested
     * @param from The start of the time range for the spending data
     * @param to The end of the time range for the spending data
     * @return A list of SpendingByProductDTO
     */
    List<SpendingByProductDTO> getSpendingByProduct(Long currentUserId, Long groupId, Instant from, Instant to);

    /**
     * Fetches the purchase history for a given group within the specified time range.
     * @param currentUserId The ID of the current user making the request
     * @param groupId The ID of the group for which the purchase history is being requested
     * @param from The start of the time range for the purchase history
     * @param to The end of the time range for the purchase history
     * @return A list of PurchaseHistoryEntryDTO
     */
    List<PurchaseHistoryEntryDTO> getPurchaseHistory(Long currentUserId, Long groupId, Instant from, Instant to);
}
