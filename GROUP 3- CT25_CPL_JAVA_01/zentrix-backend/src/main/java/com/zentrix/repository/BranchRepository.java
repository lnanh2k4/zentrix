package com.zentrix.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.zentrix.model.entity.Branch;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Branch entities in the database.
 * Extends JpaRepository to provide basic CRUD operations and custom query methods.
 *
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date March 17, 2025
 */
@Repository // Marks this interface as a Spring Data repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    /**
     * Retrieves a paginated list of all branches.
     *
     * @param pageable Pagination and sorting information (e.g., page number, size, sort order).
     * @return A Page object containing a list of Branch entities for the specified page.
     */
    Page<Branch> findAll(Pageable pageable);

    /**
     * Finds a branch by its exact name.
     *
     * @param brchName The name of the branch to search for.
     * @return An Optional containing the Branch entity if found, or empty if not found.
     */
    Optional<Branch> findByBrchName(String brchName);

    /**
     * Finds branches whose names contain the specified keyword, ignoring case.
     *
     * @param keyword The keyword to search for within branch names.
     * @return A List of Branch entities matching the keyword (partial match, case-insensitive).
     */
    List<Branch> findByBrchNameContainingIgnoreCase(String keyword);
}