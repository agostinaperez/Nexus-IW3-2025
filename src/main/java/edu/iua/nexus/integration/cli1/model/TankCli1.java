package edu.iua.nexus.integration.cli1.model;

import edu.iua.nexus.model.Tank;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cli1_tanks")
@PrimaryKeyJoinColumn(name = "id_tank")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class TankCli1 extends Tank {

    @Column(nullable = false, unique = true)
    private String idCli1;

    private boolean codCli1Temp=false;
}