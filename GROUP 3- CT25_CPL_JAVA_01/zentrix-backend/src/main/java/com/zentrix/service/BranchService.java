package com.zentrix.service;

import java.util.List;
import com.zentrix.model.entity.Branch;
import com.zentrix.model.entity.ProductTypeBranch;
import com.zentrix.model.response.PaginationWrapper;

/**
 * Service interface defining business logic for managing Branch entities.
 * Provides methods for retrieving, creating, updating, and deleting branches.
 *
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date April 02, 2025
 */
public interface BranchService {

    /**
     * Retrieves all branches without pagination.
     *
     * @return A List of all Branch entities in the system.
     */
    List<Branch> getAllBranchesNonPaged();

    /**
     * Retrieves a paginated list of all branches.
     *
     * @param page The page number to retrieve (zero-based).
     * @param size The number of items per page.
     * @return A PaginationWrapper containing the list of Branch entities and pagination metadata.
     */
    PaginationWrapper<List<Branch>> getAllBranches(int page, int size);

    /**
     * Retrieves a specific branch by its ID.
     *
     * @param id The unique identifier of the branch to retrieve.
     * @return The Branch entity with the specified ID, or null if not found.
     */
    Branch getBranchById(Long id);

    /**
     * Creates a new branch in the system.
     *
     * @param branch The Branch entity to be created.
     * @return The created Branch entity, typically with an assigned ID.
     */
    Branch createBranch(Branch branch);

    /**
     * Updates an existing branch identified by its ID.
     *
     * @param id The unique identifier of the branch to update.
     * @param branch The Branch entity containing updated details.
     * @return The updated Branch entity, or null if the branch was not found.
     */
    Branch updateBranch(Long id, Branch branch);

    /**
     * Deletes a branch by its ID.
     *
     * @param id The unique identifier of the branch to delete.
     * @return true if the branch was successfully deleted, false if it was not found.
     */
    boolean deleteBranch(Long id);

    /**
     * Finds branches whose names contain the specified keyword.
     *
     * @param name The keyword to search for within branch names.
     * @return A List of Branch entities matching the keyword (partial match).
     */
    List<Branch> findBranchesByName(String name);

    /**
     * Finds a branch by its exact name.
     *
     * @param brchName The exact name of the branch to search for.
     * @return The Branch entity with the specified name, or null if not found.
     */
    Branch findByBrchName(String brchName);
    /**
     * Retrieves all product types associated with a specific branch.
     *
     * @param branchId The unique identifier of the branch.
     * @return A List of ProductTypeBranch entities for the specified branch.
     */
    List<ProductTypeBranch> getProductTypesByBranchId(Long branchId);

    

}