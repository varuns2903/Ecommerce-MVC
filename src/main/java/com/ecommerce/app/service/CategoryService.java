package com.ecommerce.app.service;

import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.repository.CategoryRepository;
import com.ecommerce.app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
    }

    public Category createCategory(Category category) {
        category.setSlug(generateSlug(category.getName()));
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public Category updateCategory(String id, Category updatedCategory) {
        Category category = getCategoryById(id);
        category.setName(updatedCategory.getName());
        category.setSlug(generateSlug(updatedCategory.getName())); // Generate slug from name
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public void deleteCategory(String id) {
        List<Product> products = productRepository.findByCategoryId(id);

        Category uncategorizedCategory = categoryRepository.findBySlug("uncategorized").get();

        if (uncategorizedCategory == null) {
            uncategorizedCategory = new Category("Uncategorized", "uncategorized");
            uncategorizedCategory = categoryRepository.save(uncategorizedCategory);
        }

        for (Product product : products) {
            product.setCategoryId(uncategorizedCategory.getId());
        }

        productRepository.saveAll(products);

        categoryRepository.deleteById(id);
    }


    private String generateSlug(String name) {
        if (name == null) return "";
        return name.toLowerCase().replaceAll("\\s+", "-");
    }
}
