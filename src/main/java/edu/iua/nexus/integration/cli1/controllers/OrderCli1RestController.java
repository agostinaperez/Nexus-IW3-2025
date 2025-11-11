package edu.iua.nexus.integration.cli1.controllers;

import edu.iua.nexus.Constants;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import edu.iua.nexus.controllers.BaseRestController;
import edu.iua.nexus.integration.cli1.model.OrderCli1;
import edu.iua.nexus.integration.cli1.model.business.interfaces.IOrderCli1Business;

@RestController
@RequestMapping(Constants.URL_INTEGRATION_CLI1 + "/orders")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLI1')")
public class OrderCli1RestController extends BaseRestController {

    @Autowired
    private IOrderCli1Business orderCli1Business;
//EL SNEAKY THROWS DE LOMBOK ME DEJA PONER ESTO EN VEZ DE UN BLOQUE DE TRY CARCH O UN THROWS, PERO SIRVE SOLO PARA EXCEPCIONES COMPROBADAS NO LAS Q CREO YO
    @SneakyThrows
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list() {
        return new ResponseEntity<>(orderCli1Business.list(), HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/{orderNumberCli1}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loadByCode(@PathVariable(value = "orderNumberCli1") String orderNumberCli1) {
        return new ResponseEntity<>(orderCli1Business.load(orderNumberCli1), HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping(value = "/b2b")
    public ResponseEntity<?> addExternal(HttpEntity<String> httpEntity) {
        OrderCli1 response = orderCli1Business.addExternal(httpEntity.getBody());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Location", Constants.URL_INTEGRATION_CLI1 + "/orders/" + response.getOrderNumberCli1());
        return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
    }

    @SneakyThrows
    @PostMapping(value = "/cancel")
    public ResponseEntity<?> cancelExternal(@RequestHeader("orderId") String orderNumberCli1) {
        OrderCli1 response = orderCli1Business.cancelExternal(orderNumberCli1);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", Constants.URL_INTEGRATION_CLI1 + "/orders/" + response.getOrderNumberCli1());
        return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }

}