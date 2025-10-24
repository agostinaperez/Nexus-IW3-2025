package edu.iua.nexus.integration.cli1.model.deserializers;

import java.io.Serial;
import java.util.Date;

import edu.iua.nexus.integration.cli1.model.OrderCli1;
import edu.iua.nexus.integration.cli1.model.business.interfaces.IClientCli1Business;
import edu.iua.nexus.integration.cli1.model.business.interfaces.IDriverCli1Business;
import edu.iua.nexus.integration.cli1.model.business.interfaces.IProductCli1Business;
import edu.iua.nexus.integration.cli1.model.business.interfaces.ITruckCli1Business;
import edu.iua.nexus.integration.cli1.util.JsonUtilsCli1;
import edu.iua.nexus.model.*;
import edu.iua.nexus.model.business.BadRequestException;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.util.JsonUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static edu.iua.nexus.integration.cli1.util.JsonAttributeConstants.*;

public class OrderCli1JsonDeserializer extends StdDeserializer<OrderCli1> {

    @Serial
    private static final long serialVersionUID = -3881285352118964728L;
    private static final Logger log = LoggerFactory.getLogger(OrderCli1JsonDeserializer.class);

    protected OrderCli1JsonDeserializer(Class<?> vc) {
        super(vc);
    }

    private IClientCli1Business clientBusiness;
    private IProductCli1Business productBusiness;
    private IDriverCli1Business driverBusiness;
    private ITruckCli1Business truckBusiness;

    public OrderCli1JsonDeserializer(Class<?> vc, IDriverCli1Business driverBusiness, ITruckCli1Business truckBusiness,
                                     IClientCli1Business clientBusiness, IProductCli1Business productBusiness) {
        super(vc);
        this.driverBusiness = driverBusiness;
        this.truckBusiness = truckBusiness;
        this.clientBusiness = clientBusiness;
        this.productBusiness = productBusiness;
    }

    @SneakyThrows
    @Override
    public OrderCli1 deserialize(JsonParser jp, DeserializationContext ctxt) {

        OrderCli1 r = new OrderCli1();
        JsonNode node = jp.getCodec().readTree(jp);

        try {
            int preset = (int) JsonUtils.getValue(node, ORDER_PRESET_ATTRIBUTES, 0);
            if (preset < 0) {
                throw new BadRequestException("Preset falta o no es válido");
            }
            String orderNumber = JsonUtils.getString(node, ORDER_NUMBER_ATTRIBUTES, "");
            if (orderNumber == null || orderNumber.isEmpty()) {
                throw new BadRequestException("Número de orden falta o es inválido");
            }
            Date estimatedTime = JsonUtils.getDate(node, ORDER_ESTIMATED_DATE_ATTRIBUTES, String.valueOf(new Date()));
            if (estimatedTime == null) {
                throw new BadRequestException("Fecha estimada falta o es inválida");
            }

            Driver driver = JsonUtilsCli1.getDriver(node, DRIVER_DOCUMENT_ATTRIBUTES, driverBusiness);
            Truck truck = JsonUtilsCli1.getTruck(node, TRUCK_LICENSE_PLATE_ATTRIBUTES, truckBusiness);
            Client client = JsonUtilsCli1.getClient(node, CLIENT_NAME_ATTRIBUTES, clientBusiness);
            Product product = JsonUtilsCli1.getProduct(node, PRODUCT_NAME_ATTRIBUTES, productBusiness);

            r.setOrderNumberCli1(orderNumber);
            r.setEstimatedDate(estimatedTime);
            r.setExternalReceptionDate(new Date(System.currentTimeMillis()));
            r.setPreset(preset);

            if (product != null && client != null && truck != null && driver != null) {
                r.setClient(client);
                r.setDriver(driver);
                r.setProduct(product);
                r.setTruck(truck);
            }

            r.setStatus(Order.Status.RECEIVED);
            return r;
        } catch (BadRequestException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }
}