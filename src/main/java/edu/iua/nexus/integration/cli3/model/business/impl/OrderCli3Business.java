package edu.iua.nexus.integration.cli3.model.business.impl;

import edu.iua.nexus.events.DetailEvent;
import edu.iua.nexus.integration.cli3.model.business.interfaces.IOrderCli3Business;
import edu.iua.nexus.model.Detail;
import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.ConflictException;
import edu.iua.nexus.model.business.NotFoundException;
import edu.iua.nexus.model.business.impl.OrderBusiness;
import edu.iua.nexus.model.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class OrderCli3Business implements IOrderCli3Business {

    @Autowired
    private OrderRepository orderDAO;

    @Autowired
    private OrderBusiness orderBusiness;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Order validatePassword(int password) throws NotFoundException, BusinessException, ConflictException {
        Optional<Order> order;

        try {
            order = orderDAO.findByActivatePassword(password);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("Error al recuperar orden", e);
        }

        if (order.isEmpty()) {
            throw new NotFoundException("Orden no econtrada");
        }
        checkOrderStatus(order.get());
        return order.get();
    }

    @Override
    public Order receiveDetails(Detail detail) throws NotFoundException, BusinessException, ConflictException {
        Order orderFound = orderBusiness.load(detail.getOrder().getId());
//validaciones
        if (orderFound.getStatus() != Order.Status.REGISTERED_INITIAL_WEIGHING) {
            throw new ConflictException("Estado de orden no válido");
        }
        if (detail.getFlowRate() < 0) {
            throw new BusinessException("Caudal no válido");
        }
        if (detail.getAccumulatedMass() < orderFound.getLastAccumulatedMass()) {
            throw new BusinessException("Masa acumulada no válida");
        }

        Date currentTime = new Date(System.currentTimeMillis());
        // Actualizacion de cabecera de la orden
        orderFound.setLastTimeStamp(currentTime);
        orderFound.setLastAccumulatedMass(detail.getAccumulatedMass());
        orderFound.setLastDensity(detail.getDensity());
        orderFound.setLastTemperature(detail.getTemperature());
        orderFound.setLastFlowRate(detail.getFlowRate());
        orderDAO.save(orderFound);

        // Evento para manejar el almacenamiento de detalle
        applicationEventPublisher.publishEvent(new DetailEvent(detail, DetailEvent.TypeEvent.SAVE_DETAIL));

        return orderFound;
    }

    
    private void checkOrderStatus(Order order) throws ConflictException {
        if (order.getStatus() != Order.Status.REGISTERED_INITIAL_WEIGHING) {
            throw new ConflictException("Estado de orden no válido");
        }
    }

    //Punto 4
    @Override
    @Transactional
    public Order closeOrder(Long orderId) throws BusinessException, NotFoundException, ConflictException {
        // 1. Cargar la orden usando el business base
        Order order = orderBusiness.load(orderId); // Throws NotFoundException, BusinessException

        // 2. Validar el estado actual
        if (order.getStatus() != Order.Status.REGISTERED_INITIAL_WEIGHING) {
            throw new ConflictException("La orden no se puede cerrar porque no está en estado 'REGISTERED_INITIAL_WEIGHING'. Estado actual: " + order.getStatus());
        }

        // 3. Cambiar el estado
        order.setStatus(Order.Status.CLOSED);

        // 4. Opcional: Anular contraseña de activación
        order.setActivatePassword(null);

        // 5. Guardar la orden actualizada usando el business base
        return orderBusiness.update(order); // Throws NotFoundException, BusinessException
    }
}
