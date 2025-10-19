package edu.iua.nexus.model.business.impl;

import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.NotFoundException;
import edu.iua.nexus.model.business.interfaces.IOrderBusiness;
import edu.iua.nexus.model.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class OrderBusiness implements IOrderBusiness {

    @Autowired
    private OrderRepository orderDAO;

    @Override
    public Page<Order> list(Pageable pageable, List<String> statuses) throws BusinessException {
        try {
            return statuses == null || statuses.isEmpty()
                    ? orderDAO.findAll(pageable) // Si no hay filtro, devuelve todo
                    : orderDAO.findByStatuses(statuses, pageable);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }


    @Override
    public Order load(long id) throws NotFoundException, BusinessException {
        Optional<Order> orderFound;
        try {
            orderFound = orderDAO.findById(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if (orderFound.isEmpty())
            throw NotFoundException.builder().message("No se encuentra la Orden id= " + id).build();
        return orderFound.get();
    }

    @Override
    public Order update(Order order) throws NotFoundException, BusinessException {
        load(order.getId());
        try {
            return orderDAO.save(order);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //throw BusinessException.builder().ex(e).build();
            throw BusinessException.builder().message("Error al Actualizar Orden").build();
        }
    }

}
