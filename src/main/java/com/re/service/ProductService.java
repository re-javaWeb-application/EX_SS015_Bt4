package com.re.service;

import com.re.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public void updateBulkPrice(String categoryName, BigDecimal discountPercentage) {
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) < 0 || discountPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Phần trăm giảm giá không hợp lệ");
        }

        int updatedCount = productRepository.updatePriceByCategory(categoryName, discountPercentage);

        if (updatedCount == 0) {
            throw new RuntimeException("Không tìm thấy sản phẩm nào để cập nhật");
        }
    }
}
