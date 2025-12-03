package edu.iua.nexus.websockets.wrappers;

import edu.iua.nexus.model.Alarm;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AlarmWsWrapper {

    private long id;
    private long orderId;
    private Alarm.Status status;
    private float temperature;
    private String observation;
    private Date timeStamp;
    private String user;
    private float thresholdTemperature;

}
