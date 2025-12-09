package edu.iua.nexus.model.business.impl;

import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.Product;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.NotFoundException;
import edu.iua.nexus.model.business.interfaces.IOrderBusiness;
import edu.iua.nexus.model.repository.OrderRepository;
import edu.iua.nexus.util.PdfGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class OrderBusiness implements IOrderBusiness {

    @Autowired
    private OrderRepository orderDAO;

    @Autowired
    DetailBusiness detailBusiness;

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

    @Override
    public byte[] getConciliationPdf(Long idOrder) throws BusinessException, NotFoundException {
        Optional<Order> orderFound;
        try {
            orderFound = orderDAO.findByIdAndStatus(idOrder, Order.Status.REGISTERED_FINAL_WEIGHING);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().message("").build();
        }
        if (orderFound.isEmpty())
            throw NotFoundException.builder().message("No se encuentra la Orden id= " + idOrder).build();

        float initialWeighing = orderFound.get().getInitialWeighing();
        float productLoaded = orderFound.get().getLastAccumulatedMass();
        float finalWeight = orderFound.get().getFinalWeighing();
        float netWeight = finalWeight - initialWeighing;
        float difference = netWeight - productLoaded;
        float avgTemperature = detailBusiness.calculateAverageTemperature(orderFound.get().getId());
        float avgDensity = detailBusiness.calculateAverageDensity(orderFound.get().getId());
        float avgFlow = detailBusiness.calculateAverageFlowRate(orderFound.get().getId());
        Product product = orderFound.get().getProduct();

        try {
            return PdfGenerator.generateFuelLoadingReconciliationReport(initialWeighing, finalWeight, productLoaded, netWeight, difference, avgTemperature, avgDensity, avgFlow, product);
        } catch (DocumentException | IOException e) {
            log.error("Error generando el PDF: {}", e.getMessage(), e);
            throw BusinessException.builder().message("Error al generar el reporte PDF").ex(e).build();
        }
    }

     @Override
    public Map<String, Object> getConciliationJson(Long idOrder) throws BusinessException, NotFoundException{
        Optional<Order> orderFound;
        try {
            orderFound = orderDAO.findByIdAndStatus(idOrder, Order.Status.REGISTERED_FINAL_WEIGHING);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().message("").build();
        }
        if (orderFound.isEmpty())
            throw NotFoundException.builder().message("No se encuentra la Orden id= " + idOrder).build();

        float initialWeighing = orderFound.get().getInitialWeighing();
        float productLoaded = orderFound.get().getLastAccumulatedMass();
        float finalWeight = orderFound.get().getFinalWeighing();
        float netWeight = finalWeight - initialWeighing;
        float difference = netWeight - productLoaded;
        float avgTemperature = detailBusiness.calculateAverageTemperature(orderFound.get().getId());
        float avgDensity = detailBusiness.calculateAverageDensity(orderFound.get().getId());
        float avgFlow = detailBusiness.calculateAverageFlowRate(orderFound.get().getId());
        Product product = orderFound.get().getProduct();

        Map<String, Object> conciliationData = new HashMap<>();
        conciliationData.put("initialWeighing", initialWeighing);
        conciliationData.put("finalWeighing", finalWeight);
        conciliationData.put("accumulatedMass", productLoaded);
        conciliationData.put("netWeight", netWeight);
        conciliationData.put("differenceWeight", difference);
        conciliationData.put("averageTemperature", avgTemperature);
        conciliationData.put("averageDensity", avgDensity);
        conciliationData.put("averageFlowRate", avgFlow);
        conciliationData.put("product", product != null ? product.getProduct() : "Unknown");
        return conciliationData;
    }


}
