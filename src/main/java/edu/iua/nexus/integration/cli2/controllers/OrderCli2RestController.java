package edu.iua.nexus.integration.cli2.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
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
import edu.iua.nexus.integration.cli2.model.business.impl.OrderCli2Business;
import edu.iua.nexus.model.Order;

@Tag(name = "Cliente 2 - Balanza (TMS)", description = "Endpoints para el Punto 2 (Pesaje Inicial) y Punto 5 (Pesaje Final).")
@RestController
@RequestMapping(Constants.URL_INTEGRATION_CLI2 + "/orders")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLI2')")
public class OrderCli2RestController extends BaseRestController{
    
    @Autowired
    private OrderCli2Business orderCli2Business;
    // --- Documentación de Swagger ---
    @Operation(
        summary = "Punto 2: Registrar Pesaje Inicial",
        description = "Registra la tara (peso inicial) del camión y genera la contraseña de activación."
    )
    @Parameter(
        name = "License-Plate", 
        description = "Patente del camión a pesar.", 
        required = true, 
        in = ParameterIn.HEADER, // Indica que está en la cabecera
        example = "AA123BB"
    )
    @Parameter(
        name = "Initial-Weight", 
        description = "Peso inicial (tara) en Kg.", 
        required = true, 
        in = ParameterIn.HEADER,
        example = "15000.5"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Pesaje registrado. Devuelve el PIN de 5 dígitos en el body.", 
            headers = @Header(name = "Order-Id", description = "ID de la orden actualizada")
        ),
        @ApiResponse(responseCode = "401", description = "No autorizado (Falta token o es inválido)"),
        @ApiResponse(responseCode = "403", description = "Prohibido (Token no tiene rol CLI2 o ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada para esa patente y estado 'RECEIVED'")
    })
    // --- Fin de la Documentación ---
    @SneakyThrows
    @PostMapping(value = "/initial-weighing", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> registerInitialWeighing(
            @RequestHeader("License-Plate") String licensePlate,
            @RequestHeader("Initial-Weight") float initialWeight) {
        Order order = orderCli2Business.registerInitialWeighing(licensePlate, initialWeight);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Order-Id", String.valueOf(order.getId()));
        return new ResponseEntity<>(order.getActivatePassword().toString(), responseHeaders, HttpStatus.OK);
    }

// ENDPOINT 5
    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(
        summary = "Punto 5: Registrar Pesaje Final y obtener Conciliación",
        description = "Registra el peso final del camión, cierra la orden a estado 4 y devuelve la conciliación en JSON."
    )
    @Parameter(
        name = "License-Plate", 
        description = "Patente del camión.", 
        required = true, 
        in = ParameterIn.HEADER, 
        example = "AA123BB"
    )
    @Parameter(
        name = "Final-Weight", 
        description = "Peso final (bruto) en Kg.", 
        required = true, 
        in = ParameterIn.HEADER,
        example = "40000.0"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Pesaje final registrado. Devuelve el JSON de conciliación.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))
        ),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Prohibido"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada para esa patente y estado 'CLOSED'"),
        @ApiResponse(responseCode = "400", description = "El peso final es menor o igual al inicial")
    })
    // --- Fin de la Documentación ---
    @PostMapping(value = "/final-weighing", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerFinalWeighing(
            @RequestHeader("License-Plate") String licensePlate,
            @RequestHeader("Final-Weight") float finalWeight) {

        byte[] conciliation = orderCli2Business.registerFinalWeighing(licensePlate, finalWeight);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");

        return new ResponseEntity<>(conciliation, responseHeaders, HttpStatus.OK);
    }

    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(
        summary = "Punto 5: Obtener Conciliación (ya finalizada)",
        description = "Permite a un cliente (ej: el frontend) consultar la conciliación de una orden que ya esté en estado 4 (REGISTERED_FINAL_WEIGHING)."
    )
    @Parameter(
        name = "License-Plate", 
        description = "Patente del camión de la orden finalizada.", 
        required = true, 
        in = ParameterIn.HEADER, 
        example = "AA123BB"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Devuelve el JSON de conciliación.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))
        ),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Prohibido"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada en estado 4 para esa patente")
    })
    // --- Fin de la Documentación ---
    @GetMapping(value = "/conciliation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getConciliation(
            @RequestHeader("License-Plate") String licensePlate) {

        byte[] conciliation = orderCli2Business.getConciliation(licensePlate);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");

        return new ResponseEntity<>(conciliation, responseHeaders, HttpStatus.OK);
    }
}
