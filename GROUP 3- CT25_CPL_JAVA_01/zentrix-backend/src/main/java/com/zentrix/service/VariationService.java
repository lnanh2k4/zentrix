package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.Variation;
import com.zentrix.model.request.VariationRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
public interface VariationService {

    /**
     * Retrieves all Variation records.
     *
     * @return A list of all Variation entities.
     */
    PaginationWrapper<List<Variation>> getAll(int page, int size);

    /**
     * Retrieves a Variation by its ID.
     *
     * @param id The ID of the Variation.
     * @return The Variation entity or null if not found.
     */
    Variation getById(Long id);

    /**
     * Creates and saves a new Variation entity.
     *
     * @param variation The Variation entity to create.
     * @return The saved Variation entity.
     */
    Variation create(VariationRequest variation);

    /**
     * Updates an existing Variation by its ID.
     * If the entity exists, it updates the record; otherwise, it returns null.
     *
     * @param id        The ID of the Variation to update.
     * @param variation The new Variation details.
     * @return The updated Variation entity or null if the ID does not exist.
     */
    Variation update(Long id, Variation variation);

    /**
     * Deletes a Variation by its ID.
     *
     * @param id The ID of the Variation to delete.
     */
    boolean delete(Long id);
}
