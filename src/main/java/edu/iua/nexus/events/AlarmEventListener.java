package edu.iua.nexus.events;

import edu.iua.nexus.model.Alarm;
import edu.iua.nexus.model.Detail;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.interfaces.IAlarmBusiness;
import edu.iua.nexus.util.EmailBusiness;
import edu.iua.nexus.websockets.wrappers.AlarmWsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class AlarmEventListener implements ApplicationListener<AlarmEvent> {

    /**
     * Escucha eventos de tipo {@link AlarmEvent} y coordina las acciones que deben dispararse
     * cuando una alarma de temperatura es reportada. El flujo principal crea la alarma en base
     * de datos, notifica a los clientes conectados via WebSocket y envia un correo de alerta al
     * destinatario configurado.
     */
    @Override
    public void onApplicationEvent(AlarmEvent event) {
        if (event.getTypeEvent().equals(AlarmEvent.TypeEvent.TEMPERATURE_EXCEEDED) && event.getSource() instanceof Detail) {
            handleTemperatureExceeded((Detail) event.getSource());
        }
    }

    @Autowired
    private EmailBusiness emailBusiness;

    @Autowired
    private IAlarmBusiness alarmBusiness;

    @Autowired
    private SimpMessagingTemplate wSock;

    @Value("${mail.temperature.exceeded.send.to}")
    private String to;

    /**
     * Procesa el evento de temperatura excedida en tres pasos:
     * <ol>
     *     <li>Persistir la alarma con estado {@code PENDING} asociada a la orden.</li>
     *     <li>Enviar la alarma a clientes suscritos al topic {@code /topic/alarms/order/{orderId}}.</li>
     *     <li>Notificar por correo usando los datos del {@link Detail} que disparo el evento.</li>
     * </ol>
     */
    private void handleTemperatureExceeded(Detail detail) {
        Date now = new Date(System.currentTimeMillis());

        // Guardado de alerta en db
        Alarm alarm = new Alarm();
        alarm.setOrder(detail.getOrder());
        alarm.setTimeStamp(now);
        alarm.setTemperature(detail.getTemperature());
        alarm.setStatus(Alarm.Status.PENDING);

        try {
            alarm = alarmBusiness.add(alarm);
        } catch (BusinessException | FoundException e) {
            log.error(e.getMessage(), e);
        }

        // Envío de alerta a clientes (WebSocket)
        AlarmWsWrapper alarmWsWrapper = new AlarmWsWrapper();
        alarmWsWrapper.setId(alarm.getId());
        alarmWsWrapper.setOrderId(alarm.getOrder().getId());
        alarmWsWrapper.setStatus(alarm.getStatus());
        alarmWsWrapper.setTemperature(alarm.getTemperature());
        alarmWsWrapper.setTimeStamp(alarm.getTimeStamp());
        //alarmWsWrapper.setThresholdTemperature(alarm.getOrder().getProduct().getThresholdTemperature()); //todo tira null pointer ver que onda
        alarmWsWrapper.setObservation(alarm.getObservation() != null ? alarm.getObservation() : null);
        alarmWsWrapper.setUser(
                alarm.getUser() != null && alarm.getUser().getUsername() != null
                        ? alarm.getUser().getUsername()
                        : null
        );

        String topic = "/topic/alarms/order/" + detail.getOrder().getId();
        try {
            wSock.convertAndSend(topic, alarmWsWrapper);
        } catch (Exception e) {
            log.error("Failed to send alert notification", e);
        }

        try {
            final String remindersTopic = "/topic/alarms/reminders";
            log.info("Sending alarm to reminders topic for order {}", alarm.getOrder().getId());
            wSock.convertAndSend(remindersTopic, alarmWsWrapper);
        } catch (Exception e) {
            log.error("Failed to send alarm to reminders topic", e);
        }

        // Armado de mail de alerta
        String subject = "Temperatura Excedida Orden Nro " + detail.getOrder().getId();
        String mensaje = String.format(
                """
                        ALERTA: Temperatura Excedida en la Orden Nro %s

                        Detalles de la Alerta:
                        ---------------------------------
                        Orden ID: %s
                        Fecha/Hora del Evento: %s
                        Temperatura Registrada: %.2f °C
                        Masa Acumulada: %.2f kg
                        Densidad: %.2f kg/m³
                        Caudal: %.2f Kg/h
                        ---------------------------------

                        Descripción: La temperatura del combustible ha superado el umbral establecido. \
                        Por favor, revise esta alerta lo antes posible para evitar inconvenientes.

                        Atentamente,
                        Sistema de Monitoreo de Carga de Combustible Nexus""",
                detail.getOrder().getId(),
                detail.getOrder().getId(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now),
                detail.getTemperature(),
                detail.getAccumulatedMass(),
                detail.getDensity(),
                detail.getFlowRate()
        );

        try {
            emailBusiness.sendSimpleMessage(to, subject, mensaje);
            log.info("Enviando mensaje '{}'", mensaje);
        } catch (BusinessException e) {
            log.error(e.getMessage(), e);
        }
    }
}
