package com.mercadolibre.infrastructure.adapter.output.persistence.repository;

import com.mercadolibre.infrastructure.adapter.output.persistence.entity.ProductEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByIdIn(List<Long> ids);
}
