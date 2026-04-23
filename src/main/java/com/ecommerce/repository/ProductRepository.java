package com.ecommerce.repository;

import com.ecommerce.entity.Merchant;
import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByApprovedTrue(Pageable pageable);

    Page<Product> findByPriceBetween(double v, double v1, Pageable pageable);

    Page<Product> findByNameAndCategory(String name, String category, Pageable pageable);

    Page<Product> findByName(String name, Pageable pageable);

    Page<Product> findByCategory(String category, Pageable pageable);

    List<Product> findByMerchant(Merchant merchant);

    Optional<Product> findByIdAndMerchant(Long id, Merchant merchant);
}
