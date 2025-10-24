package edu.iua.nexus.integration.cli1.model;
import edu.iua.nexus.model.Client;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cli1_clients")
@PrimaryKeyJoinColumn(name = "id_client")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class ClientCli1 extends Client {

    @Column(nullable = false, unique = true)
    private String idCli1;
}