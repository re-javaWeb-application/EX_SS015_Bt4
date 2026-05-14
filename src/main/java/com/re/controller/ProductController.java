package com.re.controller;

import com.re.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/")
    public String showForm() {
        return "update-price";
    }

    @PostMapping("/update-price")
    public String updatePrice(@RequestParam String categoryName, 
                              @RequestParam BigDecimal discountPercentage, 
                              Model model) {
        try {
            productService.updateBulkPrice(categoryName, discountPercentage);
            model.addAttribute("success", "Cập nhật thành công!");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "update-price";
    }
}
