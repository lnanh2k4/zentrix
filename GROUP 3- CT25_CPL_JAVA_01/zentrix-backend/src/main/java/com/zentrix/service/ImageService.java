package com.zentrix.service;

import org.springframework.web.multipart.MultipartFile;

import com.zentrix.model.entity.Image;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */
public interface ImageService {
    /**
     * Uploads an image and saves it to the server.
     *
     * @param file The image file to be uploaded.
     * @return The saved Image entity.
     */
    Image uploadImage(MultipartFile file);

    /**
     * Sets the example image link for display or storage purposes.
     *
     * @param imageLink the URL of the image to be used as an example
     */
    public void ExampleImage(String imageLink);

}
