package com.zentrix.controller;

import com.zentrix.model.entity.*;
import com.zentrix.model.request.*;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.repository.ProductTypeAttributeRepository;
import com.zentrix.repository.ProductTypeVariationRepository;
import com.zentrix.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;
    @Mock
    private ProductTypeService productTypeService;
    @Mock
    private ProductTypeAttributeService productTypeAttributeService;
    @Mock
    private ProductTypeBranchService productTypeBranchService;
    @Mock
    private ProductTypeVariationService productTypeVariationService;
    @Mock
    private AttributeService attributeService;
    @Mock
    private VariationService variationService;
    @Mock
    private ImageProductTypeService imageProductTypeService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        // Any common setup can go here
    }

    // Test for POST endpoint - createImage
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createImage_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "test".getBytes());
        Long productTypeId = 1L;
        ImageProductType imageProductType = new ImageProductType();

        when(imageProductTypeService.create(any(), anyLong())).thenReturn(imageProductType);

        ResponseEntity<ImageProductType> response = productController.createImage(file, productTypeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(imageProductType, response.getBody());
        verify(imageProductTypeService).create(file, productTypeId);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createImage_EmptyFile_ReturnsBadRequest() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "",
                MediaType.IMAGE_JPEG_VALUE, new byte[0]);
        Long productTypeId = 1L;

        ResponseEntity<ImageProductType> response = productController.createImage(emptyFile, productTypeId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(imageProductTypeService, never()).create(any(), anyLong());
    }

    // Test for GET endpoint - getProductTypesByProductId
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getProductTypesByProductId_Success() {
        Long prodId = 1L;
        List<ProductType> productTypes = Arrays.asList(new ProductType());

        when(productTypeService.findProductTypesByProductId(prodId)).thenReturn(productTypes);

        ResponseEntity<List<ProductType>> response = productController.getProductTypesByProductId(prodId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productTypes, response.getBody());
        verify(productTypeService).findProductTypesByProductId(prodId);
    }

    // Test for GET endpoint with pagination - getAllProducts
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getAllProducts_Success() {
        List<Product> products = Arrays.asList(new Product());

        // Use the Builder to create PaginationWrapper
        PaginationWrapper<List<Product>> wrapper = new PaginationWrapper.Builder<List<Product>>()
                .setData(products)
                .setPage(0)
                .setSize(100)
                .setTotalPages(1)
                .setTotalElements(products.size())
                .build();

        when(productService.getAllProducts(0, 100)).thenReturn(wrapper);

        ResponseEntity<ResponseObject<List<Product>>> response = productController.getAllProducts(0, 100, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(products, response.getBody().getContent());
        verify(productService).getAllProducts(0, 100);
    }

    // Test for PUT endpoint - updateProduct
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateProduct_Success() {
        Long id = 1L;
        ProductRequest request = new ProductRequest();
        Product existingProduct = new Product();
        Product updatedProduct = new Product();

        when(productService.findProductById(id)).thenReturn(existingProduct);
        when(productService.updateProduct(id, request)).thenReturn(updatedProduct);

        ResponseEntity<Product> response = productController.updateProduct(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedProduct, response.getBody());
        verify(productService).updateProduct(id, request);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateProduct_NotFound() {
        Long id = 1L;
        ProductRequest request = new ProductRequest();

        when(productService.findProductById(id)).thenReturn(null);

        ResponseEntity<Product> response = productController.updateProduct(id, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productService, never()).updateProduct(anyLong(), any());
    }

    // Test for DELETE endpoint - deleteProduct
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteProduct_Success() {
        Long id = 1L;

        when(productService.deleteProduct(id)).thenReturn(true);

        ResponseEntity<Void> response = productController.deleteProduct(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService).deleteProduct(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteProduct_NotFound() {
        Long id = 1L;

        when(productService.deleteProduct(id)).thenReturn(false);

        ResponseEntity<Void> response = productController.deleteProduct(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productService).deleteProduct(id);
    }

    // Test for exception handling - createImages
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createImages_Exception_ReturnsInternalServerError() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "test".getBytes());
        Long productTypeId = 1L;

        when(imageProductTypeService.createMultiple(anyList(), anyLong()))
                .thenThrow(new RuntimeException("Test exception"));

        ResponseEntity<List<ImageProductType>> response = productController
                .createImages(Collections.singletonList(file), productTypeId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(imageProductTypeService).createMultiple(anyList(), eq(productTypeId));
    }

    // Test for search endpoint
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void searchProduct_Success() {
        String keyword = "test";
        List<Product> products = Arrays.asList(new Product());

        // Use the Builder to create PaginationWrapper
        PaginationWrapper<List<Product>> wrapper = new PaginationWrapper.Builder<List<Product>>()
                .setData(products)
                .setPage(0)
                .setSize(10)
                .setTotalPages(1)
                .setTotalElements(products.size())
                .build();

        when(productService.searchProductByName(keyword, 0, 10)).thenReturn(wrapper);

        ResponseEntity<ResponseObject<List<Product>>> response = productController.searchProduct(keyword, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(products, response.getBody().getContent());
        verify(productService).searchProductByName(keyword, 0, 10);
    }

    // Test for deleteImages
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteImages_Success() {
        Long productTypeId = 1L;

        doNothing().when(imageProductTypeService).deleteImagesByProductType(productTypeId);

        ResponseEntity<Void> response = productController.deleteImages(productTypeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(imageProductTypeService).deleteImagesByProductType(productTypeId);
    }

    // Test for deleteImage
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteImage_Success() {
        Long imageProdId = 1L;

        doNothing().when(imageProductTypeService).deleteImageById(imageProdId);

        ResponseEntity<Void> response = productController.deleteImage(imageProdId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(imageProductTypeService).deleteImageById(imageProdId);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteImage_Exception_ReturnsInternalServerError() {
        Long imageProdId = 1L;

        doThrow(new RuntimeException("Delete error")).when(imageProductTypeService).deleteImageById(imageProdId);

        ResponseEntity<Void> response = productController.deleteImage(imageProdId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(imageProductTypeService).deleteImageById(imageProdId);
    }

    // Test for getProductTypeVariationbyProId
    @Mock
    ProductTypeVariationRepository productTypeVariationRepository;

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getProductTypeVariationbyProId_Success() {
        Long prodTypeId = 1L;
        ProductType productType = new ProductType();
        List<ProductTypeVariation> variations = Arrays.asList(new ProductTypeVariation());

        // Mock the service and repository calls
        when(productTypeService.findProductTypeById(prodTypeId)).thenReturn(productType);
        when(productTypeVariationRepository.findByProdTypeId(productType)).thenReturn(variations);

        // Call the controller method
        ResponseEntity<List<ProductTypeVariation>> response = productController
                .getProductTypeVariationbyProId(prodTypeId);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(variations, response.getBody());
        verify(productTypeService).findProductTypeById(prodTypeId);
        verify(productTypeVariationRepository).findByProdTypeId(productType);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getProductTypeBranchByProdTypeId_Exception_ReturnsInternalServerError() {
        Long prodTypeId = 1L;

        when(productTypeService.findProductTypeById(prodTypeId)).thenThrow(new RuntimeException("Fetch error"));

        ResponseEntity<List<ProductTypeBranch>> response = productController
                .getProductTypeBranchByProdTypeId(prodTypeId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(productTypeService).findProductTypeById(prodTypeId);
    }

    @Mock
    ProductTypeAttributeRepository productTypeAttributeRepository;

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getProductTypeAttributebyProId_Success() {
        Long prodTypeId = 1L;
        ProductType productType = new ProductType();
        List<ProductTypeAttribute> attributes = Arrays.asList(new ProductTypeAttribute());

        when(productTypeService.findProductTypeById(prodTypeId)).thenReturn(productType);
        when(productTypeAttributeRepository.findByProdTypeId(productType)).thenReturn(attributes);

        ResponseEntity<List<ProductTypeAttribute>> response = productController
                .getProductTypeAttributebyProId(prodTypeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(attributes, response.getBody());
        verify(productTypeService).findProductTypeById(prodTypeId);
        verify(productTypeAttributeRepository).findByProdTypeId(productType);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getProductTypeAttributebyProId_Exception_ReturnsInternalServerError() {
        Long prodTypeId = 1L;

        when(productTypeService.findProductTypeById(prodTypeId)).thenThrow(new RuntimeException("Fetch error"));

        ResponseEntity<List<ProductTypeAttribute>> response = productController
                .getProductTypeAttributebyProId(prodTypeId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(productTypeService).findProductTypeById(prodTypeId);
    }

    // Test for checkAttributeUsage
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void checkAttributeUsage_Success() {
        Long atbId = 1L;

        when(productTypeAttributeService.isAttributeUsed(atbId)).thenReturn(true);

        ResponseEntity<Boolean> response = productController.checkAttributeUsage(atbId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(productTypeAttributeService).isAttributeUsed(atbId);
    }

    // Test for checkVariationUsage
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void checkVariationUsage_Success() {
        Long variId = 1L;

        when(productTypeVariationService.isVariationUsed(variId)).thenReturn(false);

        ResponseEntity<Boolean> response = productController.checkVariationUsage(variId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
        verify(productTypeVariationService).isVariationUsed(variId);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getImagesByProductType_Success() {
        Long prodTypeId = 1L;
        List<ImageProductType> images = Arrays.asList(new ImageProductType());

        when(imageProductTypeService.getByProdId(prodTypeId)).thenReturn(images);

        ResponseEntity<List<ImageProductType>> response = productController.getImagesByProductType(prodTypeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(images, response.getBody());
        verify(imageProductTypeService).getByProdId(prodTypeId);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getImagesByProductType_Exception_ReturnsInternalServerError() {
        Long prodTypeId = 1L;

        when(imageProductTypeService.getByProdId(prodTypeId)).thenThrow(new RuntimeException("Fetch error"));

        ResponseEntity<List<ImageProductType>> response = productController.getImagesByProductType(prodTypeId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(imageProductTypeService).getByProdId(prodTypeId);
    }

    // Test for getProductById
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getProductById_Success() {
        Long id = 1L;
        Product product = new Product();

        when(productService.findProductById(id)).thenReturn(product);

        ResponseEntity<Product> response = productController.getProductById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(product, response.getBody());
        verify(productService).findProductById(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getProductById_NotFound() {
        Long id = 1L;

        when(productService.findProductById(id)).thenReturn(null);

        ResponseEntity<Product> response = productController.getProductById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productService).findProductById(id);
    }

    // Test for findProductTypeBranchId
    @Test
    @WithMockUser
    void findProductTypeBranchId_Success() {
        Long prodTypeId = 1L;
        Long brchId = 2L;
        ProductTypeBranch productTypeBranch = new ProductTypeBranch();
        productTypeBranch.setProdTypeBrchId(3L);

        when(productTypeBranchService.findByProdTypeIdAndBrchId(prodTypeId, brchId)).thenReturn(productTypeBranch);

        ResponseEntity<Long> response = productController.findProductTypeBranchId(prodTypeId, brchId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3L, response.getBody());
        verify(productTypeBranchService).findByProdTypeIdAndBrchId(prodTypeId, brchId);
    }

    @Test
    @WithMockUser
    void findProductTypeBranchId_NotFound() {
        Long prodTypeId = 1L;
        Long brchId = 2L;

        when(productTypeBranchService.findByProdTypeIdAndBrchId(prodTypeId, brchId)).thenReturn(null);

        ResponseEntity<Long> response = productController.findProductTypeBranchId(prodTypeId, brchId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(productTypeBranchService).findByProdTypeIdAndBrchId(prodTypeId, brchId);
    }

    @Test
    @WithMockUser
    void findProductTypeBranchId_Exception_ReturnsInternalServerError() {
        Long prodTypeId = 1L;
        Long brchId = 2L;

        when(productTypeBranchService.findByProdTypeIdAndBrchId(prodTypeId, brchId))
                .thenThrow(new RuntimeException("Fetch error"));

        ResponseEntity<Long> response = productController.findProductTypeBranchId(prodTypeId, brchId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(productTypeBranchService).findByProdTypeIdAndBrchId(prodTypeId, brchId);
    }

    // Test for getAllProductTypeAttribute
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getAllProductTypeAttribute_Success() {
        List<ProductTypeAttribute> attributes = Arrays.asList(new ProductTypeAttribute());
        PaginationWrapper<List<ProductTypeAttribute>> wrapper = new PaginationWrapper.Builder<List<ProductTypeAttribute>>()
                .setData(attributes)
                .setPage(0)
                .setSize(10000)
                .setTotalPages(1)
                .setTotalElements(attributes.size())
                .build();

        when(productTypeAttributeService.getAll(0, 10000)).thenReturn(wrapper);

        ResponseEntity<ResponseObject<List<ProductTypeAttribute>>> response = productController
                .getAllProductTypeAttribute(0, 10000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(attributes, response.getBody().getContent());
        verify(productTypeAttributeService).getAll(0, 10000);
    }

    // Test for getAllProductTypeBranch
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getAllProductTypeBranch_Success() {
        List<ProductTypeBranch> branches = Arrays.asList(new ProductTypeBranch());
        PaginationWrapper<List<ProductTypeBranch>> wrapper = new PaginationWrapper.Builder<List<ProductTypeBranch>>()
                .setData(branches)
                .setPage(0)
                .setSize(10000)
                .setTotalPages(1)
                .setTotalElements(branches.size())
                .build();

        when(productTypeBranchService.getAllProductTypeBranches(0, 10000)).thenReturn(wrapper);

        ResponseEntity<ResponseObject<List<ProductTypeBranch>>> response = productController.getAllProductTypeBranch(0,
                10000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(branches, response.getBody().getContent());
        verify(productTypeBranchService).getAllProductTypeBranches(0, 10000);
    }

    // Test for getAllProductTypeVariations
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getAllProductTypeVariations_Success() {
        List<ProductTypeVariation> variations = Arrays.asList(new ProductTypeVariation());
        PaginationWrapper<List<ProductTypeVariation>> wrapper = new PaginationWrapper.Builder<List<ProductTypeVariation>>()
                .setData(variations)
                .setPage(0)
                .setSize(10000)
                .setTotalPages(1)
                .setTotalElements(variations.size())
                .build();

        when(productTypeVariationService.getAll(0, 10000)).thenReturn(wrapper);

        ResponseEntity<ResponseObject<List<ProductTypeVariation>>> response = productController
                .getAllProductTypeVariations(0, 10000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(variations, response.getBody().getContent());
        verify(productTypeVariationService).getAll(0, 10000);
    }

    // Test for getAllProductTypes
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getAllProductTypes_Success() {
        List<ProductType> productTypes = Arrays.asList(new ProductType());
        PaginationWrapper<List<ProductType>> wrapper = new PaginationWrapper.Builder<List<ProductType>>()
                .setData(productTypes)
                .setPage(0)
                .setSize(10000)
                .setTotalPages(1)
                .setTotalElements(productTypes.size())
                .build();

        when(productTypeService.getAllProductTypes(0, 10000)).thenReturn(wrapper);

        ResponseEntity<ResponseObject<List<ProductType>>> response = productController.getAllProductTypes(0, 10000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(productTypes, response.getBody().getContent());
        verify(productTypeService).getAllProductTypes(0, 10000);
    }

    // Test for getAllProductTypesForCustomer
    @Test
    @WithMockUser(roles = "GUEST")
    void getAllProductTypesForCustomer_Success() {
        List<ProductType> productTypes = Arrays.asList(new ProductType());
        PaginationWrapper<List<ProductType>> wrapper = new PaginationWrapper.Builder<List<ProductType>>()
                .setData(productTypes)
                .setPage(0)
                .setSize(10000)
                .setTotalPages(1)
                .setTotalElements(productTypes.size())
                .build();

        when(productTypeService.getAllProductTypes(0, 10000)).thenReturn(wrapper);

        ResponseEntity<ResponseObject<List<ProductType>>> response = productController.getAllProductTypesForCustomer(0,
                10000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(productTypes, response.getBody().getContent());
        verify(productTypeService).getAllProductTypes(0, 10000);
    }

    // Test for searchProductTypes
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void searchProductTypes_Success() {
        String keyword = "test";
        List<ProductType> productTypes = Arrays.asList(new ProductType());
        PaginationWrapper<List<ProductType>> wrapper = new PaginationWrapper.Builder<List<ProductType>>()
                .setData(productTypes)
                .setPage(0)
                .setSize(10)
                .setTotalPages(1)
                .setTotalElements(productTypes.size())
                .build();

        when(productTypeService.searchProductTypesByName(keyword, 0, 10)).thenReturn(wrapper);

        ResponseEntity<PaginationWrapper<List<ProductType>>> response = productController.searchProductTypes(keyword, 0,
                10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(wrapper, response.getBody());
        verify(productTypeService).searchProductTypesByName(keyword, 0, 10);
    }

    // Test for createProductType
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createProductType_Success() {
        ProductTypeRequest request = new ProductTypeRequest();
        ProductType productType = new ProductType();

        when(productTypeService.saveProductType(request)).thenReturn(productType);

        ResponseEntity<ProductType> response = productController.createProductType(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productType, response.getBody());
        verify(productTypeService).saveProductType(request);
    }

    // Test for updateProductType
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateProductType_Success() {
        Long id = 1L;
        ProductTypeRequest request = new ProductTypeRequest();
        ProductType existingProductType = new ProductType();
        ProductType updatedProductType = new ProductType();

        when(productTypeService.findProductTypeById(id)).thenReturn(existingProductType);
        when(productTypeService.updateProductType(id, request)).thenReturn(updatedProductType);

        ResponseEntity<ProductType> response = productController.updateProductType(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedProductType, response.getBody());
        verify(productTypeService).findProductTypeById(id);
        verify(productTypeService).updateProductType(id, request);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateProductType_NotFound() {
        Long id = 1L;
        ProductTypeRequest request = new ProductTypeRequest();

        when(productTypeService.findProductTypeById(id)).thenReturn(null);

        ResponseEntity<ProductType> response = productController.updateProductType(id, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productTypeService).findProductTypeById(id);
        verify(productTypeService, never()).updateProductType(anyLong(), any());
    }

    // Test for updateProductTypeAttribute
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateProductTypeAttribute_Success() {
        Long id = 1L;
        ProductTypeAttributeRequest request = new ProductTypeAttributeRequest();
        ProductTypeAttribute existingAttribute = new ProductTypeAttribute();
        ProductTypeAttribute updatedAttribute = new ProductTypeAttribute();

        when(productTypeAttributeService.getById(id)).thenReturn(existingAttribute);
        when(productTypeAttributeService.update(id, request)).thenReturn(updatedAttribute);

        ResponseEntity<ProductTypeAttribute> response = productController.updateProductTypeAttribute(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedAttribute, response.getBody());
        verify(productTypeAttributeService).getById(id);
        verify(productTypeAttributeService).update(id, request);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateProductTypeAttribute_NotFound() {
        Long id = 1L;
        ProductTypeAttributeRequest request = new ProductTypeAttributeRequest();

        when(productTypeAttributeService.getById(id)).thenReturn(null);

        ResponseEntity<ProductTypeAttribute> response = productController.updateProductTypeAttribute(id, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productTypeAttributeService).getById(id);
        verify(productTypeAttributeService, never()).update(anyLong(), any());
    }

    // Test for updateProductTypeBranch
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateProductTypeBranch_Success() {
        Long id = 1L;
        ProductTypeBranchRequest request = new ProductTypeBranchRequest();
        ProductTypeBranch existingBranch = new ProductTypeBranch();
        ProductTypeBranch updatedBranch = new ProductTypeBranch();

        when(productTypeBranchService.findProductTypeBranchById(id)).thenReturn(existingBranch);
        when(productTypeBranchService.updateProductTypeBranch(id, request)).thenReturn(updatedBranch);

        ResponseEntity<ProductTypeBranch> response = productController.updateProductTypeBranch(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedBranch, response.getBody());
        verify(productTypeBranchService).findProductTypeBranchById(id);
        verify(productTypeBranchService).updateProductTypeBranch(id, request);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateProductTypeBranch_NotFound() {
        Long id = 1L;
        ProductTypeBranchRequest request = new ProductTypeBranchRequest();

        when(productTypeBranchService.findProductTypeBranchById(id)).thenReturn(null);

        ResponseEntity<ProductTypeBranch> response = productController.updateProductTypeBranch(id, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productTypeBranchService).findProductTypeBranchById(id);
        verify(productTypeBranchService, never()).updateProductTypeBranch(anyLong(), any());
    }

    // Test for updateProductTypeVariation
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateProductTypeVariation_Success() {
        Long id = 1L;
        ProductTypeVariationRequest request = new ProductTypeVariationRequest();
        ProductTypeVariation existingVariation = new ProductTypeVariation();
        ProductTypeVariation updatedVariation = new ProductTypeVariation();

        when(productTypeVariationService.getById(id)).thenReturn(existingVariation);
        when(productTypeVariationService.update(id, request)).thenReturn(updatedVariation);

        ResponseEntity<ProductTypeVariation> response = productController.updateProductTypeVariation(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedVariation, response.getBody());
        verify(productTypeVariationService).getById(id);
        verify(productTypeVariationService).update(id, request);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateProductTypeVariation_NotFound() {
        Long id = 1L;
        ProductTypeVariationRequest request = new ProductTypeVariationRequest();

        when(productTypeVariationService.getById(id)).thenReturn(null);

        ResponseEntity<ProductTypeVariation> response = productController.updateProductTypeVariation(id, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productTypeVariationService).getById(id);
        verify(productTypeVariationService, never()).update(anyLong(), any());
    }

    // Test for createProductTypeAttribte
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createProductTypeAttribte_Success() {
        ProductTypeAttributeRequest request = new ProductTypeAttributeRequest();
        ProductTypeAttribute attribute = new ProductTypeAttribute();

        when(productTypeAttributeService.create(request)).thenReturn(attribute);

        ResponseEntity<ProductTypeAttribute> response = productController.createProductTypeAttribte(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(attribute, response.getBody());
        verify(productTypeAttributeService).create(request);
    }

    // Test for createAttribute
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createAttribute_Success() {
        AttributeRequest request = new AttributeRequest();
        Attribute attribute = new Attribute();

        when(attributeService.create(request)).thenReturn(attribute);

        ResponseEntity<Attribute> response = productController.createAttribute(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(attribute, response.getBody());
        verify(attributeService).create(request);
    }

    // Test for createProductTypeBranch
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createProductTypeBranch_Success() {
        ProductTypeBranchRequest request = new ProductTypeBranchRequest();
        ProductTypeBranch branch = new ProductTypeBranch();

        when(productTypeBranchService.saveProductTypeBranch(request)).thenReturn(branch);

        ResponseEntity<ProductTypeBranch> response = productController.createProductTypeBranch(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(branch, response.getBody());
        verify(productTypeBranchService).saveProductTypeBranch(request);
    }

    // Test for createProductTypeVariation
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createProductTypeVariation_Success() {
        ProductTypeVariationRequest request = new ProductTypeVariationRequest();
        ProductTypeVariation variation = new ProductTypeVariation();

        when(productTypeVariationService.create(request)).thenReturn(variation);

        ResponseEntity<ProductTypeVariation> response = productController.createProductTypeVariation(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(variation, response.getBody());
        verify(productTypeVariationService).create(request);
    }

    // Test for createVariation
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createVariation_Success() {
        VariationRequest request = new VariationRequest();
        Variation variation = new Variation();

        when(variationService.create(request)).thenReturn(variation);

        ResponseEntity<Variation> response = productController.createVariation(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(variation, response.getBody());
        verify(variationService).create(request);
    }

    // Test for createProduct
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createProduct_Success() {
        ProductRequest request = new ProductRequest();
        request.setCateId(1);
        request.setSuppId(2);
        Product product = new Product();

        when(productService.createProduct(request, 1, 2)).thenReturn(product);

        ResponseEntity<Product> response = productController.createProduct(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(product, response.getBody());
        verify(productService).createProduct(request, 1, 2);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void unactiveProduct_NotFound() {
        Long id = 1L;

        when(productService.findProductById(id)).thenReturn(null);

        ResponseEntity<Product> response = productController.unactiveProduct(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productService).findProductById(id);
        verify(productService, never()).unactiveProduct(anyLong());
    }

    // Test for deleteAttribute
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteAttribute_Success() {
        Long id = 1L;

        when(attributeService.delete(id)).thenReturn(true);

        ResponseEntity<Void> response = productController.deleteAttribute(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(attributeService).delete(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteAttribute_NotFound() {
        Long id = 1L;

        when(attributeService.delete(id)).thenReturn(false);

        ResponseEntity<Void> response = productController.deleteAttribute(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(attributeService).delete(id);
    }

    // Test for deleteVariation
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteVariation_Success() {
        Long id = 1L;

        when(variationService.delete(id)).thenReturn(true);

        ResponseEntity<Void> response = productController.deleteVariation(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(variationService).delete(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteVariation_NotFound() {
        Long id = 1L;

        when(variationService.delete(id)).thenReturn(false);

        ResponseEntity<Void> response = productController.deleteVariation(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(variationService).delete(id);
    }

    // Test for deleteProductType
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteProductType_Success() {
        Long id = 1L;

        when(productTypeService.deleteProductType(id)).thenReturn(true);

        ResponseEntity<Void> response = productController.deleteProductType(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productTypeService).deleteProductType(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteProductType_NotFound() {
        Long id = 1L;

        when(productTypeService.deleteProductType(id)).thenReturn(false);

        ResponseEntity<Void> response = productController.deleteProductType(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productTypeService).deleteProductType(id);
    }

    // Test for deleteProductTypeAttribute
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteProductTypeAttribute_Success() {
        Long id = 1L;

        when(productTypeAttributeService.delete(id)).thenReturn(true);

        ResponseEntity<Void> response = productController.deleteProductTypeAttribute(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productTypeAttributeService).delete(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteProductTypeAttribute_NotFound() {
        Long id = 1L;

        when(productTypeAttributeService.delete(id)).thenReturn(false);

        ResponseEntity<Void> response = productController.deleteProductTypeAttribute(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productTypeAttributeService).delete(id);
    }

    // Test for deleteProductTypeVariation
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteProductTypeVariation_Success() {
        Long id = 1L;

        when(productTypeVariationService.delete(id)).thenReturn(true);

        ResponseEntity<Void> response = productController.deleteProductTypeVariation(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productTypeVariationService).delete(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteProductTypeVariation_NotFound() {
        Long id = 1L;

        when(productTypeVariationService.delete(id)).thenReturn(false);

        ResponseEntity<Void> response = productController.deleteProductTypeVariation(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productTypeVariationService).delete(id);
    }

    // Test for deleteProductTypeBranch
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteProductTypeBranch_Success() {
        Long id = 1L;

        when(productTypeBranchService.deleteProductTypeBranch(id)).thenReturn(true);

        ResponseEntity<Void> response = productController.deleteProductTypeBranch(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productTypeBranchService).deleteProductTypeBranch(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteProductTypeBranch_NotFound() {
        Long id = 1L;

        when(productTypeBranchService.deleteProductTypeBranch(id)).thenReturn(false);

        ResponseEntity<Void> response = productController.deleteProductTypeBranch(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productTypeBranchService).deleteProductTypeBranch(id);
    }
}