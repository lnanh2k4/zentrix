package com.zentrix.service;

import com.zentrix.model.entity.Branch;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeBranch;
import com.zentrix.model.request.ProductTypeBranchRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.ProductTypeBranchRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 14, 2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductTypeBranchServiceImpl implements ProductTypeBranchService {
    ProductTypeBranchRepository productTypeBranchRepository;
    ProductTypeService productTypeService;
    BranchService branchService;

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public ProductTypeBranch saveProductTypeBranch(ProductTypeBranchRequest request) {
        ProductType productType = productTypeService.findProductTypeById(request.getProdTypeId());
        if (productType == null) {
            return null;
        }
        Branch branch = branchService.getBranchById(request.getBrchId());
        if (branch == null) {
            return null;
        }

        ProductTypeBranch existingProductTypeBranch = productTypeBranchRepository
                .findByProdTypeIdAndBrchId(productType, branch);

        if (existingProductTypeBranch != null) {
            existingProductTypeBranch.setQuantity(
                    existingProductTypeBranch.getQuantity() + request.getQuantity());
            return productTypeBranchRepository.save(existingProductTypeBranch);
        } else {
            ProductTypeBranch productTypeBranch = new ProductTypeBranch();
            productTypeBranch.setBrchId(branch);
            productTypeBranch.setProdTypeId(productType);
            productTypeBranch.setQuantity(request.getQuantity());
            productTypeBranch.setStatus(1);
            return productTypeBranchRepository.save(productTypeBranch);
        }
    }

    @Override
    public ProductTypeBranch findProductTypeBranchById(Long id) {

        return productTypeBranchRepository.findById(id).orElse(null);
    }

    @Override
    public PaginationWrapper<List<ProductTypeBranch>> getAllProductTypeBranches(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductTypeBranch> branchPage = productTypeBranchRepository.findAll(pageable);
        return new PaginationWrapper.Builder<List<ProductTypeBranch>>()
                .setData(branchPage.getContent())
                .setPaginationInfo(branchPage)
                .build();
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean deleteProductTypeBranch(Long id) {
        if (productTypeBranchRepository.existsById(id)) {
            productTypeBranchRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public ProductTypeBranch updateProductTypeBranch(Long id, ProductTypeBranchRequest request) {
        ProductTypeBranch existingProductTypeBranch = productTypeBranchRepository.findById(id).orElse(null);
        if (existingProductTypeBranch == null) {
            return null;
        }

        ProductType productType = productTypeService.findProductTypeById(request.getProdTypeId());
        Branch branch = branchService.getBranchById(request.getBrchId());
        if (productType == null || branch == null) {
            return null;
        }

        existingProductTypeBranch.setProdTypeId(productType);
        existingProductTypeBranch.setBrchId(branch);
        existingProductTypeBranch.setQuantity(request.getQuantity());

        return productTypeBranchRepository.save(existingProductTypeBranch);
    }

    @Override
    public List<ProductTypeBranch> findByProdTypeId(ProductType productType) {
        if (productType == null) {
            return List.of();
        }
        return productTypeBranchRepository.findByProdTypeId(productType);
    }

    @Override
    public List<ProductTypeBranch> getProductTypeBranchByProdTypeId(Long id) {
        if (id == null) {
            return List.of();
        }
        return productTypeBranchRepository.findByProdTypeIdProdTypeId(id);
    }

    @Override
    public ProductTypeBranch findByProdTypeIdAndBrchId(Long prodTypeId, Long brchId) {
        if (prodTypeId == null || brchId == null) {
            return null;
        }
        return productTypeBranchRepository.findByProdTypeIdProdTypeIdAndBrchIdBrchId(prodTypeId, brchId);
    }

}