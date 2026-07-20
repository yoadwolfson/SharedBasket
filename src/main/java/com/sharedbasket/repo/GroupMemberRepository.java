package com.sharedbasket.repo;

import com.sharedbasket.domain.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
	List<GroupMember> findByShoppingGroup_Id(Long groupId);
	List<GroupMember> findByUser_Id(Long userId);
	List<GroupMember> findByShoppingGroup_IdAndStatus(Long groupId, String status);
	List<GroupMember> findByUser_IdAndStatus(Long userId, String status);
	Optional<GroupMember> findByShoppingGroup_IdAndUser_Id(Long groupId, Long userId);
	boolean existsByShoppingGroup_IdAndUser_IdAndStatus(Long groupId, Long userId, String status);
	long countByShoppingGroup_IdAndRoleAndStatus(Long groupId, String role, String status);
}