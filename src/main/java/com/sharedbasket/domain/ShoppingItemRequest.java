package com.sharedbasket.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "shopping_item_request")
public class ShoppingItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SbGroup shoppingGroup;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_item_id")
    private ShoppingItem shoppingItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by_user_id")
    private User decidedByUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShoppingItemRequestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShoppingItemRequestStatus status = ShoppingItemRequestStatus.PENDING;

    @Column(name = "item_version")
    private Long itemVersion;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity = BigDecimal.ONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 20)
    private ItemUnit unit = ItemUnit.PIECE;

    @Column(name = "estimated_price", precision = 12, scale = 2)
    private BigDecimal estimatedPrice;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decision_note", columnDefinition = "TEXT")
    private String decisionNote;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SbGroup getShoppingGroup() { return shoppingGroup; }
    public void setShoppingGroup(SbGroup shoppingGroup) { this.shoppingGroup = shoppingGroup; }
    public User getRequestedByUser() { return requestedByUser; }
    public void setRequestedByUser(User requestedByUser) { this.requestedByUser = requestedByUser; }
    public ShoppingItem getShoppingItem() { return shoppingItem; }
    public void setShoppingItem(ShoppingItem shoppingItem) { this.shoppingItem = shoppingItem; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public User getDecidedByUser() { return decidedByUser; }
    public void setDecidedByUser(User decidedByUser) { this.decidedByUser = decidedByUser; }
    public ShoppingItemRequestType getType() { return type; }
    public void setType(ShoppingItemRequestType type) { this.type = type; }
    public ShoppingItemRequestStatus getStatus() { return status; }
    public void setStatus(ShoppingItemRequestStatus status) { this.status = status; }
    public Long getItemVersion() { return itemVersion; }
    public void setItemVersion(Long itemVersion) { this.itemVersion = itemVersion; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public ItemUnit getUnit() { return unit; }
    public void setUnit(ItemUnit unit) { this.unit = unit; }
    public BigDecimal getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(BigDecimal estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
}
