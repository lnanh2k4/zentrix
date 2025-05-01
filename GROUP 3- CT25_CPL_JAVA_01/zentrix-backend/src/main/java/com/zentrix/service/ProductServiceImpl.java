package com.zentrix.service;

import com.zentrix.model.entity.Category;
import com.zentrix.model.entity.Product;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.Supplier;
import com.zentrix.model.request.ProductRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 11, 2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    CategoryService categoryService;
    SupplierService supplierService;

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public Product createProduct(ProductRequest req, Integer categoryId, Integer supplierId) {
        if (req == null || categoryId == null || supplierId == null) {
            return null;
        }
        Category category = categoryService.getCategoryById(categoryId);
        Supplier supplier = supplierService.getSupplierById(supplierId);

        if (category == null || supplier == null) {
            return null;
        }

        Product product = new Product();
        product.setProdName(req.getProdName());
        product.setDescription(req.getDescription());
        product.setVat(req.getVat());
        product.setCateId(category);
        product.setSuppId(supplier);
        product.setStatus(1);

        return productRepository.save(product);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return null;
        }

        if (request.getCateId() != null) {
            Category category = categoryService.getCategoryById(request.getCateId());
            product.setCateId(category);
        }
        if (request.getSuppId() != null) {
            Supplier supplier = supplierService.getSupplierById(request.getSuppId());
            product.setSuppId(supplier);

        }

        product.setProdName(request.getProdName());
        product.setDescription(request.getDescription());
        product.setVat(request.getVat());

        return productRepository.save(product);
    }

    @Override
    public Product findProductById(Long proId) {
        return productRepository.findById(proId).orElse(null);
    }

    @Override
    public Product findProductByProdTypeId(Long proTypeId) {
        return productRepository.findByProductTypeId(proTypeId);
    }

    @Override
    public PaginationWrapper<List<Product>> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("prodId").descending());

        Page<Product> productPage = productRepository.findAll(pageable);

        return new PaginationWrapper.Builder<List<Product>>()
                .setData(productPage.getContent())
                .setPaginationInfo(productPage)
                .build();
    }

    @Override
    public PaginationWrapper<List<Product>> getProductsByStatus(List<Integer> statusList, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("prodId").descending());

        Page<Product> productPage = productRepository.findByStatusIn(statusList, pageable);

        return new PaginationWrapper.Builder<List<Product>>()
                .setData(productPage.getContent())
                .setPaginationInfo(productPage)
                .build();
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean deleteProduct(Long proId) {
        if (productRepository.existsById(proId)) {
            productRepository.deleteById(proId);
            return true;
        }
        return false;
    }

    @Override
    public PaginationWrapper<List<Product>> searchProductByName(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByProdNameContainingIgnoreCase(keyword, pageable);
        return new PaginationWrapper.Builder<List<Product>>()
                .setData(productPage.getContent())
                .setPaginationInfo(productPage)
                .build();
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean unactiveProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return false;
        }

        if (product.getStatus() == 1) {
            product.setStatus(0);
        } else {
            product.setStatus(1);

        }
        productRepository.save(product);
        return true;
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean softdelete(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return false;
        }

        if (product.getStatus() != 3) {
            product.setStatus(3);
        } else {
            product.setStatus(1);

        }
        productRepository.save(product);
        return true;
    }
}