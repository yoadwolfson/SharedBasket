package com.sharedbasket.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "shopping_item")
public class ShoppingItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false)
	private SbGroup shoppingGroup;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private Category category;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity = BigDecimal.ONE;

	@Column
	private String unit;

	@Column(name = "estimated_price", precision = 12, scale = 2)
	private BigDecimal estimatedPrice;

	@Column(nullable = false)
	private Boolean purchased = Boolean.FALSE;

	@Version
	@Column(nullable = false)
	private Long version = 0L;

	@Column(name = "purchased_at")
	private Instant purchasedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "purchased_by_user_id")
	private User purchasedByUser;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_user_id", nullable = false)
	private User createdByUser;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SbGroup getShoppingGroup() {
		return shoppingGroup;
	}

	public void setShoppingGroup(SbGroup shoppingGroup) {
		this.shoppingGroup = shoppingGroup;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public BigDecimal getEstimatedPrice() {
		return estimatedPrice;
	}

	public void setEstimatedPrice(BigDecimal estimatedPrice) {
		this.estimatedPrice = estimatedPrice;
	}

	public Boolean getPurchased() {
		return purchased;
	}

	public void setPurchased(Boolean purchased) {
		this.purchased = purchased;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Instant getPurchasedAt() {
		return purchasedAt;
	}

	public void setPurchasedAt(Instant purchasedAt) {
		this.purchasedAt = purchasedAt;
	}

	public User getPurchasedByUser() {
		return purchasedByUser;
	}

	public void setPurchasedByUser(User purchasedByUser) {
		this.purchasedByUser = purchasedByUser;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public User getCreatedByUser() {
		return createdByUser;
	}

	public void setCreatedByUser(User createdByUser) {
		this.createdByUser = createdByUser;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}