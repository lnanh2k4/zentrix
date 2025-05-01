package com.zentrix.service;

import com.zentrix.model.entity.Variation;
import com.zentrix.model.request.VariationRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.VariationRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class VariationServiceImpl implements VariationService {
    VariationRepository repository;

    @Override
    public PaginationWrapper<List<Variation>> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Variation> variationPage = repository.findAll(pageable);
        return new PaginationWrapper.Builder<List<Variation>>()
                .setData(variationPage.getContent())
                .setPaginationInfo(variationPage)
                .build();
    }

    @Override
    public Variation getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public Variation create(VariationRequest variation) {
        Variation variation2 = new Variation();
        variation2.setVariName(variation.getVariName());
        return repository.save(variation2);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public Variation update(Long id, Variation variation) {
        if (repository.existsById(id)) {
            variation.setVariId(id);
            return repository.save(variation);
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