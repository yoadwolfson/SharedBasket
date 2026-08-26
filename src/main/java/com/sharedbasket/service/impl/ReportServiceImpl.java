package com.sharedbasket.service.impl;

import com.sharedbasket.domain.PurchaseRecord;
import com.sharedbasket.dto.PurchaseHistoryEntryDTO;
import com.sharedbasket.dto.SpendingByCategoryDTO;
import com.sharedbasket.dto.SpendingByProductDTO;
import com.sharedbasket.dto.StatisticsDTO;
import com.sharedbasket.exception.BadRequestException;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.repo.GroupMemberRepository;
import com.sharedbasket.repo.PurchaseRecordRepository;
import com.sharedbasket.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final String STATUS_ACCEPTED = "ACCEPTED";

    private final PurchaseRecordRepository purchaseRecordRepository;
    private final GroupMemberRepository groupMemberRepository;

    public ReportServiceImpl(PurchaseRecordRepository purchaseRecordRepository,
                              GroupMemberRepository groupMemberRepository) {
        this.purchaseRecordRepository = purchaseRecordRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Override
    public StatisticsDTO getStatistics(Long currentUserId, Long groupId, Instant from, Instant to) {
        // Reports read immutable purchase snapshots so later item edits cannot change historical totals.
        requireReportInput(groupId);
        Instant[] window = normalizeWindow(from, to);
        requireMember(groupId, currentUserId);
        BigDecimal totalCost = purchaseRecordRepository.sumSpendByGroupAndDateRange(groupId, window[0], window[1]);
        long itemsPurchased = purchaseRecordRepository.countByGroupAndDateRange(groupId, window[0], window[1]);

        StatisticsDTO dto = new StatisticsDTO();
        dto.setTotalItems((int) itemsPurchased);
        dto.setTotalCost(totalCost.doubleValue());
        dto.setItemsPurchased((int) itemsPurchased);
        dto.setPeriodStart(window[0]);
        dto.setPeriodEnd(window[1]);
        dto.setCurrency("NIS");
        return dto;
    }

    @Override
    public List<SpendingByCategoryDTO> getSpendingByCategory(Long currentUserId, Long groupId, Instant from, Instant to) {
        requireReportInput(groupId);
        Instant[] window = normalizeWindow(from, to);
        requireMember(groupId, currentUserId);
        List<Object[]> rows = purchaseRecordRepository.sumSpendByCategory(groupId, window[0], window[1]);
        BigDecimal total = rows.stream()
            .map(row -> (BigDecimal) row[1])
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream()
            .map(row -> new SpendingByCategoryDTO(
                (String) row[0],
                (BigDecimal) row[1],
                (BigDecimal) row[2],
                percentageOf((BigDecimal) row[1], total)))
            .collect(Collectors.toList());
    }

    @Override
    public List<SpendingByProductDTO> getSpendingByProduct(Long currentUserId, Long groupId, Instant from, Instant to) {
        requireReportInput(groupId);
        Instant[] window = normalizeWindow(from, to);
        requireMember(groupId, currentUserId);
        List<Object[]> rows = purchaseRecordRepository.sumSpendByProduct(groupId, window[0], window[1]);
        BigDecimal total = rows.stream()
            .map(row -> (BigDecimal) row[1])
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream()
            .map(row -> new SpendingByProductDTO(
                (String) row[0],
                (BigDecimal) row[1],
                (BigDecimal) row[2],
                percentageOf((BigDecimal) row[1], total)))
            .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseHistoryEntryDTO> getPurchaseHistory(Long currentUserId, Long groupId, Instant from, Instant to) {
        requireReportInput(groupId);
        Instant[] window = normalizeWindow(from, to);
        requireMember(groupId, currentUserId);
        return purchaseRecordRepository
            .findByShoppingGroup_IdAndPurchasedAtBetweenOrderByPurchasedAtDesc(groupId, window[0], window[1])
            .stream()
            .map(this::toHistoryEntry)
            .collect(Collectors.toList());
    }
/**
     * Converts a PurchaseRecord entity to a PurchaseHistoryEntryDTO.
     * @param record The PurchaseRecord entity to convert
     * @return A PurchaseHistoryEntryDTO representing the purchase record
     */
    private PurchaseHistoryEntryDTO toHistoryEntry(PurchaseRecord record) {
        PurchaseHistoryEntryDTO dto = new PurchaseHistoryEntryDTO();
        dto.setId(record.getId());
        dto.setItemName(record.getItemName());
        dto.setCategoryName(record.getCategory() != null ? record.getCategory().getName() : "Uncategorized");
        dto.setQuantity(record.getQuantity());
        dto.setUnit(record.getUnit());
        dto.setPricePaid(record.getPricePaid());
        dto.setPurchasedAt(record.getPurchasedAt());
        dto.setPurchasedByUserName(record.getPurchasedByUser() != null
            ? record.getPurchasedByUser().getUsername() : null);
        return dto;
    }
    /**
     * Calculates the percentage of a given amount relative to a total, returning 0 if the total is null or zero.
     * @param amount The amount to calculate the percentage for
     * @param total The total amount relative to which the percentage is calculated
     * @return The percentage of the amount from the total.
     */
    private double percentageOf(BigDecimal amount, BigDecimal total) {
        // An empty period has no meaningful denominator, so its percentages are reported as zero.
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return 0d;
        }
        return amount.multiply(BigDecimal.valueOf(100))
            .divide(total, 2, RoundingMode.HALF_UP)
            .doubleValue();
    }
    /**
     * Checks if a user is a member of a group and throws a ForbiddenOperationException if not.
     * @param groupId The ID of the group to check membership for
     * @param userId The ID of the user to check membership for
     */
    private void requireMember(Long groupId, Long userId) {
        boolean isMember = groupMemberRepository
            .existsByShoppingGroup_IdAndUser_IdAndStatus(groupId, userId, STATUS_ACCEPTED);
        if (!isMember) {
            throw new ForbiddenOperationException("You are not a member of this group");
        }
    }

    /**
     * Guards report calls with a minimal shared validation contract.
     */
    private void requireReportInput(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group id is required");
        }
    }

    /**
     * Normalizes a date window and rejects invalid ranges early to keep report behavior deterministic.
     */
    private Instant[] normalizeWindow(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new BadRequestException("Both from and to are required");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("from must be earlier than or equal to to");
        }
        return Arrays.copyOf(new Instant[] { from, to }, 2);
    }
}
