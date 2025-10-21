package edu.iua.nexus.integration.cli1.model.repository;

import edu.iua.nexus.integration.cli1.model.ClientCli1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientCli1Repository extends JpaRepository<ClientCli1, Long> {

    Optional<ClientCli1> findOneByIdCli1(String idCli1);

    Optional<ClientCli1> findByNameAndIdCli1NotAndCodCli1Temp(String name, String idCli1, boolean codCli1Temp);

    Optional<ClientCli1> findByName(String name);

    @Modifying
    @Query(value = "INSERT INTO cli1_clients (id_client, id_cli1, cod_cli1temp) VALUES (:idClient, :idCli1, false)", nativeQuery = true)
    void insertClientCli1(@Param("idClient") Long idClient, @Param("idCli1") String idCli1);
}
