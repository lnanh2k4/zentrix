package com.zentrix.service;

import com.zentrix.model.entity.Category;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.repository.CategoryRepository;
import com.zentrix.repository.ProductRepository;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/*
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date April 07, 2025
 */

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    ProductRepository productRepository;

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    
    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean existsByCateName(String cateName) {
        return categoryRepository.existsByCateName(cateName);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Category getCategoryById(int id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public List<Category> getSubCategories(int parentId) {
        Optional<Category> parent = categoryRepository.findById(parentId);
        return parent.map(categoryRepository::findByParentCateId).orElse(List.of());
    }

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public Category addCategory(Category category) {
        if (category.getParentCateId() != null) {
            Optional<Category> parentCategory = categoryRepository.findById(category.getParentCateId().getCateId());
            if (parentCategory.isEmpty()) {
                return null;
            }
            category.setParentCateId(parentCategory.get());
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public Category updateCategory(Category category) {
        Optional<Category> existingCategory = categoryRepository.findById(category.getCateId());
        if (existingCategory.isEmpty()) {
            return null;
        }

        Category updatedCategory = existingCategory.get();
        updatedCategory.setCateName(category.getCateName());

        if (category.getParentCateId() != null) {
            Optional<Category> parentCategory = categoryRepository.findById(category.getParentCateId().getCateId());
            if (parentCategory.isPresent() && parentCategory.get().getCateId() != category.getCateId()) {
                updatedCategory.setParentCateId(parentCategory.get());
            }
        }

        return categoryRepository.save(updatedCategory);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean deleteCategory(int id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            return false;
        }

        List<Category> subCategories = categoryRepository.findByParentCateId(category.get());
        if (!subCategories.isEmpty()) {
            throw new RuntimeException("Cannot delete category because it has " + subCategories.size() + " subcategories");
        }

        long productCount = productRepository.countByCateId(category.get());
        if (productCount > 0) {
            throw new RuntimeException("Cannot delete category because it is associated with " + productCount + " products");
        }

        categoryRepository.delete(category.get());
        return true;
    }

    
}