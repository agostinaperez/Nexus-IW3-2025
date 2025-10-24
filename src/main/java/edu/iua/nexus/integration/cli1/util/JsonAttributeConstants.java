package edu.iua.nexus.integration.cli1.util;

public class JsonAttributeConstants {

    // Constantes para el nodo Order
    public static final String[] ORDER_NUMBER_ATTRIBUTES = {"number", "order_number", "order"};
    public static final String[] ORDER_ESTIMATED_DATE_ATTRIBUTES = {"estimated_date", "estimated_date_order", "estimated_time", "estimated_time_order"};
    public static final String[] ORDER_PRESET_ATTRIBUTES = {"preset", "order_preset"};

    // Constantes para el nodo Driver
    public static final String[] DRIVER_NODE_ATTRIBUTES = {"driver", "Driver", "choffer", "Choffer"};
    public static final String[] DRIVER_IDCLI1_ATTRIBUTES = {"id", "id_driver", "code", "driver_code"};
    public static final String[] DRIVER_NAME_ATTRIBUTES = {"name", "driver_name", "driver","chofer_name"};
    public static final String[] DRIVER_LASTNAME_ATTRIBUTES = {"lastname", "driver_lastname", "last_name", "driver_last_name","choffer_last_name"};
    public static final String[] DRIVER_DOCUMENT_ATTRIBUTES = {"driver_document", "driver_document_number", "document","choffer_document"};

    // Constantes para el nodo Truck
    public static final String[] TRUCK_NODE_ATTRIBUTES = {"truck", "Truck", "vehicle", "Vehicle"};
    public static final String[] TRUCK_IDCLI1_ATTRIBUTES = {"id", "id_truck", "code", "truck_code"};
    public static final String[] TRUCK_LICENSE_PLATE_ATTRIBUTES = {"truck_plate", "truck_plate_number", "license_plate", "truck_license_plate"};
    public static final String[] TRUCK_DESCRIPTION_ATTRIBUTES = {"description", "truck_description"};

    // Constantes para el nodo Tank
    public static final String[] TANK_CAPACITY_ATTRIBUTES = {"capacity_liters", "capacity"};
    public static final String[] TANK_IDCLI1_ATTRIBUTES = {"id", "id_tank", "code", "tank_code"};
    public static final String[] TANK_LICENSE_ATTRIBUTES = {"license", "license_plate", "plate"};

    // Constantes para el nodo Client
    public static final String[] CLIENT_NODE_ATTRIBUTES = {"client", "Client", "client", "Client"};
    public static final String[] CLIENT_IDCLI1_ATTRIBUTES = {"id", "id_client", "code", "client_code"};
    public static final String[] CLIENT_NAME_ATTRIBUTES = {"name", "client_name", "business_name", "client", "client_name"};
    public static final String[] CLIENT_EMAIL_ATTRIBUTES = {"mail", "email", "contact", "mail_contact"};

    // Constantes para el nodo Product
    public static final String[] PRODUCT_NODE_ATTRIBUTES = {"product", "Product", "gas", "Gas", "fuel", "Fuel"};
    public static final String[] PRODUCT_IDCLI1_ATTRIBUTES = {"id", "id_product", "code", "product_code"};
    public static final String[] PRODUCT_NAME_ATTRIBUTES = {"product", "product_name","name"};
}