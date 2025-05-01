package com.zentrix.service;

import com.zentrix.model.entity.Product;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.request.ProductTypeRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.ProductTypeRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;

/*
* @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
* @date February 14, 2025
*/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductTypeServiceImpl implements ProductTypeService {
    ProductTypeRepository productTypeRepository;
    ProductService productService;

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public ProductType saveProductType(ProductTypeRequest request) {
        Product product = productService.findProductById(request.getProdId());

        if (product == null) {
            throw new IllegalArgumentException("Product not found for ID: " + request.getProdId());
        }
        try {
            ProductType createProductType = new ProductType();
            createProductType.setProdId(product);
            createProductType.setProdTypeName(request.getProdTypeName());
            createProductType.setProdTypePrice(request.getProdTypePrice());
            createProductType.setUnit(request.getUnit());
            createProductType.setUnitPrice(request.getUnitPrice());
            createProductType.setStatus(1);
            return productTypeRepository.save(createProductType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ProductType: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public ProductType updateProductType(Long id, ProductTypeRequest request) {
        ProductType productType = this.findProductTypeById(id);
        if (productType == null) {
            throw new IllegalArgumentException("ProductType not found for ID: " + id);
        }
        Product product = productService.findProductById(request.getProdId());
        if (product == null) {
            throw new IllegalArgumentException("Product not found for ID: " + request.getProdId());
        }
        productType.setProdId(product);
        productType.setProdTypeName(request.getProdTypeName());
        productType.setProdTypePrice(request.getProdTypePrice());
        productType.setUnit(request.getUnit());
        productType.setUnitPrice(request.getUnitPrice());
        try {
            return productTypeRepository.save(productType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update ProductType: " + e.getMessage(), e);
        }
    }

    @Override
    public ProductType findProductTypeById(Long id) {

        return productTypeRepository.findById(id).orElse(null);
    }

    @Override
    public PaginationWrapper<List<ProductType>> getAllProductTypes(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<ProductType> productTypePage = productTypeRepository.findAll(pageable);

            return new PaginationWrapper.Builder<List<ProductType>>()
                    .setData(productTypePage.getContent())
                    .setPaginationInfo(productTypePage)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve ProductTypes: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean deleteProductType(Long id) {
        try {
            productTypeRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete ProductType: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductType> findProductTypesByProductId(Long prodId) {
        try {
            Product product = productService.findProductById(prodId);
            if (product == null) {
                throw new IllegalArgumentException("Product not found for ID: " + prodId);
            }
            return productTypeRepository.findByProdId(product);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to retrieve ProductTypes for Product ID " + prodId + ": " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean unactiveProductType(Long id, Integer status) {
        ProductType productType = productTypeRepository.findById(id).orElse(null);
        if (productType == null) {
            return false;
        }
        if (status == 3) {
            productType.setStatus(3);

        } else {
            if (productType.getStatus() == 1) {
                productType.setStatus(0);

            } else {
                productType.setStatus(1);

            }
        }

        productTypeRepository.save(productType);
        return true;
    }

    @Override
    public PaginationWrapper<List<ProductType>> searchProductTypesByName(String keyword, int page, int size) {
        try {
            // Kiểm tra keyword có null hoặc rỗng không
            if (keyword == null || keyword.trim().isEmpty()) {
                throw new IllegalArgumentException("Search keyword cannot be empty");
            }

            // Tạo đối tượng Pageable để phân trang
            Pageable pageable = PageRequest.of(page, size);

            // Gọi repository để tìm kiếm theo prodTypeName (dùng LIKE để tìm gần đúng)
            Page<ProductType> productTypePage = productTypeRepository.findByProdTypeNameContainingIgnoreCase(
                    keyword.trim(), pageable);

            // Trả về kết quả trong PaginationWrapper
            return new PaginationWrapper.Builder<List<ProductType>>()
                    .setData(productTypePage.getContent())
                    .setPaginationInfo(productTypePage)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to search ProductTypes by name: " + e.getMessage(), e);
        }
    }
}
