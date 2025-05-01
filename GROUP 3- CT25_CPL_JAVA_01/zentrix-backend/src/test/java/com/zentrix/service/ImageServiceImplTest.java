package com.zentrix.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import com.zentrix.model.entity.Image;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */

public class ImageServiceImplTest {

    // // Mock dependencies
    // private final ImageService imageService = mock(ImageService.class);
    // private final MultipartFile file = mock(MultipartFile.class);

    // /**
    // * Tests the uploadImage() method in ImageService.
    // * It ensures that the file is uploaded correctly and returns an Image object.
    // */
    // @Test
    // public void testUploadImage() {
    // // Mock file behavior
    // when(file.getOriginalFilename()).thenReturn("test-image.jpg");

    // // Mock service behavior
    // when(imageService.uploadImage(file)).thenReturn(new Image(1L,
    // "test-image.jpg", null));

    // // Execute the method under test
    // Image image = imageService.uploadImage(file);

    // // Assertions: Verify that the returned image is correct
    // assertThat(image.getImageId()).isNotNull(); // Image ID should not be null
    // assertThat(image.getImageLink()).isEqualTo("test-image.jpg"); // File link
    // should match expected value
    // }
}