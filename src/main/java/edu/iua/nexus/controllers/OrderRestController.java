package edu.iua.nexus.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import edu.iua.nexus.Constants;
import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.business.interfaces.IOrderBusiness;
import edu.iua.nexus.model.serializers.OrderSlimJsonSerializer;
import edu.iua.nexus.util.FieldValidator;
import edu.iua.nexus.util.JsonUtils;
import edu.iua.nexus.util.PaginationInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Órdenes (Frontend)", description = "Endpoints para la UI interna (Monitoreo, listado y detalle de órdenes).")
@RestController
@RequestMapping(Constants.URL_ORDERS)
public class OrderRestController extends BaseRestController {

    @Autowired
    private IOrderBusiness orderBusiness;

    /* ENPOINT PARA OBTENER UNA LISTA DE ORDENES (PAGINABLE) */
    // --- Documentación de Swagger ---
    @Operation(
        summary = "Listar órdenes (Paginado)",
        description = "Obtiene una lista paginada de todas las órdenes, con filtros y ordenamiento. Para el frontend."
    )
    @Parameter(name = "page", description = "Número de página (empieza en 0)", in = ParameterIn.QUERY, example = "0")
    @Parameter(name = "size", description = "Tamaño de la página", in = ParameterIn.QUERY, example = "10")
    @Parameter(name = "filter", description = "Filtro por estado(s), separado por comas (ej: RECEIVED,CLOSED)", in = ParameterIn.QUERY)
    @Parameter(name = "sort", description = "Campo para ordenar (ej: 'id,asc' o 'externalReceptionDate,desc')", in = ParameterIn.QUERY, example = "id,desc")
    @ApiResponse(responseCode = "200", description = "Página de órdenes obtenida")
    // --- Fin de la Documentación ---
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_OPERATOR')")
    @SneakyThrows
    public ResponseEntity<?> getAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    @RequestParam(value = "filter", required = false) String filter,
                                    @RequestParam(value = "sort", defaultValue = "externalReceptionDate,desc") String sort) {

        Pageable pageable;
        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0].trim();
            String sortDirection = (sortParams.length > 1 ? sortParams[1].trim().toLowerCase() : "desc"); // Dirección predeterminada
            Sort.Direction direction = sortDirection.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

            // Validar el campo de ordenación
            if (FieldValidator.isValidField(Order.class, sortField)) {
                throw new IllegalArgumentException("El campo de ordenación '" + sortField + "' no es válido para la entidad Order");
            }
            pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        } else {
            pageable = PageRequest.of(page, size);
        }

        final List<String> statusFilters = (filter != null && !filter.isEmpty())
                ? List.of(filter.split(","))
                : null;

        Page<Order> orders = orderBusiness.list(pageable, statusFilters);

        StdSerializer<Order> orderSerializer = new OrderSlimJsonSerializer(Order.class, false);
        ObjectMapper mapper = JsonUtils.getObjectMapper(Order.class, orderSerializer, null);

        List<Object> serializedOrders = orders.getContent().stream()
                .map(order -> {
                    try {
                        return mapper.valueToTree(order);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al serializar el objeto Order", e);
                    }
                }).toList();

        PaginationInfo paginationInfo = new PaginationInfo(
                orders.getPageable(),
                orders.getTotalPages(),
                orders.getTotalElements(),
                orders.getNumber(),
                orders.getSize(),
                orders.getNumberOfElements()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("orders", serializedOrders);
        response.put("pagination", paginationInfo);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /* ENPOINT PARA OBTENER EL DETALLE DE UNA ORDEN POR SU ID */
    // --- Documentación de Swagger ---
    @Operation(
        summary = "Obtener detalle de una Orden por ID",
        description = "Devuelve todos los datos de una orden específica, serializada con `OrderSlimJsonSerializer.serializeOrderDetail`."
    )
    @Parameter(name = "id", description = "ID interno de la orden", in = ParameterIn.PATH, required = true, example = "102")
    @ApiResponse(responseCode = "200", description = "Orden encontrada")
    @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    // --- Fin de la Documentación ---
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_OPERATOR')")
    @SneakyThrows
    public ResponseEntity<?> getOrderById(@PathVariable("id") Long id) {
        Order order = orderBusiness.load(id);
        OrderSlimJsonSerializer orderSerializer = new OrderSlimJsonSerializer(Order.class, false);
        ObjectMapper mapper = JsonUtils.getObjectMapper(Order.class, orderSerializer, null);
        try {
            // Crear un generador de JSON
            StringWriter writer = new StringWriter();
            JsonGenerator jsonGenerator = mapper.getFactory().createGenerator(writer);

            // Llamar al method de serialización para el detalle
            orderSerializer.serializeOrderDetail(order, jsonGenerator);

            // Cerrar el generador y obtener el resultado
            jsonGenerator.close();
            String serializedOrder = writer.toString();

            return new ResponseEntity<>(serializedOrder, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Error al serializar el objeto Order", e);
        }
    }
}