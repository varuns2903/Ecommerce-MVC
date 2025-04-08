package com.ecommerce.app.controller;

import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.service.CategoryService;
import com.ecommerce.app.service.OrderService;
import com.ecommerce.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;

    // Dashboard
    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("orders", orderService.getOrdersByUser(null));
        return "admin/dashboard";
    }

    // List all products
    @GetMapping("/products")
    public String getProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products/list";
    }

    // Show product form for new product
    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/products/form";
    }

    // Show product form for editing existing product
    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable String id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/products/form";
    }

    // Save or update product
    @PostMapping("/products")
    public String saveProduct(@ModelAttribute("product") Product product) {
        if (product.getId() == null ) {
            productService.createProduct(product);
        } else {
            productService.updateProduct(product.getId(), product);
        }
        return "redirect:/admin/products";
    }

    // Delete product
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }







    // Categories
    @GetMapping("/categories")
    public String getCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories/list";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable String id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/categories/form";
    }

    @PostMapping("/categories")
    public String saveCategory(@ModelAttribute("category") Category category) {
        if (category.getId() == null || category.getId().trim().isEmpty()) {
            categoryService.createCategory(category);
        } else {
            categoryService.updateCategory(category.getId(), category);
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }









    // Orders
    @GetMapping("/orders")
    public String getOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/orders/list";
    }

    @GetMapping("/orders/{id}")
    public String editOrder(@PathVariable String id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("statuses", Order.OrderStatus.values());
        return "admin/orders/form";
    }

    @PostMapping("/orders")
    public String updateOrder(@ModelAttribute Order order) {
        orderService.updateOrder(order.getId(), order);
        return "redirect:/admin/orders";
    }

    @GetMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable String id) {
        orderService.deleteById(id);
        return "redirect:/admin/orders";
    }
}