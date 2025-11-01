package edu.iua.nexus.integration.cli2.model.business.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import edu.iua.nexus.integration.cli2.model.business.interfaces.IOrderCli2Business;
import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.Product;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;
import edu.iua.nexus.model.business.impl.DetailBusiness;
import edu.iua.nexus.model.business.interfaces.IOrderBusiness;
import edu.iua.nexus.model.repository.OrderRepository;
import edu.iua.nexus.util.ActivationPasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class OrderCli2Business implements IOrderCli2Business {

    @Autowired
    private OrderRepository orderDAO;

    @Autowired
    private IOrderBusiness orderBusiness;

    @Autowired
    private DetailBusiness detailBusiness;

    private byte[] buildConciliationResponse(Order order, float finalWeight) throws BusinessException {
    float initialWeighing = order.getInitialWeighing();
    float productLoaded = order.getLastAccumulatedMass();
    float netWeight = finalWeight - initialWeighing;
    float difference = netWeight - productLoaded;
    float avgTemperature = detailBusiness.calculateAverageTemperature(order.getId());
    float avgDensity = detailBusiness.calculateAverageDensity(order.getId());
    float avgFlow = detailBusiness.calculateAverageFlowRate(order.getId());
    Product product = order.getProduct();

    Map<String, Object> conciliacion = new LinkedHashMap<>();
    conciliacion.put("pesajeInicial", initialWeighing);
    conciliacion.put("pesajeFinal", finalWeight);
    conciliacion.put("productoCargado", productLoaded);
    conciliacion.put("netoPorBalanza", netWeight);
    conciliacion.put("diferenciaBalanzaCaudalimetro", difference);
    conciliacion.put("promedioTemperatura", avgTemperature);
    conciliacion.put("promedioDensidad", avgDensity);
    conciliacion.put("promedioCaudal", avgFlow);
    conciliacion.put("producto", product);

    Map<String, Object> respuesta = new LinkedHashMap<>();
    respuesta.put("mensaje", "Conciliacion de pesajes:");
    respuesta.put("conciliacion", conciliacion);

    try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(respuesta);
    } catch (Exception e) {
        throw new BusinessException("Error al generar conciliación JSON", e);
    }
}


    @Override
    public Order registerInitialWeighing(String licensePlate, float initialWeight) throws BusinessException, NotFoundException, FoundException {
        Optional<Order> orderFound;

        try {
            orderFound = orderDAO.findByTruck_LicensePlateAndStatus(licensePlate, Order.Status.RECEIVED);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if (orderFound.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra orden para cargar en camion con patente " + licensePlate).build();
        }

        int password;
        do {
            password = Integer.parseInt(ActivationPasswordGenerator.generateActivationPassword());
        } while (orderDAO.findByActivatePassword(password).isPresent());

        orderFound.get().setActivatePassword(password);
        orderFound.get().setInitialWeighing(initialWeight);
        orderFound.get().setInitialWeighingDate(new Date(System.currentTimeMillis()));
        orderFound.get().setStatus(Order.Status.REGISTERED_INITIAL_WEIGHING);
        orderBusiness.update(orderFound.get());
        return orderFound.get();
    }

    // ENDPONIT 5.0
    @Override
    public byte[] registerFinalWeighing(String licensePlate, float finalWeight)
            throws BusinessException, NotFoundException, FoundException {

        Optional<Order> orderFound;
        try {
            orderFound = orderDAO.findByTruck_LicensePlateAndStatus(licensePlate, Order.Status.CLOSED);
        } catch (Exception e) {
            log.error("Error buscando orden: {}", e.getMessage(), e);
            throw BusinessException.builder().message("Error al buscar orden").ex(e).build();
        }

        if (orderFound.isEmpty()) {
            throw NotFoundException.builder()
                    .message("No se encuentra orden para camion con patente " + licensePlate + " o la misma aun no fue cerrada")
                    .build();
        }

        Order order = orderFound.get();

        if (finalWeight <= order.getInitialWeighing()) {
            throw BusinessException.builder()
                    .message("El pesaje final no puede ser menor o igual al pesaje inicial.")
                    .build();
        }

        order.setFinalWeighing(finalWeight);
        order.setFinalWeighingDate(new Date());
        order.setStatus(Order.Status.REGISTERED_FINAL_WEIGHING);
        orderBusiness.update(order);

        return buildConciliationResponse(order, finalWeight);
    }

    // ENDPONIT 5.1
    @Override
    public byte[] getConciliation(String licensePlate)
            throws BusinessException, NotFoundException {

    Optional<Order> orderFound;
    try {
        orderFound = orderDAO.findByTruck_LicensePlateAndStatus(
                licensePlate, Order.Status.REGISTERED_FINAL_WEIGHING);
    } catch (Exception e) {
        log.error("Error buscando orden: {}", e.getMessage(), e);
        throw BusinessException.builder()
                .message("Error al buscar orden para conciliación")
                .ex(e)
                .build();
    }

    if (orderFound.isEmpty()) {
        throw NotFoundException.builder()
                .message("No se encuentra orden en estado 4 para la patente " + licensePlate + " o la misma aun no fue cerrada")
                .build();
    }

    Order order = orderFound.get();

    return buildConciliationResponse(order, order.getFinalWeighing());
    }

}

