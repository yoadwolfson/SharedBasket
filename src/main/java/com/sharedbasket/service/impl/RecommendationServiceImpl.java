package com.sharedbasket.service.impl;

import com.sharedbasket.domain.SbGroup;
import com.sharedbasket.domain.ShoppingItem;
import com.sharedbasket.domain.User;
import com.sharedbasket.dto.RebuildRequestDTO;
import com.sharedbasket.dto.RebuildResultDTO;
import com.sharedbasket.dto.RecommendationDTO;
import com.sharedbasket.exception.BadRequestException;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.exception.ResourceNotFoundException;
import com.sharedbasket.repo.CategoryRepository;
import com.sharedbasket.repo.GroupMemberRepository;
import com.sharedbasket.repo.PurchaseRecordRepository;
import com.sharedbasket.repo.SbGroupRepository;
import com.sharedbasket.repo.ShoppingItemRepository;
import com.sharedbasket.repo.UserRepository;
import com.sharedbasket.service.RecommendationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecommendationServiceImpl implements RecommendationService {

    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final int LOOKBACK_DAYS = 90;
    private static final int MAX_RECOMMENDATIONS = 10;

    private final PurchaseRecordRepository purchaseRecordRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CategoryRepository categoryRepository;
    private final SbGroupRepository groupRepository;
    private final UserRepository userRepository;

    public RecommendationServiceImpl(PurchaseRecordRepository purchaseRecordRepository,
                                      ShoppingItemRepository shoppingItemRepository,
                                      GroupMemberRepository groupMemberRepository,
                                      CategoryRepository categoryRepository,
                                      SbGroupRepository groupRepository,
                                      UserRepository userRepository) {
        this.purchaseRecordRepository = purchaseRecordRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.categoryRepository = categoryRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationDTO> getRecommendations(Long currentUserId, Long groupId) {
        requireGroupId(groupId);
        requireMember(groupId, currentUserId);
        return buildRecommendations(groupId);
    }

    @Override
    public RebuildResultDTO rebuildFromRecommendations(Long currentUserId, Long groupId, RebuildRequestDTO request) {
        requireGroupId(groupId);
        requireMember(groupId, currentUserId);
        if (request == null || request.getItemNames() == null || request.getItemNames().isEmpty()) {
            throw new BadRequestException("At least one item name is required");
        }

        SbGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<String> existingUnpurchasedNames = shoppingItemRepository
            .findByShoppingGroup_IdAndPurchasedFalse(groupId)
            .stream()
            .map(item -> item.getName().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

        List<RecommendationDTO> recommendations = buildRecommendations(groupId);

        int created = 0;
        int skipped = 0;
        List<String> conflicts = new ArrayList<>();

        for (String itemName : request.getItemNames()) {
            if (itemName == null || itemName.trim().isEmpty()) {
                continue;
            }
            String normalized = itemName.trim();
            if (existingUnpurchasedNames.contains(normalized.toLowerCase(Locale.ROOT))) {
                skipped++;
                conflicts.add(normalized);
                continue;
            }

            RecommendationDTO match = recommendations.stream()
                .filter(rec -> rec.getItemName().equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(null);

            ShoppingItem item = new ShoppingItem();
            item.setShoppingGroup(group);
            item.setName(normalized);
            item.setQuantity(BigDecimal.ONE);
            item.setPurchased(Boolean.FALSE);
            item.setCreatedByUser(currentUser);
            item.setCreatedAt(Instant.now());
            item.setUpdatedAt(Instant.now());
            if (match != null) {
                item.setEstimatedPrice(match.getEstimatedUnitPrice());
                categoryRepository.findByShoppingGroup_IdAndName(groupId, match.getCategoryName())
                    .ifPresent(item::setCategory);
            }
            shoppingItemRepository.save(item);
            created++;
        }

        return new RebuildResultDTO(created, skipped, conflicts);
    }
    /**
     * Builds a list of recommendations for a given group based on purchase history and current shopping list.
     * @param groupId The ID of the group for which recommendations are being built
     * @return A list of RecommendationDTO
     */
    private List<RecommendationDTO> buildRecommendations(Long groupId) {
        Instant since = Instant.now().minus(LOOKBACK_DAYS, ChronoUnit.DAYS);
        Instant now = Instant.now();
        List<Object[]> rows = purchaseRecordRepository.findFrequentItemsSince(groupId, since);

        Set<String> alreadyOnList = shoppingItemRepository
            .findByShoppingGroup_IdAndPurchasedFalse(groupId)
            .stream()
            .map(item -> item.getName().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

        return rows.stream()
            .filter(row -> !alreadyOnList.contains(((String) row[0]).toLowerCase(Locale.ROOT)))
            .map(row -> toRecommendationDTO(row, since, now))
            // Stable ordering improves API predictability when frequencies tie.
            .sorted((a, b) -> {
                int byFrequency = Integer.compare(b.getFrequency(), a.getFrequency());
                return byFrequency != 0 ? byFrequency : a.getItemName().compareToIgnoreCase(b.getItemName());
            })
            .limit(MAX_RECOMMENDATIONS)
            .collect(Collectors.toList());
    }
    /**
     * Converts a database row into a RecommendationDTO, including rationale based on frequency and last purchase date.
     * @param row The database row containing item name, frequency, average price, last purchased date, and category name
     * @param since The start of the lookback period for recommendations
     * @param now The current timestamp for the end of the lookback period
     * @return A RecommendationDTO 
     */
    private RecommendationDTO toRecommendationDTO(Object[] row, Instant since, Instant now) {
        String itemName = (String) row[0];
        long frequency = (Long) row[1];
        // avg() in JPQL returns Double even though pricePaid is BigDecimal
        BigDecimal avgPrice = BigDecimal.valueOf(((Number) row[2]).doubleValue()).setScale(2, java.math.RoundingMode.HALF_UP);
        Instant lastPurchasedAt = (Instant) row[3];
        String categoryName = (String) row[4];

        RecommendationDTO dto = new RecommendationDTO();
        dto.setRecommendationId(itemName);
        dto.setItemName(itemName);
        dto.setCategory(categoryName);
        dto.setCategoryName(categoryName);
        dto.setFrequency((int) frequency);
        dto.setEstimatedUnitPrice(avgPrice);
        dto.setEstimatedTotalContribution(avgPrice);
        dto.setSourceWindowStart(since);
        dto.setSourceWindowEnd(now);
        dto.setRationale(buildRationale(frequency, lastPurchasedAt));
        return dto;
    }
    /**
     * Builds a rationale string for a recommendation based on its frequency and last purchase date.
     * @param frequency The number of times the item was purchased in the lookback period
     * @param lastPurchasedAt The timestamp of the last purchase of the item
     * @return A  string describing the recommendation
     */ 
    private String buildRationale(long frequency, Instant lastPurchasedAt) {
        long daysSinceLastPurchase = lastPurchasedAt == null
            ? -1
            : ChronoUnit.DAYS.between(lastPurchasedAt, Instant.now());
        StringBuilder sb = new StringBuilder("Bought ").append(frequency)
            .append(frequency == 1 ? " time" : " times")
            .append(" in the last ").append(LOOKBACK_DAYS).append(" days");
        if (daysSinceLastPurchase == 0) {
            sb.append(", last purchased today");
        } else if (daysSinceLastPurchase > 0) {
            sb.append(", last purchased ").append(daysSinceLastPurchase).append(" days ago");
        }
        return sb.toString();
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

    private void requireGroupId(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group id is required");
        }
    }
}
