package edu.iua.nexus.integration.cli1.util;

import edu.iua.nexus.integration.cli1.model.*;
import edu.iua.nexus.model.Tank;
import edu.iua.nexus.model.business.BadRequestException;
import edu.iua.nexus.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.Set;

import static edu.iua.nexus.integration.cli1.util.JsonAttributeConstants.*;

/**
 * Tiene la lógica para transformar un JsonNode en
 * objetos de transferencia propios del integrador, aplicando validaciones básicas
 * antes de permitir que lleguen a la capa de negocio.
 */
public class BuildEntityUtils {
    //si algo llega a fallar, me tengo q acordar de que estoy asumiendo q siempre voy a haber pasado un codcli1
    public static DriverCli1 buildDriver(JsonNode driverNode) throws BadRequestException {
        DriverCli1 newDriver = new DriverCli1();

        String idCli1 = JsonUtils.getString(driverNode, DRIVER_IDCLI1_ATTRIBUTES, "");
        if (idCli1 != null && !idCli1.isEmpty()) {
            newDriver.setIdCli1(idCli1);
        } else {
            throw new BadRequestException("El identificador del conductor no puede ser nulo o vacío");
        }

        String name = JsonUtils.getString(driverNode, DRIVER_NAME_ATTRIBUTES, "");
        if (name != null && !name.isEmpty()) {
            newDriver.setName(name);
        } else {
            throw new BadRequestException("El nombre del conductor no puede ser nulo o vacío");
        }

        String lastName = JsonUtils.getString(driverNode, DRIVER_LASTNAME_ATTRIBUTES, "");
        if (lastName != null && !lastName.isEmpty()) {
            newDriver.setLastName(lastName);
        } else {
            throw new BadRequestException("El apellido del conductor no puede ser nulo o vacío");
        }

        String identityDocument = JsonUtils.getString(driverNode, DRIVER_DOCUMENT_ATTRIBUTES, "");
        if (identityDocument != null && !identityDocument.isEmpty()) {
            newDriver.setDocument(identityDocument);
        } else {
            throw new BadRequestException("El documento del conductor no puede ser nulo o vacío");
        }
        return newDriver;
    }


    public static TruckCli1 buildTruck(JsonNode truckNode, JsonNode tanksNode) throws BadRequestException {
        TruckCli1 newTruck = new TruckCli1();

        String idCli1 = JsonUtils.getString(truckNode, TRUCK_IDCLI1_ATTRIBUTES, "");
        if (idCli1 != null && !idCli1.isEmpty()) {
            newTruck.setIdCli1(idCli1);
        } else {
            throw new BadRequestException("El identificador del camión no puede ser nulo o vacío");
        }

        String licensePlate = JsonUtils.getString(truckNode, TRUCK_LICENSE_PLATE_ATTRIBUTES, "");
        if (licensePlate != null && !licensePlate.isEmpty()) {
            newTruck.setLicensePlate(licensePlate);
        } else {
            throw new BadRequestException("La placa del camión no puede ser nula o vacía");
        }

        String description = JsonUtils.getString(truckNode, TRUCK_DESCRIPTION_ATTRIBUTES, "");
        if (description != null && !description.isEmpty()) {
            newTruck.setDescription(description);
        }

        Set<Tank> newTanks = new HashSet<>();
        if (tanksNode != null && tanksNode.isArray()) {
            for (JsonNode tankNode : tanksNode) {
                TankCli1 tank = new TankCli1();

                String tankIdCli1 = JsonUtils.getString(tankNode, TANK_IDCLI1_ATTRIBUTES, "");
                if (tankIdCli1 != null && !tankIdCli1.isEmpty()) {
                    tank.setIdCli1(tankIdCli1);
                } else {
                    throw new BadRequestException("El identificador del tanque no puede ser nulo o vacío");
                }

                long capacityLiters = (long) JsonUtils.getValue(tankNode, TANK_CAPACITY_ATTRIBUTES, 0);
                if (capacityLiters > 0) {
                    tank.setCapacity_liters(capacityLiters);
                }

                String license = JsonUtils.getString(tankNode, TANK_LICENSE_ATTRIBUTES, "");
                if (license != null && !license.isEmpty()) {

                    tank.setLicense(license);
                }

                // Agregar al set
                newTanks.add(tank);
            }
        }
        newTruck.setTanks(newTanks);
        return newTruck;
    }

    public static ClientCli1 buildClient(JsonNode clientNode) throws BadRequestException {
        ClientCli1 newClient = new ClientCli1();

        String idCli1 = JsonUtils.getString(clientNode, CLIENT_IDCLI1_ATTRIBUTES, "");
        if (idCli1 != null && !idCli1.isEmpty()) {
            newClient.setIdCli1(idCli1);
        } else {
            throw new BadRequestException("El identificador del cliente no puede ser nulo o vacío");
        }

        String name = JsonUtils.getString(clientNode, CLIENT_NAME_ATTRIBUTES, "");
        if (name != null && !name.isEmpty()) {
            newClient.setName(name);
        } else {
            throw new BadRequestException("El nombre del cliente no puede ser nulo o vacío");
        }

        String email = JsonUtils.getString(clientNode, CLIENT_EMAIL_ATTRIBUTES, "");
        if (email != null && !email.isEmpty()) {
            newClient.setEmail(email);
        } else {
            throw new BadRequestException("El email del cliente no puede ser nulo o vacío");
        }
        return newClient;
    }


    public static ProductCli1 buildProduct(JsonNode productNode) throws BadRequestException {
        ProductCli1 newProduct = new ProductCli1();

        String idCli1 = JsonUtils.getString(productNode, PRODUCT_IDCLI1_ATTRIBUTES, "");
        if (idCli1 != null && !idCli1.isEmpty()) {
            newProduct.setIdCli1(idCli1);
        } else {
            throw new BadRequestException("El identificador del producto no puede ser nulo o vacío");
        }

        String product = JsonUtils.getString(productNode, PRODUCT_NAME_ATTRIBUTES, "");

        if (product != null && !product.isEmpty()) {
            newProduct.setProduct(product);
        } else {
            throw new BadRequestException("El nombre del producto no puede ser nulo o vacío");
        }
        return newProduct;
    }

}
