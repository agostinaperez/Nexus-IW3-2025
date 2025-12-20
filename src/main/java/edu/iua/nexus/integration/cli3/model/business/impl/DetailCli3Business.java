package edu.iua.nexus.integration.cli3.model.business.impl;

import edu.iua.nexus.integration.cli3.model.business.interfaces.IDetailCli3Business;
import edu.iua.nexus.model.Detail;
import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;
import edu.iua.nexus.model.business.interfaces.IDetailBusiness;
import edu.iua.nexus.model.business.interfaces.IOrderBusiness;
import edu.iua.nexus.model.repository.DetailRepository;
import edu.iua.nexus.websockets.wrappers.DetailWsWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/** Servicio que recibe los detalles reportados por el CLI3 y decide, según la frecuencia mínima, si se guardan. */
@Service
public class DetailCli3Business implements IDetailCli3Business {

    @Autowired
    private IOrderBusiness orderBusiness;

    @Autowired
    private IDetailBusiness detailBusiness;

    @Autowired
    private DetailRepository detailDAO;

    @Autowired
    private SimpMessagingTemplate wSock;

    /** Persiste un nuevo detalle recibido desde el CLI3 y actualiza la cabecera de la orden. */
    @Override
    public void add(Detail detail) throws FoundException, BusinessException, NotFoundException {
        long currentTime = System.currentTimeMillis();
        Order orderFound = orderBusiness.load(detail.getOrder().getId());
        Optional<List<Detail>> detailsOptional = detailDAO.findByOrderId(detail.getOrder().getId());

        DetailWsWrapper detailWsWrapper = new DetailWsWrapper();
        detailWsWrapper.setTimeStamp(new Date(currentTime));
        detailWsWrapper.setAccumulatedMass(detail.getAccumulatedMass());
        detailWsWrapper.setDensity(detail.getDensity());
        detailWsWrapper.setTemperature(detail.getTemperature());
        detailWsWrapper.setFlowRate(detail.getFlowRate());

        if ((detailsOptional.isPresent() && !detailsOptional.get().isEmpty())) {
            Date lastTimeStamp = orderFound.getFuelingEndDate();
            if (checkFrequency(currentTime, lastTimeStamp)) {
                // Solo persisto el detail si pasó el intervalo mínimo configurado en application.properties
                detail.setTimeStamp(new Date(currentTime));
                Detail savedDetail = detailBusiness.add(detail);
                orderFound.setFuelingEndDate(new Date(currentTime));
                orderBusiness.update(orderFound);

                detailWsWrapper.setId(savedDetail.getId());
                // Envío de detalle de carga a clientes (WebSocket)
                wSock.convertAndSend("/topic/details/order/" + detail.getOrder().getId(), detailWsWrapper);
            }
        } else {
            // Primer detalle: inicio y guardo
            detail.setTimeStamp(new Date(currentTime));
            detailBusiness.add(detail);
            orderFound.setFuelingStartDate(new Date(currentTime));
            orderFound.setFuelingEndDate(new Date(currentTime));
            orderBusiness.update(orderFound);
            // Envío de detalle de carga a clientes (WebSocket)
            wSock.convertAndSend("/topic/details/order/" + detail.getOrder().getId(), detailWsWrapper);
        }
    }

    @Value("${loading.details.saving.frequency}")
    private long SAVE_INTERVAL_MS;

    /**
     * Evalúa si transcurrió el intervalo mínimo entre dos registros consecutivos.
     *
     * @param currentTime   marca temporal del nuevo detalle
     * @param lastTimeStamp fecha del último detalle persistido
     * @return true si se debe guardar, false si se considera ruido y se ignora
     */
    private boolean checkFrequency(long currentTime, Date lastTimeStamp) {
        return currentTime - lastTimeStamp.getTime() >= SAVE_INTERVAL_MS;
    }
}
