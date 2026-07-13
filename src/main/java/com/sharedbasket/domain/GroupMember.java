package com.sharedbasket.domain;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "group_member")
public class GroupMember {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false)
	private SbGroup shoppingGroup;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String role = "MEMBER";

	@Column(name = "joined_at", nullable = false)
	private Instant joinedAt = Instant.now();

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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}

	public void setJoinedAt(Instant joinedAt) {
		this.joinedAt = joinedAt;
	}
}