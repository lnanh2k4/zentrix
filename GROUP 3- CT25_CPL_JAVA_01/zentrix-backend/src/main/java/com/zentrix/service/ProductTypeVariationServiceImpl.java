package com.zentrix.service;

import com.zentrix.model.entity.Attribute;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeVariation;
import com.zentrix.model.entity.Variation;
import com.zentrix.model.request.ProductTypeVariationRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.ProductTypeVariationRepository;

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
 * @date February 17, 2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductTypeVariationServiceImpl implements ProductTypeVariationService {
    ProductTypeVariationRepository repository;
    ProductTypeService productTypeService;
    VariationService variationService;

    @Override
    public boolean isVariationUsed(Long variId) {
        Variation variation = new Variation();
        variation.setVariId(variId);
        return repository.existsByVariId(variation);
    }

    @Override
    public PaginationWrapper<List<ProductTypeVariation>> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ProductTypeVariation> variationPage = repository.findAll(pageable);

        return new PaginationWrapper.Builder<List<ProductTypeVariation>>()
                .setData(variationPage.getContent())
                .setPaginationInfo(variationPage)
                .build();
    }

    @Override
    public ProductTypeVariation getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public ProductTypeVariation create(ProductTypeVariationRequest request) {
        ProductType productType = productTypeService.findProductTypeById(request.getProdTypeId());
        if (productType == null) {
            return null;
        }
        Variation variation = variationService.getById(request.getVariId());
        if (variation == null) {
            return null;
        }
        ProductTypeVariation productTypeVariation = new ProductTypeVariation();
        productTypeVariation.setProdTypeId(productType);
        productTypeVariation.setVariId(variation);
        productTypeVariation.setProdTypeValue(request.getProdTypeValue());
        if (request.getDefaultVari() == null) {
            productTypeVariation.setDefaultVari(0);
        } else {
            productTypeVariation.setDefaultVari(1);
        }
        return repository.save(productTypeVariation);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public ProductTypeVariation update(Long id, ProductTypeVariationRequest request) {
        return repository.findById(id).map(existing -> {
            Variation variation = variationService.getById(request.getVariId());
            ProductType productType = productTypeService.findProductTypeById(request.getProdTypeId());
            if (variation == null || productType == null) {
                return null;
            }
            existing.setVariId(variation);
            existing.setProdTypeId(productType);
            existing.setProdTypeValue(request.getProdTypeValue());
            return repository.save(existing);
        }).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean delete(Long id) {
        repository.deleteById(id);
        return true;
    }
}