package com.sharedbasket.repo;

import com.sharedbasket.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByShoppingGroup_Id(Long groupId);
	List<Category> findByShoppingGroup_IdOrderBySortOrderAscNameAsc(Long groupId);
	List<Category> findByShoppingGroup_IdAndArchivedFalseOrderBySortOrderAscNameAsc(Long groupId);
	Optional<Category> findByShoppingGroup_IdAndName(Long groupId, String name);
	Optional<Category> findByIdAndShoppingGroup_Id(Long categoryId, Long groupId);
}