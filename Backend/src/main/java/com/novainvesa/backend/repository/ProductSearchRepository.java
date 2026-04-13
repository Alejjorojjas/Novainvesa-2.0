package com.novainvesa.backend.repository;

import com.novainvesa.backend.entity.ProductSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends JpaRepository<ProductSearch, Long> {

    List<ProductSearch> findTop10ByOrderByCreatedAtDesc();
}
