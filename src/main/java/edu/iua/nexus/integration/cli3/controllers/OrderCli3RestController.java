package edu.iua.nexus.integration.cli3.controllers;

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
import edu.iua.nexus.integration.cli3.model.serializers.OrderCli3SlimV1JsonSerializer;
import edu.iua.nexus.integration.cli3.model.business.interfaces.IOrderCli3Business;
import edu.iua.nexus.model.Detail;
import edu.iua.nexus.model.Order;
import edu.iua.nexus.util.JsonUtils;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.logging.Logger;

@Tag(name = "Cliente 3 - Sistema de Carga", description = "Punto 3 (Datos en tiempo real) y Punto 4 (Cierre de Orden).")
@RestController
@RequestMapping(Constants.URL_INTEGRATION_CLI3 + "/orders")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLI3')")
public class OrderCli3RestController {
@Autowired
    private IOrderCli3Business orderCli3Business;
    private static final Logger log = Logger.getLogger(OrderCli3RestController.class.getName());

    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(
        summary = "Punto 3: Validar Contraseña de Activación",
        description = "Valida el PIN de 5 dígitos (generado en Punto 2) para habilitar la carga. Devuelve el ID de la orden y el preset."
    )
    @Parameter(name = "Password", description = "PIN de 5 dígitos", required = true, in = ParameterIn.HEADER, example = "54321")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Contraseña válida. Devuelve JSON con ID y preset.",
            content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"id\":102, \"preset\":25000}"))
        ),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Prohibido"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada (contraseña inválida)"),
        @ApiResponse(responseCode = "409", description = "La orden no está en el estado correcto (ej: ya se cargó)")
    })
    // --- Fin de la Documentación ---
    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    //el request header es para un dato que no es parte del contenido del request, sino metadatos (por ej. tokens, claves, hashes)
    //en el body no es necesario pero podría, y en los params no pq no quiero q se vea
    public ResponseEntity<?> validatePassword(@RequestHeader("Password") Integer password) {
        log.info("REST request to validate password for CLI3 Order");
        Order order = orderCli3Business.validatePassword(password);
        //el serializer solo devuelve los campos necesarios q son el id y el preset, o sea q devuelvo un json reducido
        StdSerializer<Order> ser = new OrderCli3SlimV1JsonSerializer(Order.class, false);
        String result = JsonUtils.getObjectMapper(Order.class, ser, null).writeValueAsString(order);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(
        summary = "Punto 3: Recibir Datos de Carga en Tiempo Real",
        description = "Recibe un objeto 'Detail' con los datos del caudalímetro (masa, densidad, temp, caudal)."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Objeto JSON con el detalle de carga. El 'id' de la orden debe venir anidado.",
        required = true,
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Detail.class),
            examples = @ExampleObject(value = 
                "{\n" +
                "  \"order\": { \"id\": 102 },\n" +
                "  \"accumulatedMass\": 1500.25,\n" +
                "  \"density\": 0.85,\n" +
                "  \"temperature\": 25.5,\n" +
                "  \"flowRate\": 5000.0\n" +
                "}"
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle procesado", headers = @Header(name = "Order-Id", description = "ID de la orden actualizada")),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Prohibido"),
        @ApiResponse(responseCode = "409", description = "La orden no está en el estado correcto (ej: está cerrada)")
    })
    // --- Fin de la Documentación ---
    @PostMapping("/detail")
    public ResponseEntity<?> receiveLoadData(@RequestBody Detail detail) {
        log.info("REST request to receive detail for CLI3 Order");
        Order order = orderCli3Business.receiveDetails(detail);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Order-Id", String.valueOf(order.getId()));
        return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }

    //Punto 4
    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(
        summary = "Punto 4: Cerrar Orden de Carga",
        description = "Cambia el estado de la orden a 'CLOSED' (Cerrada para carga), indicando que finalizó el llenado."
    )
    @Parameter(name = "OrderId", description = "ID de la orden que se desea cerrar.", required = true, in = ParameterIn.HEADER, example = "102")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Orden cerrada exitosamente.",
            headers = @Header(name = "Order-Id", description = "ID de la orden que se cerró")
        ),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Prohibido"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada."),
        @ApiResponse(responseCode = "409", description = "La orden no está en el estado correcto para ser cerrada.")
    })
    // --- Fin de la Documentación ---
    @PostMapping("/close")
    public ResponseEntity<?> closeOrderCli3Endpoint(@RequestHeader("OrderId") Long orderId) {
        Order order = orderCli3Business.closeOrder(orderId);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Order-Id", String.valueOf(order.getId()));
        return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }
}
