package com.sharedbasket.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "purchase_record")
public class PurchaseRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false)
	private SbGroup shoppingGroup;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shopping_item_id")
	private ShoppingItem shoppingItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private Category category;

	@Column(name = "item_name", nullable = false)
	private String itemName;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "purchased_by_user_id", nullable = false)
	private User purchasedByUser;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity = BigDecimal.ONE;

	@Column
	private String unit;

	@Column(name = "price_paid", precision = 12, scale = 2)
	private BigDecimal pricePaid;

	@Column(name = "purchased_at", nullable = false)
	private Instant purchasedAt = Instant.now();

	@Column(columnDefinition = "TEXT")
	private String notes;

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

	public ShoppingItem getShoppingItem() {
		return shoppingItem;
	}

	public void setShoppingItem(ShoppingItem shoppingItem) {
		this.shoppingItem = shoppingItem;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public User getPurchasedByUser() {
		return purchasedByUser;
	}

	public void setPurchasedByUser(User purchasedByUser) {
		this.purchasedByUser = purchasedByUser;
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

	public BigDecimal getPricePaid() {
		return pricePaid;
	}

	public void setPricePaid(BigDecimal pricePaid) {
		this.pricePaid = pricePaid;
	}

	public Instant getPurchasedAt() {
		return purchasedAt;
	}

	public void setPurchasedAt(Instant purchasedAt) {
		this.purchasedAt = purchasedAt;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}