package com.sharedbasket.repo;

import com.sharedbasket.domain.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
	List<GroupMember> findByShoppingGroup_Id(Long groupId);
	List<GroupMember> findByUser_Id(Long userId);
}