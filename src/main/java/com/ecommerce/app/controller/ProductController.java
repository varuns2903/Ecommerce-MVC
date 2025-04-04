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
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/product/{id}")
    public String products(@PathVariable String id, Model model) {
        List<Category> categories = categoryService.getAllCategories();
        Product product = productService.getProductById(id);

        List<Product> similarProducts = productService.getSimilarProducts(product.getCategoryId(), id);

        model.addAttribute("categories", categories);
        model.addAttribute("product", product);
        model.addAttribute("similarProduct", similarProducts);

        return "product";
    }
}
