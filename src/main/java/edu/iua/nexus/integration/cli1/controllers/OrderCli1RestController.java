package edu.iua.nexus.integration.cli1.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import edu.iua.nexus.Constants;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import edu.iua.nexus.controllers.BaseRestController;
import edu.iua.nexus.integration.cli1.model.OrderCli1;
import edu.iua.nexus.integration.cli1.model.business.interfaces.IOrderCli1Business;

@Tag(name = "Cliente 1 - Sistema Externo (SAP)", description = "Punto 1: Recepción y cancelación de órdenes.")
@RestController
@RequestMapping(Constants.URL_INTEGRATION_CLI1 + "/orders")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLI1')")
public class OrderCli1RestController extends BaseRestController {

    @Autowired
    private IOrderCli1Business orderCli1Business;
//EL SNEAKY THROWS DE LOMBOK ME DEJA PONER ESTO EN VEZ DE UN BLOQUE DE TRY CARCH O UN THROWS, PERO SIRVE SOLO PARA EXCEPCIONES COMPROBADAS NO LAS Q CREO YO
    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(summary = "Listar todas las órdenes de CLI1", description = "Devuelve una lista de todas las órdenes creadas por el Cliente 1.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de órdenes"),
        @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    // --- Fin de la Documentación ---
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list() {
        return new ResponseEntity<>(orderCli1Business.list(), HttpStatus.OK);
    }

    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(summary = "Obtener orden por número externo (orderNumberCli1)", description = "Busca y devuelve una orden usando su identificador externo único.")
    @Parameter(name = "orderNumberCli1", description = "ID externo de la orden", required = true, in = ParameterIn.PATH, example = "ORD-SAP-12345")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden encontrada"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    // --- Fin de la Documentación ---
    @GetMapping(value = "/{orderNumberCli1}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loadByCode(@PathVariable(value = "orderNumberCli1") String orderNumberCli1) {
        return new ResponseEntity<>(orderCli1Business.load(orderNumberCli1), HttpStatus.OK);
    }

    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(
        summary = "Punto 1: Recibir y crear orden (B2B)",
        description = "Endpoint principal para recibir el JSON del sistema externo (SAP) y crear una nueva orden de carga."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "JSON con los datos de la orden. El formato es flexible y se parsea con `OrderCli1JsonDeserializer`.",
        required = true,
        content = @Content(mediaType = "application/json",
            schema = @Schema(type = "object"),
            examples = @ExampleObject(value = 
                "{\n" +
                "  \"order_number\": \"ORD-SAP-12345\",\n" +
                "  \"estimated_date\": \"2025-11-10T14:00:00-0300\",\n" +
                "  \"preset\": 25000,\n" +
                "  \"client\": {\n" +
                "    \"id_client\": \"C-YPF-01\",\n" +
                "    \"name\": \"YPF S.A.\",\n" +
                "    \"email\": \"contacto@ypf.com\"\n" +
                "  },\n" +
                "  \"product\": {\n" +
                "    \"id_product\": \"P-GASOIL-INF\",\n" +
                "    \"product_name\": \"GASOIL INFINIA\"\n" +
                "  },\n" +
                "  \"truck\": {\n" +
                "    \"id_truck\": \"T-001\",\n" +
                "    \"license_plate\": \"AA123BB\",\n" +
                "    \"description\": \"Scania V8\",\n" +
                "    \"tanks\": [\n" +
                "      { \"id_tank\": \"TK-001A\", \"capacity_liters\": 15000, \"license\": \"CIST-001\" },\n" +
                "      { \"id_tank\": \"TK-001B\", \"capacity_liters\": 10000, \"license\": \"CIST-002\" }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"driver\": {\n" +
                "    \"id_driver\": \"D-789\",\n" +
                "    \"name\": \"Juan\",\n" +
                "    \"lastname\": \"Perez\",\n" +
                "    \"document\": \"30123456\"\n" +
                "  }\n" +
                "}"
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Orden creada exitosamente", headers = @Header(name = "Location", description = "URL de la nueva orden")),
        @ApiResponse(responseCode = "400", description = "JSON mal formado o datos faltantes"),
        @ApiResponse(responseCode = "409", description = "Conflicto (ej: orden duplicada)")
    })
    // --- Fin de la Documentación ---
    @PostMapping(value = "/b2b")
    public ResponseEntity<?> addExternal(HttpEntity<String> httpEntity) {
        OrderCli1 response = orderCli1Business.addExternal(httpEntity.getBody());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Location", Constants.URL_INTEGRATION_CLI1 + "/orders/" + response.getOrderNumberCli1());
        return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
    }

    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(summary = "Cancelar orden externa", description = "Cancela una orden que aún esté en estado 'RECEIVED'.")
    @Parameter(name = "orderId", description = "ID externo (orderNumberCli1) de la orden a cancelar", required = true, in = ParameterIn.HEADER, example = "ORD-SAP-12345")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden cancelada"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
        @ApiResponse(responseCode = "500", description = "La orden no se puede cancelar (ej: estado incorrecto)")
    })
    // --- Fin de la Documentación ---
    @PostMapping(value = "/cancel")
    public ResponseEntity<?> cancelExternal(@RequestHeader("orderId") String orderNumberCli1) {
        OrderCli1 response = orderCli1Business.cancelExternal(orderNumberCli1);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", Constants.URL_INTEGRATION_CLI1 + "/orders/" + response.getOrderNumberCli1());
        return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }

}