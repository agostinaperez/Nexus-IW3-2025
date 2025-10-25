package edu.iua.nexus.integration.cli1.util;

import edu.iua.nexus.integration.cli1.model.ClientCli1;
import edu.iua.nexus.integration.cli1.model.DriverCli1;
import edu.iua.nexus.integration.cli1.model.business.interfaces.IClientCli1Business;
import edu.iua.nexus.integration.cli1.model.business.interfaces.IDriverCli1Business;
import edu.iua.nexus.integration.cli1.model.business.interfaces.IProductCli1Business;
import edu.iua.nexus.integration.cli1.model.business.interfaces.ITruckCli1Business;
import edu.iua.nexus.model.Client;
import edu.iua.nexus.model.Driver;
import edu.iua.nexus.model.Product;
import edu.iua.nexus.model.Truck;
import edu.iua.nexus.model.business.BadRequestException;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;

import static edu.iua.nexus.integration.cli1.util.JsonAttributeConstants.*;
import static edu.iua.nexus.util.JsonUtils.getJsonNode;
import static edu.iua.nexus.util.JsonUtils.getString;

/**
 * Funciones utilitarias que encapsulan la lógica de lectura de los nodos JSON enviados por el CLI1.
 * Cada helper navega hasta el subdocumento correcto, valida los atributos esperados y delega en los
 * business correspondientes para crear/obtener las entidades del dominio interno.
 */
public class JsonUtilsCli1 {

    /**
     * Obtiene o crea un conductor a partir del nodo "driver".
     *
     * @param node nodo raíz del payload CLI1
     * @param attrs posibles nombres del campo documento
     * @param driverCli1Business capa de negocio que persiste el conductor
     */
    public static Driver getDriver(JsonNode node, String[] attrs, IDriverCli1Business driverCli1Business) throws FoundException, BusinessException, NotFoundException, BadRequestException {
        JsonNode driverNode = getJsonNode(node, DRIVER_NODE_ATTRIBUTES); // Buscar el nodo padre "driver"
        if (driverNode != null) {
            String driverDocument = null;
            // Recorremos los atributos dentro del nodo "driver"
            for (String attr : attrs) {
                if (driverNode.get(attr) != null) {
                    driverDocument = driverNode.get(attr).asText();
                    break;
                }
            }
            if (driverDocument != null) {
                DriverCli1 driver = BuildEntityUtils.buildDriver(driverNode);
                return driverCli1Business.addExternal(driver);
            } else {
                throw new BadRequestException("El campo documento del conductor no se recibió correctamente");
            }
        } else {
            throw new BadRequestException("El nodo conductor no se recibió correctamente");
        }
    }

    /**
     * Recorre el nodo "truck", procesa la colección de tanques y delega el alta/lookup del camión.
     */
    public static Truck getTruck(JsonNode node, String[] attrs, ITruckCli1Business truckCli1Business) throws FoundException, BusinessException, NotFoundException, BadRequestException {
        JsonNode truckNode = getJsonNode(node, TRUCK_NODE_ATTRIBUTES); // Buscar el nodo padre "truck"
        if (truckNode != null) {
            String truckLicensePlate = getString(truckNode, attrs, null);  // Obtener placa del camión desde los atributos
            if (truckLicensePlate != null) {
                JsonNode tanksNode = truckNode.get("tanks");
                return truckCli1Business.addExternal(BuildEntityUtils.buildTruck(truckNode, tanksNode));
            } else {
                throw new BadRequestException("El campo placa del camión no se recibió correctamente");
            }
        } else {
            throw new BadRequestException("El nodo camión no se recibió correctamente");
        }
    }

    /**
     * Interpreta el nodo "client" y registra al cliente si aún no existe.
     */
    public static Client getClient(JsonNode node, String[] attrs, IClientCli1Business clientCli1Business) throws FoundException, BusinessException, NotFoundException, BadRequestException {
        JsonNode clientNode = getJsonNode(node, CLIENT_NODE_ATTRIBUTES); // Buscar el nodo padre "client"
        if (clientNode != null) {
            String clientName = null;
            // Recorremos los atributos dentro del nodo "client"
            for (String attr : attrs) {
                if (clientNode.get(attr) != null) {
                    clientName = clientNode.get(attr).asText();
                    break;
                }
            }
            if (clientName != null) {
                ClientCli1 client = BuildEntityUtils.buildClient(clientNode);
                return clientCli1Business.addExternal(client); // Cargar el client desde el business
            } else {
                throw new BadRequestException("El campo cliente no se recibió correctamente");
            }
        } else {
            throw new BadRequestException("El nodo cliente no se recibió correctamente");
        }
        //return null;
    }

    /**
     * Carga el producto asociado a la orden recurriendo al catálogo interno.
     */
    public static Product getProduct(JsonNode node, String[] attrs, IProductCli1Business productCli1Business) throws BusinessException, NotFoundException, FoundException, BadRequestException {
        JsonNode productNode = getJsonNode(node, PRODUCT_NODE_ATTRIBUTES); // Buscar el nodo padre "product"
        if (productNode != null) {
            String productName = null;
            // Recorremos los atributos dentro del nodo "product"
            for (String attr : attrs) {
                if (productNode.get(attr) != null) {
                    productName = productNode.get(attr).asText();
                    break;
                }
            }
            if (productName != null) {
                return productCli1Business.loadExternal(BuildEntityUtils.buildProduct(productNode)); // Cargar el producto desde el business
            } else {
                throw new BadRequestException("El campo producto no se recibió correctamente");
            }
        } else {
            throw new BadRequestException("El nodo producto no se recibió correctamente");
        }
    }
}