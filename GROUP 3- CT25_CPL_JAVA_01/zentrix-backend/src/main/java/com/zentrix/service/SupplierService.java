package com.zentrix.service;

import java.util.List;
import com.zentrix.model.entity.Supplier;
import com.zentrix.model.request.SupplierRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/*
 * Service interface for managing Supplier entities.
 * Defines the core operations for supplier management, including retrieval, creation, update, and deletion.
 *
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date February 12, 2025
 */

public interface SupplierService {

    /**
     * Retrieves a supplier by its unique ID.
     *
     * @param id the ID of the supplier
     * @return the supplier if found, otherwise null
     */
    Supplier getSupplierById(int id);

    /**
     * Adds a new supplier to the system.
     *
     * @param supplier the supplier entity to be added
     * @return the created supplier entity
     */
    Supplier addSupplier(Supplier supplier);

    /**
     * Searches for suppliers by name.
     *
     * @param name the name or partial name of the supplier
     * @return a list of suppliers matching the provided name
     */
    List<Supplier> findSuppliersByName(String name);

    /**
     * Updates an existing supplier's details.
     *
     * @param id the ID of the supplier to update
     * @param supplierRequest the new supplier details
     * @return the updated supplier entity, or null if the supplier does not exist
     */
    Supplier updateSupplier(int id, SupplierRequest supplierRequest);

    /**
     * Retrieves all suppliers with pagination support.
     *
     * @param pageable pagination parameters
     * @return a paginated list of suppliers
     */
    Page<Supplier> getAllSuppliers(Pageable pageable);

    /**
     * Deletes a supplier by its ID.
     *
     * @param id the ID of the supplier to delete
     * @return true if the supplier was deleted successfully, false otherwise
     */
    boolean deleteSupplier(int id);
}
