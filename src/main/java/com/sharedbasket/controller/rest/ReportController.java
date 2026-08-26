package com.sharedbasket.controller.rest;

import com.sharedbasket.dto.PurchaseHistoryEntryDTO;
import com.sharedbasket.dto.SpendingByCategoryDTO;
import com.sharedbasket.dto.SpendingByProductDTO;
import com.sharedbasket.dto.StatisticsDTO;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.service.AuthenticationService;
import com.sharedbasket.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final int DEFAULT_LOOKBACK_DAYS = 30;

    private final ReportService reportService;
    private final AuthenticationService authenticationService;

    public ReportController(ReportService reportService, AuthenticationService authenticationService) {
        this.reportService = reportService;
        this.authenticationService = authenticationService;
    }

    /**
     * Returns top-level spend and purchase counts for a group within a time window.
        *
        * @param groupId group whose purchases should be summarized
        * @param from optional start of the reporting window
        * @param to optional end of the reporting window
        * @return summary statistics for the resolved reporting window
     */
    @GetMapping("/statistics")
    public StatisticsDTO getStatistics(
            @RequestParam Long groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return reportService.getStatistics(getCurrentUserId(), groupId, resolveFrom(from), resolveTo(to));
    }

    /**
     * Returns aggregated spending grouped by category for the selected group and period.
        *
        * @param groupId group whose purchases should be grouped
        * @param from optional start of the reporting window
        * @param to optional end of the reporting window
        * @return spending totals grouped by category
     */
    @GetMapping("/spending-by-category")
    public List<SpendingByCategoryDTO> getSpendingByCategory(
            @RequestParam Long groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return reportService.getSpendingByCategory(getCurrentUserId(), groupId, resolveFrom(from), resolveTo(to));
    }

    /**
     * Returns aggregated spending grouped by product for the selected group and period.
        *
        * @param groupId group whose purchases should be grouped
        * @param from optional start of the reporting window
        * @param to optional end of the reporting window
        * @return spending totals grouped by product
     */
    @GetMapping("/spending-by-product")
    public List<SpendingByProductDTO> getSpendingByProduct(
            @RequestParam Long groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return reportService.getSpendingByProduct(getCurrentUserId(), groupId, resolveFrom(from), resolveTo(to));
    }

    /**
     * Returns detailed purchase history rows for timeline and auditing views.
        *
        * @param groupId group whose purchase history should be returned
        * @param from optional start of the reporting window
        * @param to optional end of the reporting window
        * @return purchase history ordered from newest to oldest
     */
    @GetMapping("/purchase-history")
    public List<PurchaseHistoryEntryDTO> getPurchaseHistory(
            @RequestParam Long groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return reportService.getPurchaseHistory(getCurrentUserId(), groupId, resolveFrom(from), resolveTo(to));
    }

    private Instant resolveFrom(Instant from) {
        // Keeping the default window at the API boundary gives every report endpoint the same period.
        return from != null ? from : Instant.now().minus(DEFAULT_LOOKBACK_DAYS, ChronoUnit.DAYS);
    }

    private Instant resolveTo(Instant to) {
        return to != null ? to : Instant.now();
    }

    private Long getCurrentUserId() {
        // Services receive an explicit user id so authorization remains testable and independent of HTTP details.
        if (authenticationService == null
                || authenticationService.getCurrentUser() == null
                || authenticationService.getCurrentUser().getId() == null) {
            throw new ForbiddenOperationException("No authenticated user in session");
        }
        return authenticationService.getCurrentUser().getId();
    }
}
