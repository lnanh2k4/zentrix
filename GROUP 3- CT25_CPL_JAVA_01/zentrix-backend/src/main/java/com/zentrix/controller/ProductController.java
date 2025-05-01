package com.zentrix.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.zentrix.model.entity.Attribute;
import com.zentrix.model.entity.ImageProductType;
import com.zentrix.model.entity.Product;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeAttribute;
import com.zentrix.model.entity.ProductTypeBranch;
import com.zentrix.model.entity.ProductTypeVariation;
import com.zentrix.model.entity.Variation;
import com.zentrix.model.request.AttributeRequest;
import com.zentrix.model.request.ProductRequest;
import com.zentrix.model.request.ProductTypeAttributeRequest;
import com.zentrix.model.request.ProductTypeBranchRequest;
import com.zentrix.model.request.ProductTypeRequest;
import com.zentrix.model.request.ProductTypeVariationRequest;
import com.zentrix.model.request.VariationRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.repository.ProductTypeAttributeRepository;
import com.zentrix.repository.ProductTypeBranchRepository;
import com.zentrix.repository.ProductTypeVariationRepository;
import com.zentrix.service.AttributeService;
import com.zentrix.service.ImageProductTypeService;
import com.zentrix.service.ProductService;
import com.zentrix.service.ProductTypeAttributeService;
import com.zentrix.service.ProductTypeBranchService;
import com.zentrix.service.ProductTypeService;
import com.zentrix.service.ProductTypeVariationService;
import com.zentrix.service.VariationService;
import org.springframework.http.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/products")
@Tag(name = "Product Controller", description = "This class contains the product CRUD method.")
public class ProductController {

    ProductService productService;
    ProductTypeAttributeService productTypeAttributeService;
    ProductTypeService productTypeService;
    ProductTypeBranchService productTypeBranchService;
    ProductTypeVariationService productTypeVariationService;
    AttributeService attributeService;
    VariationService variationService;
    ImageProductTypeService imageProductTypeService;
    ProductTypeVariationRepository productTypeVariationRepository;
    ProductTypeAttributeRepository productTypeAttributeRepository;
    ProductTypeBranchRepository productTypeBranchRepository;

    /**
     * Creates a new image for a product type.
     *
     * @param file          the image file to upload
     * @param productTypeId the ID of the product type to associate the image with
     * @return ResponseEntity containing the created ImageProductType entity, or an
     *         error status
     */
    @PostMapping(value = "/productType/createImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Transactional
    public ResponseEntity<ImageProductType> createImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productTypeId") Long productTypeId) {
        try {
            System.out.println("Received request: productTypeId=" + productTypeId + ", file="
                    + file.getOriginalFilename());
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            System.out.println("Creating new image for productTypeId=" + productTypeId);
            ImageProductType imageProductType = imageProductTypeService.create(file, productTypeId);

            System.out.println("Image created successfully: " + imageProductType.getImageProdId());
            return ResponseEntity.ok(imageProductType);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all product types associated with a specific product ID.
     *
     * @param prodId the ID of the product
     * @return ResponseEntity containing a list of ProductType entities
     */
    @GetMapping("/{prodId}/productTypes")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get all productType by product id", description = "This method is used to get all productType by product")
    public ResponseEntity<List<ProductType>> getProductTypesByProductId(@PathVariable Long prodId) {
        List<ProductType> productTypes = productTypeService.findProductTypesByProductId(prodId);
        return ResponseEntity.ok(productTypes);
    }

    @GetMapping("/product/searchBy/{prodTypeId}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get product by product type ID", description = "Retrieves the product associated with a given product type ID")
    public ResponseEntity<Product> getProductByProductTypeId(@PathVariable Long prodTypeId) {
        Product product = productService.findProductByProdTypeId(prodTypeId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    /**
     * Retrieves a specific product type by its ID.
     *
     * @param prodTypeId the ID of the product type
     * @return ResponseEntity containing the ProductType entity
     */
    @GetMapping("/productTypes/{prodTypeId}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get productType by ID", description = "This method is used to get a specific productType by its ID")
    public ResponseEntity<ProductType> getProductTypesByProdTypeId(@PathVariable Long prodTypeId) {
        ProductType productTypes = productTypeService.findProductTypeById(prodTypeId);
        return ResponseEntity.ok(productTypes);
    }

    /**
     * Creates multiple images for a product type.
     *
     * @param files         the list of image files to upload
     * @param productTypeId the ID of the product type to associate the images with
     * @return ResponseEntity containing a list of created ImageProductType
     *         entities, or an error status
     */
    @PostMapping(value = "/productType/createImages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Create multiple images for a product type", description = "This method is used to upload and create multiple images for a product type")
    @Transactional
    public ResponseEntity<List<ImageProductType>> createImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("productTypeId") Long productTypeId) {
        try {
            System.out.println("Received request: productTypeId=" + productTypeId + ", files count=" + files.size());
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            System.out.println("Creating new images for productTypeId=" + productTypeId);
            List<ImageProductType> imageProductTypes = imageProductTypeService.createMultiple(files, productTypeId);

            System.out.println("Images created successfully: " + imageProductTypes.size());
            return ResponseEntity.ok(imageProductTypes);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes all images associated with a product type.
     *
     * @param productTypeId the ID of the product type
     * @return ResponseEntity with no content on success, or an error status
     */
    @DeleteMapping("/productType/images/{productTypeId}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Delete all images of product type", description = "This method is used to delete all image product types")
    @Transactional
    public ResponseEntity<Void> deleteImages(@PathVariable Long productTypeId) {
        imageProductTypeService.deleteImagesByProductType(productTypeId);
        return ResponseEntity.ok().build();
    }

    /**
     * Deletes a specific image by its ID.
     *
     * @param imageProdId the ID of the image to delete
     * @return ResponseEntity with no content on success, or an error status
     */
    @DeleteMapping("/productType/image/{imageProdId}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Delete a specific image of product type", description = "This method is used to delete a specific image by imageProdId")
    @Transactional
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageProdId) {
        try {
            imageProductTypeService.deleteImageById(imageProdId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error deleting image: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all product type variations by product type ID.
     *
     * @param prodTypeId the ID of the product type
     * @return ResponseEntity containing a list of ProductTypeVariation entities, or
     *         an error status
     */
    @GetMapping("/productTypeVariation/{prodTypeId}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get productTypeVariation by ProductTypeId", description = "This method is used to get Typevariation by prodTypeId")
    public ResponseEntity<List<ProductTypeVariation>> getProductTypeVariationbyProId(@PathVariable Long prodTypeId) {
        try {
            List<ProductTypeVariation> prodTypeBranch = productTypeVariationRepository
                    .findByProdTypeId(productTypeService.findProductTypeById(prodTypeId));
            return ResponseEntity.ok(prodTypeBranch);
        } catch (Exception e) {
            System.err.println("Error fetching productTypeVariation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all product type branches by product type ID.
     *
     * @param prodTypeId the ID of the product type
     * @return ResponseEntity containing a list of ProductTypeBranch entities, or an
     *         error status
     */
    @GetMapping("/productTypeBranch/{prodTypeId}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get ProductTypeBranch by ProductTypeId", description = "This method retrieves all ProductTypeBranch entities associated with a given ProductType ID")
    public ResponseEntity<List<ProductTypeBranch>> getProductTypeBranchByProdTypeId(@PathVariable Long prodTypeId) {
        try {
            List<ProductTypeBranch> prodTypeBranches = productTypeBranchService
                    .findByProdTypeId(productTypeService.findProductTypeById(prodTypeId));

            if (prodTypeBranches.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(prodTypeBranches);
        } catch (Exception e) {
            System.err.println("Error fetching ProductTypeBranch: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all product type attributes by product type ID.
     *
     * @param prodTypeId the ID of the product type
     * @return ResponseEntity containing a list of ProductTypeAttribute entities, or
     *         an error status
     */
    @GetMapping("/productTypeAttribute/{prodTypeId}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get productTypeAttribute by ProductTypeId", description = "This method is used to get TypeAttribute by prodId")
    public ResponseEntity<List<ProductTypeAttribute>> getProductTypeAttributebyProId(@PathVariable Long prodTypeId) {
        try {
            List<ProductTypeAttribute> prodTypeBranch = productTypeAttributeRepository
                    .findByProdTypeId(productTypeService.findProductTypeById(prodTypeId));
            return ResponseEntity.ok(prodTypeBranch);
        } catch (Exception e) {
            System.err.println("Error fetching productTypeAttribute: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Checks if an attribute is used in any product type attribute.
     *
     * @param atbId the ID of the attribute
     * @return ResponseEntity containing a boolean indicating usage
     */
    @GetMapping("/attributes/{atbId}/usage")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    public ResponseEntity<Boolean> checkAttributeUsage(@PathVariable Long atbId) {
        boolean isUsed = productTypeAttributeService.isAttributeUsed(atbId);
        return ResponseEntity.ok(isUsed);
    }

    /**
     * Checks if a variation is used in any product type variation.
     *
     * @param variId the ID of the variation
     * @return ResponseEntity containing a boolean indicating usage
     */
    @GetMapping("/variations/{variId}/usage")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    public ResponseEntity<Boolean> checkVariationUsage(@PathVariable Long variId) {
        boolean isUsed = productTypeVariationService.isVariationUsed(variId);
        return ResponseEntity.ok(isUsed);
    }

    /**
     * Retrieves all images associated with a product type ID.
     *
     * @param prodTypeId the ID of the product type
     * @return ResponseEntity containing a list of ImageProductType entities, or an
     *         error status
     */
    @GetMapping("/ImageProduct/{prodTypeId}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get images by product type ID", description = "This method is used to get all images for a product type")
    public ResponseEntity<List<ImageProductType>> getImagesByProductType(@PathVariable Long prodTypeId) {
        try {
            List<ImageProductType> images = imageProductTypeService.getByProdId(prodTypeId);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            System.err.println("Error fetching images: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the ID of the product
     * @return ResponseEntity containing the Product entity, or NOT_FOUND if not
     *         found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get product", description = "This method is used to get a product by product ID")
    public ResponseEntity<Product> getProductById(@Valid @PathVariable Long id) {
        Product product = productService.findProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(product);
    }

    /**
     * Finds the ProductTypeBranch ID based on product type ID and branch ID.
     *
     * @param prodTypeId the ID of the product type
     * @param brchId     the ID of the branch
     * @return ResponseEntity containing the ProductTypeBranch ID, or NOT_FOUND if
     *         not found
     */
    @GetMapping("/productTypeBranch/find")
    @Operation(summary = "Find ProductTypeBranch by prodTypeId and brchId", description = "This method retrieves the ProductTypeBranch ID based on prodTypeId and brchId")
    public ResponseEntity<Long> findProductTypeBranchId(
            @RequestParam("prodTypeId") Long prodTypeId,
            @RequestParam("brchId") Long brchId) {
        try {
            ProductTypeBranch productTypeBranch = productTypeBranchService.findByProdTypeIdAndBrchId(prodTypeId,
                    brchId);

            if (productTypeBranch == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            return ResponseEntity.ok(productTypeBranch.getProdTypeBrchId());
        } catch (Exception e) {
            System.err.println("Error fetching ProductTypeBranch: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves all products with optional status filtering and pagination.
     *
     * @param page   the page number (default: 0)
     * @param size   the number of items per page (default: 100)
     * @param status optional comma-separated list of status values (e.g., "1,0")
     * @return ResponseEntity containing a ResponseObject with a list of Product
     *         entities
     */
    @GetMapping
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get all products", description = "This method is used to get all products, optionally filtered by status")
    public ResponseEntity<ResponseObject<List<Product>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String status) {
        PaginationWrapper<List<Product>> wrapper;

        if (status != null && !status.isEmpty()) {
            List<Integer> statusList = Arrays.stream(status.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            wrapper = productService.getProductsByStatus(statusList, page, size);
        } else {
            wrapper = productService.getAllProducts(page, size);
        }

        ResponseObject<List<Product>> response = new ResponseObject.Builder<List<Product>>()
                .unwrapPaginationWrapper(wrapper)
                .message("Get products list successfully!")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all product type attributes with pagination.
     *
     * @param page the page number (default: 0)
     * @param size the number of items per page (default: 10000)
     * @return ResponseEntity containing a ResponseObject with a list of
     *         ProductTypeAttribute entities
     */
    @GetMapping("/productTypeAttributes")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get all productTypeAttribute", description = "This method is used to get all productTypeAttribute")
    public ResponseEntity<ResponseObject<List<ProductTypeAttribute>>> getAllProductTypeAttribute(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size) {
        PaginationWrapper<List<ProductTypeAttribute>> wrapper = productTypeAttributeService.getAll(page, size);
        ResponseObject<List<ProductTypeAttribute>> response = new ResponseObject.Builder<List<ProductTypeAttribute>>()
                .unwrapPaginationWrapper(wrapper)
                .message("Get productTypeAttributes list successfully!")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all product type branches with pagination.
     *
     * @param page the page number (default: 0)
     * @param size the number of items per page (default: 10000)
     * @return ResponseEntity containing a ResponseObject with a list of
     *         ProductTypeBranch entities
     */
    @GetMapping("/productTypeBranchs")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get all productTypeBranch", description = "This method is used to get all productTypeBranch")
    public ResponseEntity<ResponseObject<List<ProductTypeBranch>>> getAllProductTypeBranch(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size) {
        PaginationWrapper<List<ProductTypeBranch>> wrapper = productTypeBranchService
                .getAllProductTypeBranches(page, size);
        ResponseObject<List<ProductTypeBranch>> response = new ResponseObject.Builder<List<ProductTypeBranch>>()
                .unwrapPaginationWrapper(wrapper)
                .message("Get productTypeAttributes list successfully!")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all product type variations with pagination.
     *
     * @param page the page number (default: 0)
     * @param size the number of items per page (default: 10000)
     * @return ResponseEntity containing a ResponseObject with a list of
     *         ProductTypeVariation entities
     */
    @GetMapping("/productTypeVariations")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get all productTypeVariation", description = "This method is used to get all productTypeVariation")
    public ResponseEntity<ResponseObject<List<ProductTypeVariation>>> getAllProductTypeVariations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size) {
        PaginationWrapper<List<ProductTypeVariation>> wrapper = productTypeVariationService.getAll(page, size);
        ResponseObject<List<ProductTypeVariation>> response = new ResponseObject.Builder<List<ProductTypeVariation>>()
                .unwrapPaginationWrapper(wrapper)
                .message("Get productTypeAttributes list successfully!")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all product types with pagination.
     *
     * @param page the page number (default: 0)
     * @param size the number of items per page (default: 10000)
     * @return ResponseEntity containing a ResponseObject with a list of ProductType
     *         entities
     */
    @GetMapping("/productTypes")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get all productTypes", description = "This method is used to get all productTypes")
    public ResponseEntity<ResponseObject<List<ProductType>>> getAllProductTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size) {
        PaginationWrapper<List<ProductType>> wrapper = productTypeService.getAllProductTypes(page, size);
        ResponseObject<List<ProductType>> response = new ResponseObject.Builder<List<ProductType>>()
                .unwrapPaginationWrapper(wrapper)
                .message("Get productTypeAttributes list successfully!")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all product types for the homepage with pagination, accessible by
     * guests.
     *
     * @param page the page number (default: 0)
     * @param size the number of items per page (default: 10000)
     * @return ResponseEntity containing a ResponseObject with a list of ProductType
     *         entities
     */
    @GetMapping("/homepage")
    @PreAuthorize("hasRole('GUEST')")
    @Operation(summary = "Get all productTypes in Homepage", description = "This method is used to get all productTypes")
    public ResponseEntity<ResponseObject<List<ProductType>>> getAllProductTypesForCustomer(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size) {
        PaginationWrapper<List<ProductType>> wrapper = productTypeService.getAllProductTypes(page, size);
        ResponseObject<List<ProductType>> response = new ResponseObject.Builder<List<ProductType>>()
                .unwrapPaginationWrapper(wrapper)
                .message("Get productTypeAttributes list successfully!")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for products by name with pagination.
     *
     * @param keyword the search keyword
     * @param page    the page number (default: 0)
     * @param size    the number of items per page (default: 10)
     * @return ResponseEntity containing a ResponseObject with a list of Product
     *         entities
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Search product", description = "This method is used to search for a product")
    public ResponseEntity<ResponseObject<List<Product>>> searchProduct(
            @RequestParam(value = "keyword", required = true) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        PaginationWrapper<List<Product>> wrapper = productService.searchProductByName(keyword, page, size);
        ResponseObject<List<Product>> response = new ResponseObject.Builder<List<Product>>()
                .unwrapPaginationWrapper(wrapper)
                .message("Product search completed successfully!")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/productTypes/search")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Search productType", description = "This method is used to search for a productType")
    public ResponseEntity<PaginationWrapper<List<ProductType>>> searchProductTypes(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginationWrapper<List<ProductType>> result = productTypeService.searchProductTypesByName(keyword, page,
                size);
        return ResponseEntity.ok(result);
    }

    /**
     * Creates a new product type.
     *
     * @param request the ProductTypeRequest containing product type details
     * @return ResponseEntity containing the created ProductType entity
     */
    @PostMapping("/createProductType")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Create product Type", description = "This method is used to create a new product type")
    public ResponseEntity<ProductType> createProductType(@Valid @RequestBody ProductTypeRequest request) {
        ProductType productType = productTypeService.saveProductType(request);
        return ResponseEntity.ok(productType);
    }

    /**
     * Updates an existing product type.
     *
     * @param id      the ID of the product type to update
     * @param request the ProductTypeRequest containing updated details
     * @return ResponseEntity containing the updated ProductType entity, or
     *         NOT_FOUND if not found
     */
    @PutMapping("/updateProductType/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Update product type", description = "This method is used to update an existing product type")
    public ResponseEntity<ProductType> updateProductType(@Valid @PathVariable Long id,
            @RequestBody ProductTypeRequest request) {
        ProductType existingProductType = productTypeService.findProductTypeById(id);
        if (existingProductType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        ProductType updatedProductType = productTypeService.updateProductType(id, request);
        return ResponseEntity.ok(updatedProductType);
    }

    /**
     * Updates an existing product type attribute.
     *
     * @param id      the ID of the product type attribute to update
     * @param request the ProductTypeAttributeRequest containing updated details
     * @return ResponseEntity containing the updated ProductTypeAttribute entity, or
     *         NOT_FOUND if not found
     */
    @PutMapping("/updateProductTypeAttribute/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Update productTypeAttribute", description = "This method is used to update an existing product type attribute")
    public ResponseEntity<ProductTypeAttribute> updateProductTypeAttribute(
            @Valid @PathVariable Long id,
            @RequestBody ProductTypeAttributeRequest request) {
        ProductTypeAttribute existingProductType = productTypeAttributeService.getById(id);
        if (existingProductType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        ProductTypeAttribute updatedProductTypeAttribute = productTypeAttributeService.update(id, request);
        return ResponseEntity.ok(updatedProductTypeAttribute);
    }

    /**
     * Updates an existing product type branch.
     *
     * @param id      the ID of the product type branch to update
     * @param request the ProductTypeBranchRequest containing updated details
     * @return ResponseEntity containing the updated ProductTypeBranch entity, or
     *         NOT_FOUND if not found
     */
    @PutMapping("/updateProductTypeBranch/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Update productTypeBranch", description = "This method is used to update an existing product type branch")
    public ResponseEntity<ProductTypeBranch> updateProductTypeBranch(
            @Valid @PathVariable Long id,
            @RequestBody ProductTypeBranchRequest request) {
        ProductTypeBranch existingProductType = productTypeBranchService.findProductTypeBranchById(id);
        if (existingProductType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        ProductTypeBranch updatedProductTypeBranch = productTypeBranchService.updateProductTypeBranch(id,
                request);
        return ResponseEntity.ok(updatedProductTypeBranch);
    }

    /**
     * Updates an existing product type variation.
     *
     * @param id      the ID of the product type variation to update
     * @param request the ProductTypeVariationRequest containing updated details
     * @return ResponseEntity containing the updated ProductTypeVariation entity, or
     *         NOT_FOUND if not found
     */
    @PutMapping("/updateProductTypeVariation/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Update productTypeVariation", description = "This method is used to update an existing product type variation")
    public ResponseEntity<ProductTypeVariation> updateProductTypeVariation(
            @Valid @PathVariable Long id,
            @RequestBody ProductTypeVariationRequest request) {
        ProductTypeVariation existingProductType = productTypeVariationService.getById(id);
        if (existingProductType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        ProductTypeVariation updatedProductTypeVariation = productTypeVariationService.update(id, request);
        return ResponseEntity.ok(updatedProductTypeVariation);
    }

    /**
     * Creates a new product type attribute.
     *
     * @param request the ProductTypeAttributeRequest containing attribute details
     * @return ResponseEntity containing the created ProductTypeAttribute entity
     */
    @PostMapping("/createProductTypeAttribute")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Create product Type Attribute", description = "This method is used to create a new product type attribute")
    public ResponseEntity<ProductTypeAttribute> createProductTypeAttribte(
            @Valid @RequestBody ProductTypeAttributeRequest request) {
        ProductTypeAttribute productTypeAttribute = productTypeAttributeService.create(request);
        return ResponseEntity.ok(productTypeAttribute);
    }

    /**
     * Creates a new attribute.
     *
     * @param request the AttributeRequest containing attribute details
     * @return ResponseEntity containing the created Attribute entity
     */
    @PostMapping("/createAttribute")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Create Attribute", description = "This method is used to create a new attribute")
    public ResponseEntity<Attribute> createAttribute(@Valid @RequestBody AttributeRequest request) {
        Attribute attribute = attributeService.create(request);
        return ResponseEntity.ok(attribute);
    }

    /**
     * Creates a new product type branch.
     *
     * @param request the ProductTypeBranchRequest containing branch details
     * @return ResponseEntity containing the created ProductTypeBranch entity
     */
    @PostMapping("/createProductTypeBranch")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Create product Type Branch", description = "This method is used to create a new product type branch")
    public ResponseEntity<ProductTypeBranch> createProductTypeBranch(
            @Valid @RequestBody ProductTypeBranchRequest request) {
        ProductTypeBranch productTypeBranch = productTypeBranchService.saveProductTypeBranch(request);
        return ResponseEntity.ok(productTypeBranch);
    }

    /**
     * Creates a new product type variation.
     *
     * @param request the ProductTypeVariationRequest containing variation details
     * @return ResponseEntity containing the created ProductTypeVariation entity
     */
    @PostMapping("/createProductTypeVariation")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Create product Type Variation", description = "This method is used to create a new product type branch variation")
    public ResponseEntity<ProductTypeVariation> createProductTypeVariation(
            @Valid @RequestBody ProductTypeVariationRequest request) {
        ProductTypeVariation productTypeVariation = productTypeVariationService.create(request);
        return ResponseEntity.ok(productTypeVariation);
    }

    /**
     * Creates a new variation.
     *
     * @param request the VariationRequest containing variation details
     * @return ResponseEntity containing the created Variation entity
     */
    @PostMapping("/createVariation")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Create Variation", description = "This method is used to create a new variation")
    public ResponseEntity<Variation> createVariation(@Valid @RequestBody VariationRequest request) {
        Variation variation = variationService.create(request);
        return ResponseEntity.ok(variation);
    }

    /**
     * Creates a new product.
     *
     * @param request the ProductRequest containing product details
     * @return ResponseEntity containing the created Product entity
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Create product", description = "This method is used to create a new product")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = productService.createProduct(request, request.getCateId(), request.getSuppId());
        return ResponseEntity.ok(product);
    }

    /**
     * Updates an existing product.
     *
     * @param id      the ID of the product to update
     * @param request the ProductRequest containing updated details
     * @return ResponseEntity containing the updated Product entity, or NOT_FOUND if
     *         not found
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Update product", description = "This method is used to update an existing product")
    public ResponseEntity<Product> updateProduct(@Valid @PathVariable Long id,
            @RequestBody ProductRequest request) {
        Product existingProduct = productService.findProductById(id);
        if (existingProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Product updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Deactivates an existing product by setting its status to inactive.
     *
     * @param id the ID of the product to deactivate
     * @return ResponseEntity containing the updated Product entity, or NOT_FOUND if
     *         not found
     */
    @PutMapping("/unactive/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE STAFF', 'ADMIN')")
    @Operation(summary = "Deactivate product", description = "This method deactivates an existing product by setting its status to inactive")
    public ResponseEntity<Product> unactiveProduct(@Valid @PathVariable Long id) {
        Product existingProduct = productService.findProductById(id);
        if (existingProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        productService.unactiveProduct(id);
        return ResponseEntity.ok(existingProduct);
    }

    /**
     * Soft deletes an existing product by setting its status to inactive.
     *
     * @param id the ID of the product to soft delete
     * @return ResponseEntity containing the updated Product entity, or NOT_FOUND if
     *         not found
     */
    @PutMapping("/softDeleteProduct/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE STAFF', 'ADMIN')")
    @Operation(summary = "Soft delete product", description = "This method deactivates an existing product by setting its status to inactive")
    public ResponseEntity<Product> softDeleteProduct(@Valid @PathVariable Long id) {
        Product existingProduct = productService.findProductById(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authorities: " + auth.getAuthorities());
        if (existingProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        productService.softdelete(id);
        return ResponseEntity.ok(existingProduct);
    }

    /**
     * Deactivates an existing product type by setting its status to inactive.
     *
     * @param id the ID of the product type to deactivate
     * @return ResponseEntity containing the updated ProductType entity, or
     *         NOT_FOUND if not found
     */
    @PutMapping("/unactiveProductType/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Deactivate productType", description = "This method deactivates an existing product by setting its status to inactive")
    public ResponseEntity<ProductType> unactiveProductType(@Valid @PathVariable Long id,
            @RequestParam(name = "status") Integer status) {
        ProductType existingProductType = productTypeService.findProductTypeById(id);
        if (existingProductType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        productTypeService.unactiveProductType(id, status);
        return ResponseEntity.ok(existingProductType);
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id the ID of the product to delete
     * @return ResponseEntity with no content on success, or NOT_FOUND if not found
     */
    @DeleteMapping("/deleteProduct/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Delete product", description = "This method is used to delete a product (set status to 0)")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.deleteProduct(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Deletes an attribute by its ID.
     *
     * @param id the ID of the attribute to delete
     * @return ResponseEntity with no content on success, or NOT_FOUND if not found
     */
    @DeleteMapping("/deleteAttribute/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Delete attribute", description = "This method is used to delete a attribute")
    public ResponseEntity<Void> deleteAttribute(@PathVariable Long id) {
        if (attributeService.delete(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Deletes a variation by its ID.
     *
     * @param id the ID of the variation to delete
     * @return ResponseEntity with no content on success, or NOT_FOUND if not found
     */
    @DeleteMapping("/deleteVariation/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Delete variation", description = "This method is used to delete a variation")
    public ResponseEntity<Void> deleteVariation(@PathVariable Long id) {
        if (variationService.delete(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Deletes a product type by its ID.
     *
     * @param id the ID of the product type to delete
     * @return ResponseEntity with no content on success, or NOT_FOUND if not found
     */
    @DeleteMapping("/deleteProductType/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Delete product type", description = "This method is used to delete a product type")
    public ResponseEntity<Void> deleteProductType(@PathVariable Long id) {
        if (productTypeService.deleteProductType(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Deletes a product type attribute by its ID.
     *
     * @param id the ID of the product type attribute to delete
     * @return ResponseEntity with no content on success, or NOT_FOUND if not found
     */
    @DeleteMapping("/deleteProductTypeAttribute/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Delete product type attribute", description = "This method is used to delete a product type attribute")
    public ResponseEntity<Void> deleteProductTypeAttribute(@PathVariable Long id) {
        if (productTypeAttributeService.delete(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Deletes a product type variation by its ID.
     *
     * @param id the ID of the product type variation to delete
     * @return ResponseEntity with no content on success, or NOT_FOUND if not found
     */
    @DeleteMapping("/deleteProductTypeVariation/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Delete product type variation", description = "This method is used to delete a product type variation")
    public ResponseEntity<Void> deleteProductTypeVariation(@PathVariable Long id) {
        if (productTypeVariationService.delete(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Deletes a product type branch by its ID.
     *
     * @param id the ID of the product type branch to delete
     * @return ResponseEntity with no content on success, or NOT_FOUND if not found
     */
    @DeleteMapping("/deleteProductTypeBranch/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Delete product type branch", description = "This method is used to delete a product type branch")
    public ResponseEntity<Void> deleteProductTypeBranch(@PathVariable Long id) {
        if (productTypeBranchService.deleteProductTypeBranch(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}