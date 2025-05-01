package com.zentrix.service;

import com.zentrix.model.entity.Attribute;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeAttribute;
import com.zentrix.model.request.ProductTypeAttributeRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.ProductTypeAttributeRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.beans.factory.annotation.Autowired;
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
public class ProductTypeAttributeServiceImpl implements ProductTypeAttributeService {
    ProductTypeAttributeRepository repository;
    ProductTypeService productTypeService;
    AttributeService attributeService;

    @Override
    public boolean isAttributeUsed(Long atbId) {
        Attribute attribute = new Attribute();
        attribute.setAtbId(atbId);
        return repository.existsByAtbId(attribute);
    }

    @Override
    public PaginationWrapper<List<ProductTypeAttribute>> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ProductTypeAttribute> attributePage = repository.findAll(pageable);

        return new PaginationWrapper.Builder<List<ProductTypeAttribute>>()
                .setData(attributePage.getContent())
                .setPaginationInfo(attributePage)
                .build();
    }

    @Override
    public ProductTypeAttribute getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public ProductTypeAttribute create(ProductTypeAttributeRequest request) {
        ProductType productType = productTypeService.findProductTypeById(request.getProdTypeId());
        if (productType == null) {
            return null;
        }
        Attribute attribute = attributeService.getById(request.getAtbId());
        if (attribute == null) {
            return null;
        }

        ProductTypeAttribute createProductTypeAttribute = new ProductTypeAttribute();
        createProductTypeAttribute.setProdTypeId(productType);
        createProductTypeAttribute.setAtbId(attribute);
        createProductTypeAttribute.setProdAtbValue(request.getProdAtbValue());
        createProductTypeAttribute.setAtbDescription(request.getAtbDescription());
        return repository.save(createProductTypeAttribute);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public ProductTypeAttribute update(Long id, ProductTypeAttributeRequest productTypeAttributeRequest) {
        ProductTypeAttribute existingAttribute = repository.findById(id).orElse(null);
        if (existingAttribute == null) {
            return null;
        }

        existingAttribute.setAtbDescription(productTypeAttributeRequest.getAtbDescription());
        existingAttribute.setProdAtbValue(productTypeAttributeRequest.getProdAtbValue());
        return repository.save(existingAttribute);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}