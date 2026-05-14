package com.re.repository;

import com.re.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.price = p.price - (p.price * :discount / 100) WHERE p.category = :category AND p.status = 'ACTIVE'")
    int updatePriceByCategory(@Param("category") String category, @Param("discount") BigDecimal discount);
}
