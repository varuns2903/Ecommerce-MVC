package com.ecommerce.app.controller;

import com.ecommerce.app.model.Product;
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

    @GetMapping("/product/{id}")
    public String products(@PathVariable String id, Model model) {
        Product product = productService.getProductById(id);

        List<Product> similarProducts = productService.getSimilarProducts(product.getCategoryId(), id);

        model.addAttribute("product", product);
        model.addAttribute("similarProduct", similarProducts);

        System.out.println("Model: " + model.asMap());
        return "product";
    }
}
