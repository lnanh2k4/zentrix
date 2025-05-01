package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.Attribute;
import com.zentrix.model.request.AttributeRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
* @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
* @date February 17, 2025
*/
public interface AttributeService {

    /**
     * Retrieves all attributes.
     *
     * @return A list of all attributes.
     */
    PaginationWrapper<List<Attribute>> getAll(int page, int size);

    /**
     * Retrieves an attribute by its ID.
     *
     * @param id The ID of the attribute.
     * @return The attribute with the specified ID.
     */
    Attribute getById(Long id);

    /**
     * Creates a new attribute.
     *
     * @param attribute The attribute to be created.
     * @return The created attribute.
     */
    Attribute create(AttributeRequest attribute);

    /**
     * Updates an existing attribute by ID.
     *
     * @param id        The ID of the attribute to update.
     * @param attribute The updated attribute details.
     * @return The updated attribute.
     */
    Attribute update(Long id, Attribute attribute);

    /**
     * Deletes an attribute by ID.
     *
     * @param id The ID of the attribute to delete.
     */
    boolean delete(Long id);
}
