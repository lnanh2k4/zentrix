package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.Product;
import com.zentrix.model.request.ProductRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 11, 2025
 */
public interface ProductService {
    /**
     * This method uses to create a new product
     * 
     * @param req      Information of a product
     * @param category category of prodct
     * @param supplier supplier of product
     * @return product has been created
     */
    Product createProduct(ProductRequest req, Integer category, Integer supplier);

    /**
     * This method uses to update product
     * 
     * @param proId id of product need to update
     * @param req   new information of product to change
     * @return product has been changed
     */
    Product updateProduct(Long proId, ProductRequest req);

    /**
     * This method uses to find a product by Id
     * 
     * @param proId Id of product
     * @return product has been found by id param
     */
    Product findProductById(Long proId);

    /**
     * This method uses to get all product
     * 
     * @return List of all product available
     */
    PaginationWrapper<List<Product>> getAllProducts(int page, int size);

    /**
     * This method uses to get all product by status (1: available, 0: unavailable,
     * 3: inactive)
     * 
     * @return List of all product available by status
     */
    PaginationWrapper<List<Product>> getProductsByStatus(List<Integer> statusList, int page, int size);

    /**
     * This method uses to search a product by name
     * 
     * @param keyword name of a product
     * @param page    current page
     * @param size    amount element a page
     * @return a product has been found by name
     */
    PaginationWrapper<List<Product>> searchProductByName(String keyword, int page, int size);

    /**
     * This method uses to delete a product (not use)
     * 
     * @param proId id of product
     * @return product set to inactive
     */
    boolean deleteProduct(Long proId);

    /**
     * This method uses to inactive a product by set status product to 0 , else 1
     * 
     * @param proId id of product
     * @return product set to inactive
     */
    boolean unactiveProduct(Long proId);

    /**
     * This method uses to inactive a product by set status product to 3
     * 
     * @param proId id of product
     * @return product set to inactive
     */
    public boolean softdelete(Long id);

    public Product findProductByProdTypeId(Long proTypeId);

}
