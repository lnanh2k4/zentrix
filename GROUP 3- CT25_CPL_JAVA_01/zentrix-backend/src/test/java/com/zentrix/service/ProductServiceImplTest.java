package com.zentrix.service;

import com.zentrix.model.entity.Category;
import com.zentrix.model.entity.Product;
import com.zentrix.model.entity.Supplier;
import com.zentrix.model.request.ProductRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private SupplierService supplierService;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        reset(productRepository, categoryService, supplierService);
    }

    // Test for createProduct
    @Test
    void createProduct_Success() {
        ProductRequest request = new ProductRequest();
        request.setProdName("Laptop");
        request.setDescription("Gaming Laptop");
        request.setVat(10.0F);
        Integer categoryId = 1;
        Integer supplierId = 2;
        Category category = new Category();
        Supplier supplier = new Supplier();
        Product product = new Product();
        product.setProdName("Laptop");
        product.setDescription("Gaming Laptop");
        product.setVat(10.0F);
        product.setCateId(category);
        product.setSuppId(supplier);
        product.setStatus(1);

        when(categoryService.getCategoryById(categoryId)).thenReturn(category);
        when(supplierService.getSupplierById(supplierId)).thenReturn(supplier);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.createProduct(request, categoryId, supplierId);

        assertNotNull(result);
        assertEquals("Laptop", result.getProdName());
        assertEquals("Gaming Laptop", result.getDescription());
        assertEquals(10.0F, result.getVat());
        assertEquals(category, result.getCateId());
        assertEquals(supplier, result.getSuppId());
        assertEquals(1, result.getStatus());
        verify(categoryService).getCategoryById(categoryId);
        verify(supplierService).getSupplierById(supplierId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_NullInput_ReturnsNull() {
        Product result = productService.createProduct(null, 1, 1);
        assertNull(result);

        result = productService.createProduct(new ProductRequest(), null, 1);
        assertNull(result);

        result = productService.createProduct(new ProductRequest(), 1, null);
        assertNull(result);

        verify(categoryService, never()).getCategoryById(anyInt());
        verify(supplierService, never()).getSupplierById(anyInt());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_CategoryOrSupplierNotFound_ReturnsNull() {
        ProductRequest request = new ProductRequest();
        Integer categoryId = 1;
        Integer supplierId = 2;

        when(categoryService.getCategoryById(categoryId)).thenReturn(null);
        when(supplierService.getSupplierById(supplierId)).thenReturn(new Supplier());

        Product result = productService.createProduct(request, categoryId, supplierId);
        assertNull(result);

        when(categoryService.getCategoryById(categoryId)).thenReturn(new Category());
        when(supplierService.getSupplierById(supplierId)).thenReturn(null);

        result = productService.createProduct(request, categoryId, supplierId);
        assertNull(result);

        verify(categoryService, times(2)).getCategoryById(categoryId);
        verify(supplierService, times(2)).getSupplierById(supplierId);
        verify(productRepository, never()).save(any(Product.class));
    }

    // Test for updateProduct
    @Test
    void updateProduct_Success() {
        Long id = 1L;
        ProductRequest request = new ProductRequest();
        request.setProdName("Updated Laptop");
        request.setDescription("Updated Description");
        request.setVat(15.0F);
        request.setCateId(2);
        request.setSuppId(3);
        Product existingProduct = new Product();
        existingProduct.setStatus(1);
        Category category = new Category();
        Supplier supplier = new Supplier();
        Product updatedProduct = new Product();
        updatedProduct.setProdName("Updated Laptop");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setVat(15.0F);
        updatedProduct.setCateId(category);
        updatedProduct.setSuppId(supplier);
        updatedProduct.setStatus(1);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(categoryService.getCategoryById(2)).thenReturn(category);
        when(supplierService.getSupplierById(3)).thenReturn(supplier);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.updateProduct(id, request);

        assertNotNull(result);
        assertEquals("Updated Laptop", result.getProdName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(15.0F, result.getVat());
        assertEquals(category, result.getCateId());
        assertEquals(supplier, result.getSuppId());
        verify(productRepository).findById(id);
        verify(categoryService).getCategoryById(2);
        verify(supplierService).getSupplierById(3);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_NotFound_ReturnsNull() {
        Long id = 1L;
        ProductRequest request = new ProductRequest();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        Product result = productService.updateProduct(id, request);

        assertNull(result);
        verify(productRepository).findById(id);
        verify(categoryService, never()).getCategoryById(anyInt());
        verify(supplierService, never()).getSupplierById(anyInt());
        verify(productRepository, never()).save(any(Product.class));
    }

    // Test for findProductById
    @Test
    void findProductById_Success() {
        Long id = 1L;
        Product product = new Product();

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Product result = productService.findProductById(id);

        assertNotNull(result);
        assertEquals(product, result);
        verify(productRepository).findById(id);
    }

    @Test
    void findProductById_NotFound() {
        Long id = 1L;

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        Product result = productService.findProductById(id);

        assertNull(result);
        verify(productRepository).findById(id);
    }

    // Test for getAllProducts
    @Test
    void getAllProducts_Success() {
        int page = 0;
        int size = 10;
        List<Product> products = Arrays.asList(new Product(), new Product());
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(page, size, Sort.by("prodId").descending()),
                products.size());

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        PaginationWrapper<List<Product>> result = productService.getAllProducts(page, size);

        assertNotNull(result);
        assertEquals(products, result.getData());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(products.size(), result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(productRepository).findAll(PageRequest.of(page, size, Sort.by("prodId").descending()));
    }

    // Test for getProductsByStatus
    @Test
    void getProductsByStatus_Success() {
        int page = 0;
        int size = 10;
        List<Integer> statusList = Arrays.asList(1, 2);
        List<Product> products = Arrays.asList(new Product(), new Product());
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(page, size, Sort.by("prodId").descending()),
                products.size());

        when(productRepository.findByStatusIn(eq(statusList), any(Pageable.class))).thenReturn(productPage);

        PaginationWrapper<List<Product>> result = productService.getProductsByStatus(statusList, page, size);

        assertNotNull(result);
        assertEquals(products, result.getData());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(products.size(), result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(productRepository).findByStatusIn(statusList,
                PageRequest.of(page, size, Sort.by("prodId").descending()));
    }

    // Test for deleteProduct
    @Test
    void deleteProduct_Success() {
        Long id = 1L;

        when(productRepository.existsById(id)).thenReturn(true);
        doNothing().when(productRepository).deleteById(id);

        boolean result = productService.deleteProduct(id);

        assertTrue(result);
        verify(productRepository).existsById(id);
        verify(productRepository).deleteById(id);
    }

    @Test
    void deleteProduct_NotFound() {
        Long id = 1L;

        when(productRepository.existsById(id)).thenReturn(false);

        boolean result = productService.deleteProduct(id);

        assertFalse(result);
        verify(productRepository).existsById(id);
        verify(productRepository, never()).deleteById(id);
    }

    // Test for searchProductByName
    @Test
    void searchProductByName_Success() {
        String keyword = "Laptop";
        int page = 0;
        int size = 10;
        List<Product> products = Arrays.asList(new Product(), new Product());
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(page, size), products.size());

        when(productRepository.findByProdNameContainingIgnoreCase(eq(keyword), any(Pageable.class)))
                .thenReturn(productPage);

        PaginationWrapper<List<Product>> result = productService.searchProductByName(keyword, page, size);

        assertNotNull(result);
        assertEquals(products, result.getData());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(products.size(), result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(productRepository).findByProdNameContainingIgnoreCase(keyword, PageRequest.of(page, size));
    }

    // Test for unactiveProduct
    @Test
    void unactiveProduct_Success_ActiveToInactive() {
        Long id = 1L;
        Product product = new Product();
        product.setStatus(1); // Active
        Product updatedProduct = new Product();
        updatedProduct.setStatus(0); // Inactive

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        boolean result = productService.unactiveProduct(id);

        assertTrue(result);
        assertEquals(0, updatedProduct.getStatus());
        verify(productRepository).findById(id);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void unactiveProduct_Success_InactiveToActive() {
        Long id = 1L;
        Product product = new Product();
        product.setStatus(0); // Inactive
        Product updatedProduct = new Product();
        updatedProduct.setStatus(1); // Active

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        boolean result = productService.unactiveProduct(id);

        assertTrue(result);
        assertEquals(1, updatedProduct.getStatus());
        verify(productRepository).findById(id);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void unactiveProduct_NotFound() {
        Long id = 1L;

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        boolean result = productService.unactiveProduct(id);

        assertFalse(result);
        verify(productRepository).findById(id);
        verify(productRepository, never()).save(any(Product.class));
    }

    // Test for softdelete
    @Test
    void softdelete_Success_NotDeletedToDeleted() {
        Long id = 1L;
        Product product = new Product();
        product.setStatus(1); // Not deleted
        Product updatedProduct = new Product();
        updatedProduct.setStatus(3); // Soft-deleted

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        boolean result = productService.softdelete(id);

        assertTrue(result);
        assertEquals(3, updatedProduct.getStatus());
        verify(productRepository).findById(id);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void softdelete_Success_DeletedToNotDeleted() {
        Long id = 1L;
        Product product = new Product();
        product.setStatus(3); // Soft-deleted
        Product updatedProduct = new Product();
        updatedProduct.setStatus(1); // Not deleted

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        boolean result = productService.softdelete(id);

        assertTrue(result);
        assertEquals(1, updatedProduct.getStatus());
        verify(productRepository).findById(id);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void softdelete_NotFound() {
        Long id = 1L;

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        boolean result = productService.softdelete(id);

        assertFalse(result);
        verify(productRepository).findById(id);
        verify(productRepository, never()).save(any(Product.class));
    }
}