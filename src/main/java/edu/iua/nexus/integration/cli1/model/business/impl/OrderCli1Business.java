package edu.iua.nexus.integration.cli1.model.business.impl;

import java.util.List;
import java.util.Optional;
import edu.iua.nexus.integration.cli1.model.business.interfaces.*;
import edu.iua.nexus.integration.cli1.model.repository.OrderCli1Repository;
import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.business.BadRequestException;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;
import edu.iua.nexus.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.iua.nexus.integration.cli1.model.OrderCli1;
import edu.iua.nexus.integration.cli1.model.deserializers.OrderCli1JsonDeserializer;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class OrderCli1Business implements IOrderCli1Business {

    @Autowired
    private OrderCli1Repository orderDAO;

    @Autowired
    private IClientCli1Business clientBusiness;

    @Autowired
    private ITruckCli1Business truckBusiness;

    @Autowired
    private IProductCli1Business productBusiness;

    @Autowired
    private IDriverCli1Business driverBusiness;

    @Override
    public OrderCli1 load(String orderNumberCli1) throws NotFoundException, BusinessException {
        Optional<OrderCli1> r;
        try {
            r = orderDAO.findOneByOrderNumberCli1(orderNumberCli1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if (r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra la orden orderNumberCli1=" + orderNumberCli1).build();
        }
        return r.get();
    }

    @Override
    public List<OrderCli1> list() throws BusinessException {
        try {
            return orderDAO.findAll();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public OrderCli1 add(OrderCli1 order) throws FoundException, BusinessException {
        Optional<OrderCli1> orderFound;

        orderFound = orderDAO.findOneByOrderNumberCli1(order.getOrderNumberCli1());
        if (orderFound.isPresent()) {
            throw FoundException.builder().message("Ya existe una orden con el número " + order.getOrderNumberCli1()).build();
        }
        orderFound = orderDAO.findByTruck_idAndStatus(order.getTruck().getId(), Order.Status.RECEIVED);
        if (orderFound.isPresent()) {
            throw FoundException.builder().message("Ya existe una orden para el camion id=" + order.getTruck().getId()).build();
        }

        try {
            return orderDAO.save(order);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public OrderCli1 addExternal(String json) throws FoundException, BusinessException, BadRequestException {
        ObjectMapper mapper = JsonUtils.getObjectMapper(OrderCli1.class, new OrderCli1JsonDeserializer(
                OrderCli1.class, driverBusiness, truckBusiness, clientBusiness, productBusiness), null);
        OrderCli1 order;

        try {
            order = mapper.readValue(json, OrderCli1.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().message("El formato JSON es incorrecto").build();
        }

        return add(order);
    }

    @Override
    public OrderCli1 cancelExternal(String orderNumberCli1) throws BusinessException {
        Optional<OrderCli1> orderFound = orderDAO.findOneByOrderNumberCli1(orderNumberCli1);
        if (orderFound.isPresent() && orderFound.get().getStatus().equals(Order.Status.RECEIVED)) {
            orderFound.get().setStatus(Order.Status.CANCELLED);
            orderDAO.save(orderFound.get());
            return orderFound.get();
        } else if (orderFound.isEmpty()) {
            throw new BusinessException("No se encuentra la orden orderNumberCli1=" + orderNumberCli1);
        }
        throw new BusinessException("No se puede cancelar la orden, estado actual: " + orderFound.get().getStatus());
    }

}
