package edu.iua.nexus.integration.cli3.controllers;

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

@RestController
@RequestMapping(Constants.URL_INTEGRATION_CLI3 + "/orders")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLI3')")
public class OrderCli3RestController {
@Autowired
    private IOrderCli3Business orderCli3Business;
    private static final Logger log = Logger.getLogger(OrderCli3RestController.class.getName());

    @SneakyThrows
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
    @PostMapping("/detail")
    public ResponseEntity<?> receiveLoadData(@RequestBody Detail detail) {
        log.info("REST request to receive detail for CLI3 Order");
        Order order = orderCli3Business.receiveDetails(detail);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Order-Id", String.valueOf(order.getId()));
        return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }
}
