package edu.iua.nexus.model.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.iua.nexus.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProduct(String product);

    Optional<Product> findByProductAndIdNot(String product, Long id);

}
