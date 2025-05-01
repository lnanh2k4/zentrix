package com.zentrix.service;

import com.zentrix.model.entity.Image;
import com.zentrix.model.entity.ImageProductType;
import com.zentrix.model.entity.ProductType;
import com.zentrix.repository.ImageProductTypeRepository;
import com.zentrix.repository.ImageRepository;
import com.zentrix.repository.ProductTypeRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageProductTypeServiceImpl implements ImageProductTypeService {
    ImageProductTypeRepository repository;
    ProductTypeRepository productTypeRepository;
    ImageRepository imageRepository;

    @Override
    public List<ImageProductType> getAll() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve image product types", e);
        }
    }

    @Override
    public ImageProductType getById(Long id) {
        try {
            return repository.findById(id).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve image product type by ID: " + id, e);
        }
    }

    @Override
    public ImageProductType create(MultipartFile file, Long productTypeId) {
        try {
            String uploadDir = "uploads/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, file.getBytes());

            Image image = new Image();
            String normalizedPath = filePath.toString().replace("\\", "/");
            image.setImageLink(normalizedPath);
            Image savedImage = imageRepository.save(image);

            ImageProductType imageProductType = new ImageProductType();
            imageProductType.setProdTypeId(productTypeRepository.findById(productTypeId)
                    .orElseThrow(() -> new RuntimeException("ProductType not found")));
            imageProductType.setImageId(savedImage);

            return repository.save(imageProductType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create image product type due to file operation", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create image product type", e);
        }
    }

    @Override
    public ImageProductType update(Long id, MultipartFile file, Long productTypeId) {
        try {
            String uploadDir = "uploads/";
            ImageProductType existingImageProductType = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("ImageProductType not found"));

            if (productTypeId != null) {
                ProductType productType = productTypeRepository.findById(productTypeId)
                        .orElseThrow(() -> new RuntimeException("ProductType not found"));
                existingImageProductType.setProdTypeId(productType);
            }

            if (file != null && !file.isEmpty()) {
                Image existingImage = existingImageProductType.getImageId();
                if (existingImage != null) {
                    String oldFilePath = existingImage.getImageLink();
                    if (oldFilePath != null) {
                        Files.deleteIfExists(Paths.get(oldFilePath));
                    }
                }

                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                Files.write(filePath, file.getBytes());

                Image image = existingImage != null ? existingImage : new Image();
                image.setImageLink(filePath.toString());
                Image savedImage = imageRepository.save(image);
                existingImageProductType.setImageId(savedImage);
            }

            return repository.save(existingImageProductType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update image product type due to file operation", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update image product type", e);
        }
    }

    @Override
    public void deleteImagesByProductType(Long productTypeId) {
        ProductType productType = productTypeRepository.findById(productTypeId)
                .orElseThrow(() -> new RuntimeException("ProductType not found"));
        repository.deleteByProdTypeId(productType);
    }

    @Override
    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image product type with ID: " + id, e);
        }
    }

    @Override
    public List<ImageProductType> getByProdId(Long productTypeId) {
        if (productTypeId == null) {
            throw new IllegalArgumentException("ProductType ID cannot be null");
        }

        ProductType productType = productTypeRepository.findById(productTypeId)
                .orElseThrow(() -> new RuntimeException("ProductType not found"));

        return repository.findByProdTypeId(productType);
    }

    @Override
    public List<ImageProductType> createMultiple(List<MultipartFile> files, Long productTypeId) {
        try {
            String uploadDir = "uploads/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            ProductType productType = productTypeRepository.findById(productTypeId)
                    .orElseThrow(() -> new RuntimeException("ProductType not found"));

            List<ImageProductType> createdImages = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue; // Bỏ qua file rỗng
                }

                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, file.getBytes());

                Image image = new Image();
                String normalizedPath = filePath.toString().replace("\\", "/");
                image.setImageLink(normalizedPath);
                Image savedImage = imageRepository.save(image);

                ImageProductType imageProductType = new ImageProductType();
                imageProductType.setProdTypeId(productType);
                imageProductType.setImageId(savedImage);

                createdImages.add(repository.save(imageProductType));
            }

            return createdImages;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create image product types due to file operation", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create image product types", e);
        }
    }

    @Override
    public void deleteImageById(Long imageProdId) {
        repository.deleteById(imageProdId);
    }

    @Override
    @Transactional
    public void ExampleImageProductType(Long imageId, Long prodTypeid) {

        ImageProductType image1 = new ImageProductType();
        image1.setImageId(imageRepository.findById(imageId).orElse(null));
        image1.setProdTypeId(productTypeRepository.findById(prodTypeid).orElse(null));
        repository.save(image1);
    }
}