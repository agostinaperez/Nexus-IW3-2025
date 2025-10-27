package edu.iua.nexus.integration.cli1.model.repository;

import edu.iua.nexus.integration.cli1.model.ProductCli1;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductCli1Repository extends JpaRepository<ProductCli1, Long> {

    Optional<ProductCli1> findOneByIdCli1(String idCli1);

    Optional<ProductCli1> findProductCli1ByProduct(String product);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO cli1_products (id_product, id_cli1) VALUES (:idProduct, :idCli1) " +
            "ON DUPLICATE KEY UPDATE id_product = id_product", nativeQuery = true)
    void insertProductCli1(@Param("idProduct") Long idProduct, @Param("idCli1") String idCli1);

}
