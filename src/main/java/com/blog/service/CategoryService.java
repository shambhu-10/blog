package com.blog.service;

import com.blog.dtos.CategoryRequest;
import com.blog.dtos.CategoryResponse;
import com.blog.entity.Category;
import com.blog.exception.DuplicateResourceException;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /** to create a new category
     *
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.debug("Creating category with name: {}", request.getName());

        // check if category with this name already exists
        if (categoryRepository.existsByName(request.getName())) {
            log.warn("category with name '{}' already exists", request.getName());
            throw DuplicateResourceException.forField("Category", "name", request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription((request.getDescription()));

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", savedCategory.getId());

        return convertToResponse(savedCategory);
    }

    /**
     * to get all category
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.debug("Fetching all categories");

        List<Category> categories = categoryRepository.findAll();
        log.info("Found {} categories", categories.size());

        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get category by Id
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        log.debug("Fetching category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Category not found with id: {}", id);
                    return ResourceNotFoundException.forId("Category", id);
                });
        return convertToResponse(category);
    }

    /**
     * update category
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.debug("Updating category with id {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Category", id));

        // checking if new name conflicts with existing category (excluding current one)
        categoryRepository.findByName(request.getName())
                .ifPresent(existingCategory -> {
                    if(!existingCategory.getId().equals(id)) {
                        log.warn("Cannot update: Category name '{}' already exists", request.getName());
                        throw DuplicateResourceException.forField("Category", "name", request.getName());
                    }
                });

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with id: {}", id);

        return convertToResponse(updatedCategory);

    }

    /**
     * Delete category
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.debug("Deleting category with id: {}", id);

        if (!categoryRepository.existsById(id)) {
            log.error("Cannot delete: Category not found with id: {}", id);
            throw ResourceNotFoundException.forId("Category", id);
        }

        categoryRepository.deleteById(id);
        log.info("Category deleted successfully with id: {}", id);
    }


    // helper method to convert the entity into the dto
    private CategoryResponse convertToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

}
