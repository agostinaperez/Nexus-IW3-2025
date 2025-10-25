package edu.iua.nexus.websockets;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
public class DetailsWrapper {

    private long id;

    private Date timeStamp;

    /**
     * Masa acumulada medida por la planta al momento del evento.
     */
    private float accumulatedMass;

    /**
     * Densidad instantánea del combustible informada por el dispositivo de campo.
     */
    private float density;

    private float temperature;

    /**
     * Caudal (flow rate) instantáneo, usado para mostrar velocidad de carga.
     */
    private float flowRate;

}
