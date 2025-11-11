package edu.iua.nexus.integration.cli1.controllers;

import edu.iua.nexus.Constants;
import lombok.SneakyThrows;
import io.swagger.v3.oas.annotations.headers.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import edu.iua.nexus.controllers.BaseRestController;
import edu.iua.nexus.integration.cli1.model.OrderCli1;
import edu.iua.nexus.integration.cli1.model.business.interfaces.IOrderCli1Business;
import edu.iua.nexus.util.StandartResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
@Tag(name = "Cliente 1 - Sistema Externo (SAP)", description = "Punto 1: Recepción y cancelación de órdenes.")
@RestController
@RequestMapping(Constants.URL_INTEGRATION_CLI1 + "/orders")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLI1')")
public class OrderCli1RestController extends BaseRestController {

    @Autowired
    private IOrderCli1Business orderCli1Business;
//EL SNEAKY THROWS DE LOMBOK ME DEJA PONER ESTO EN VEZ DE UN BLOQUE DE TRY CARCH O UN THROWS, PERO SIRVE SOLO PARA EXCEPCIONES COMPROBADAS NO LAS Q CREO YO
    
    // --- Documentación de Swagger ---
    @Operation(summary = "Listar todas las órdenes de CLI1", description = "Devuelve una lista de todas las órdenes creadas por el Cliente 1.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de órdenes"),
        @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    // --- Fin de la Documentación ---
    @SneakyThrows
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list() {
        return new ResponseEntity<>(orderCli1Business.list(), HttpStatus.OK);
    }

    // --- Documentación de Swagger ---
    @Operation(summary = "Obtener orden por número externo (orderNumberCli1)", description = "Busca y devuelve una orden usando su identificador externo único.")
    @Parameter(name = "orderNumberCli1", description = "ID externo de la orden", required = true, in = ParameterIn.PATH, example = "ORD-SAP-12345")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden encontrada"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    // --- Fin de la Documentación ---
    @SneakyThrows
    @GetMapping(value = "/{orderNumberCli1}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loadByCode(@PathVariable(value = "orderNumberCli1") String orderNumberCli1) {
        return new ResponseEntity<>(orderCli1Business.load(orderNumberCli1), HttpStatus.OK);
    }

   @Operation(
        summary = "Punto 1: Recibir y crear orden (B2B)",
        description = "Endpoint principal para recibir el JSON del sistema externo (SAP) y crear una nueva orden de carga.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Objeto JSON que representa los datos de la orden de carga",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(
                            example = """
            {
                "order_number": "String",
                "truck": {
                    "id": "String",
                    "license_plate": "String",
                    "description": "String",
                    "tanks": [
                        {
                            "id": "String",
                            "capacity": 0,
                            "license_plate": "String"
                        }
                    ]
                },
                "driver": {
                    "id": "String",
                    "name": "String",
                    "last_name": "String",
                    "document": "String"
                },
                "customer": {
                    "id": "String",
                    "business_name": "String",
                    "email": "String"
                },
                "product": {
                    "product": "String",
                    "description": "String"
                },
                "estimated_date": "String",
                "preset": 0
            }
            """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Carga registrada exitosamente.", headers = {
                    @Header(name = "Location", description = "Ubicacion orden", schema = @Schema(type = "string"))}),
            @ApiResponse(responseCode = "302", description = "Ya existe una orden con el codigo externo", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "302", description = "Ya existe una orden para el camion idExterno", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes para acceder al recurso.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
    })
    @SneakyThrows
    @PostMapping(value = "/b2b")
    public ResponseEntity<?> addExternal(HttpEntity<String> httpEntity) {
        OrderCli1 response = orderCli1Business.addExternal(httpEntity.getBody());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Location", Constants.URL_INTEGRATION_CLI1 + "/orders/" + response.getOrderNumberCli1());
        return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
    }
 // --- Documentación de Swagger ---
    @Operation(
            operationId = "cancel-external-order",
            summary = "Cancela orden de carga",
            description = "Cancela una orden de carga externa.")
    @Parameter(in = ParameterIn.HEADER, name = "order", schema = @Schema(type = "String"), required = true, description = "Número de orden de carga externa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden de carga cancelada exitosamente.", headers = {
                    @Header(name = "Location", description = "Ubicacion orden", schema = @Schema(type = "string"))}),
            @ApiResponse(responseCode = "400", description = "Parametros insuficientes, entidades no enviadas", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes para acceder al recurso.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
    })
    // --- Fin de la Documentación ---
    @SneakyThrows
    @PostMapping(value = "/cancel")
    public ResponseEntity<?> cancelExternal(@RequestHeader("orderId") String orderNumberCli1) {
        OrderCli1 response = orderCli1Business.cancelExternal(orderNumberCli1);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", Constants.URL_INTEGRATION_CLI1 + "/orders/" + response.getOrderNumberCli1());
        return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }

}