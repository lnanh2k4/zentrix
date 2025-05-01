package com.zentrix.service;

import org.springframework.web.multipart.MultipartFile;

import com.zentrix.model.entity.ImageProductType;

import java.util.List;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
public interface ImageProductTypeService {

    /**
     * Retrieves all ImageProductType records from the database.
     *
     * @return A list of all ImageProductType entities.
     */
    List<ImageProductType> getAll();

    /**
     * Retrieves an ImageProductType by its ID.
     *
     * @param id The ID of the ImageProductType.
     * @return The found ImageProductType or null if not found.
     */
    ImageProductType getById(Long id);

    /**
     * Creates and saves a new ImageProductType with an uploaded image.
     *
     * @param file          The image file to upload.
     * @param productTypeId The ID of the associated product type.
     * @return The saved ImageProductType entity.
     */
    ImageProductType create(MultipartFile file, Long productTypeId);

    /**
     * Updates an existing ImageProductType by its ID.
     *
     * @param id               The ID of the ImageProductType to update.
     * @param imageProductType The new ImageProductType details.
     * @return The updated ImageProductType or null if the ID does not exist.
     */
    ImageProductType update(Long id, MultipartFile file, Long productTypeId);

    /**
     * Deletes an ImageProductType by its ID.
     *
     * @param id The ID of the ImageProductType to delete.
     */
    void delete(Long id);

    List<ImageProductType> getByProdId(Long id);

    void deleteImageById(Long imageProdId);

    void deleteImagesByProductType(Long productTypeId);

    List<ImageProductType> createMultiple(List<MultipartFile> files, Long productTypeId);

    public void ExampleImageProductType(Long imageId, Long prodTypeid);
}
