package com.zentrix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.zentrix.model.entity.Supplier;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository interface for managing Supplier entities in the database.
 * Extends JpaRepository to provide basic CRUD operations and custom query methods.
 *
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date February 12, 2025
 */
@Repository // Marks this interface as a Spring Data repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    /**
     * Retrieves a list of suppliers whose names contain the specified keyword, ignoring case.
     *
     * @param suppName The keyword to search for within supplier names.
     * @return A List of Supplier entities matching the keyword (partial match, case-insensitive).
     */
    List<Supplier> findBySuppNameContainingIgnoreCase(String suppName);

    /**
     * Checks if a supplier with the specified email exists in the database.
     *
     * @param email The email address to check for existence.
     * @return true if a supplier with the given email exists, false otherwise.
     */
    boolean existsByEmail(String email);

    /**
     * Retrieves a paginated list of all suppliers.
     *
     * @param pageable Pagination and sorting information (e.g., page number, size, sort order).
     * @return A Page object containing a list of Supplier entities for the specified page.
     */
    Page<Supplier> findAll(Pageable pageable);

    /**
     * Finds a supplier by its email address.
     *
     * @param email The email address of the supplier to search for.
     * @return The Supplier entity with the given email, or null if not found.
     */
    Supplier findByEmail(String email);
}