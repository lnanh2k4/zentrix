package com.zentrix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.zentrix.model.entity.Image;
import com.zentrix.repository.ImageRepository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageServiceImpl implements ImageService {

    final ImageRepository imageRepository;
    final FileService fileService;

    public ImageServiceImpl(ImageRepository imageRepository, FileService fileService) {
        this.imageRepository = imageRepository;
        this.fileService = fileService;
    }

    @Transactional
    @Override
    public Image uploadImage(MultipartFile file) {
        String fileUrl = fileService.saveFile(file);

        Image image = new Image();
        image.setImageLink(fileUrl);

        return imageRepository.save(image);
    }

  
    @Override
    @Transactional
    public void ExampleImage(String imageLink) {
        String mockImageLink = "uploads/" + imageLink;

        Image image = new Image();
        image.setImageLink(mockImageLink);

        imageRepository.save(image);
    }

}
