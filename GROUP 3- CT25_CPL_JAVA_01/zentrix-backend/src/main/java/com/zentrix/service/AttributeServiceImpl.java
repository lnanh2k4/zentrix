package com.zentrix.service;

import com.zentrix.model.entity.Attribute;
import com.zentrix.model.request.AttributeRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.AttributeRepository;

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
public class AttributeServiceImpl implements AttributeService {
    AttributeRepository repository;

    @Override
    public PaginationWrapper<List<Attribute>> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Attribute> attributePage = repository.findAll(pageable);
        return new PaginationWrapper.Builder<List<Attribute>>()
                .setData(attributePage.getContent())
                .setPaginationInfo(attributePage)
                .build();
    }

    @Override
    public Attribute getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public Attribute create(AttributeRequest attribute) {
        Attribute createAttribute = new Attribute();
        createAttribute.setAtbName(attribute.getAtbName());
        return repository.save(createAttribute);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public Attribute update(Long id, Attribute attribute) {
        if (repository.existsById(id)) {
            attribute.setAtbId(id); // Ensure the attribute keeps its original ID.
            return repository.save(attribute);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean delete(Long id) {
        repository.deleteById(id);
        return true;
    }
}