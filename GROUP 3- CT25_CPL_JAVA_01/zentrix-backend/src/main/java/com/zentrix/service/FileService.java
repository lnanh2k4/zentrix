package com.zentrix.service;

import org.springframework.web.multipart.MultipartFile;

/*
* @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
* @date February 20, 2025
*/
public interface FileService {
    /**
     * Saves the uploaded file and returns its URL.
     *
     * @param file The file to be uploaded.
     * @return The URL of the saved file.
     */

    String saveFile(MultipartFile file);

    /**
     * Deletes a file from the server based on its file path.
     *
     * @param filePath The path of the file to be deleted.
     */
    void deleteFile(String filePath);

     /**
     * Updates the image URL for a specific entity (e.g., product) during database initialization.
     *
     * @param id  The ID of the entity (e.g., product).
     * @param url The image URL to be updated in the database.
     */
    void updateUrlImageForDatabaseInit(long id, String url);
    
     /**
     * Updates the image URL for a specific review during database initialization.
     *
     * @param id  The ID of the review.
     * @param url The image URL to be updated for the review in the database.
     */
    void updateUrlImageReviewForDatabaseInit(long id, String url);
}
