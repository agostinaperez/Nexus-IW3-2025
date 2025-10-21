package edu.iua.nexus.integration.cli1.model.repository;

import edu.iua.nexus.integration.cli1.model.TankCli1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TankCli1Repository extends JpaRepository<TankCli1, Long> {

    Optional<TankCli1> findOneByIdCli1(String idCli1);

    @Modifying
    @Query(value = "INSERT INTO cli1_tanks (id_tank, id_cli1, cod_cli1temp) VALUES (:idTank, :idCli1, false)", nativeQuery = true)
    void insertTankCli1(@Param("idTank") Long idTank, @Param("idCli1") String idCli1);
}
