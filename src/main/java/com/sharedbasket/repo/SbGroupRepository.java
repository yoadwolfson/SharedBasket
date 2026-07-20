package com.sharedbasket.repo;

import com.sharedbasket.domain.SbGroup;
import com.sharedbasket.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SbGroupRepository extends JpaRepository<SbGroup, Long> {
	Optional<SbGroup> findByInviteCode(String inviteCode);
	List<SbGroup> findByOwnerUser(User ownerUser);
}