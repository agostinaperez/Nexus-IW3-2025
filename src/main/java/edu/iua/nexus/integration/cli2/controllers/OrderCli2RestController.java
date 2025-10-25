package edu.iua.nexus.integration.cli2.controllers;

import edu.iua.nexus.Constants;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import edu.iua.nexus.controllers.BaseRestController;
import edu.iua.nexus.integration.cli2.model.business.impl.OrderCli2Business;
import edu.iua.nexus.model.Order;


@RestController
@RequestMapping(Constants.URL_INTEGRATION_CLI2 + "/orders")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLI2')")
public class OrderCli2RestController extends BaseRestController{
    
    @Autowired
    private OrderCli2Business orderCli2Business;

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
}
