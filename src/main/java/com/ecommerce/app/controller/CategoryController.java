package com.ecommerce.app.controller;

import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.service.CategoryService;
import com.ecommerce.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping("/category/{id}")
    public String getProductsByCategory(@PathVariable String id, Model model) {
        Category category = categoryService.getCategoryById(id);
        List<Product> productsByCategory = productService.getProductsByCategory(id);

        model.addAttribute("category", category);
        model.addAttribute("productsByCategory", productsByCategory);
        return "category";
    }
}
