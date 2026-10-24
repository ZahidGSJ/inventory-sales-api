package com.zahid.inventoryapi.category;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateCategoryNameException("Ya existe una categoria con el nombre: " + request.name());
        }

        Category category = new Category();

        category.setName(request.name());
        category.setDescription(request.description());

        Category savedCategory = categoryRepository.save(category);

        return CategoryResponse.from(savedCategory);
    }
    
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll(Sort.by("name"))
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        return CategoryResponse.from(findCategory(id));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategory(id);

        if (categoryRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateCategoryNameException(
                    "Ya existe una categoria con el nombre: " + request.name());
        }

        category.setName(request.name());
        category.setDescription(request.description());

        return CategoryResponse.from(categoryRepository.save(category));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Categoría no encontrada con id: " + id));
    }
}