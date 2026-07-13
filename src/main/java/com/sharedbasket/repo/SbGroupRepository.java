package com.sharedbasket.repo;

import com.sharedbasket.domain.SbGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SbGroupRepository extends JpaRepository<SbGroup, Long> {
	Optional<SbGroup> findByInviteCode(String inviteCode);
}