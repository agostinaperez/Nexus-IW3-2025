package edu.iua.nexus.integration.cli1.model;

import edu.iua.nexus.model.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cli1_orders")
@PrimaryKeyJoinColumn(name = "id_order")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderCli1 extends Order {

    @Column(nullable = false, unique = true)
    private String orderNumberCli1; //código externo único

    private boolean codCli1Temp=false; // esto está en caso de que me pasen sin código externo. Le voy a generar uno temporal
    //igual x ahora asumo q siempre me viene el num de order, no voy a usar esta validacion x ahora

}
