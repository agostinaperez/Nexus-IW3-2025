package edu.iua.nexus.controllers;

import edu.iua.nexus.Constants;
import edu.iua.nexus.model.Product;
import edu.iua.nexus.model.business.interfaces.IProductBusiness;
import edu.iua.nexus.model.serializers.ProductSlimV1JsonSerializer;
import edu.iua.nexus.util.JsonUtils;
import edu.iua.nexus.util.StandartResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(description = "API para Gestionar Productos", name = "Product")
@RestController
@RequestMapping(Constants.URL_PRODUCTS)
public class ProductRestController extends BaseRestController {

    @Autowired
    private IProductBusiness productBusiness;


    @Operation(operationId = "list-internal-products", summary = "Listar productos", description = "Lista todos los productos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))}),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes para acceder al recurso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
    })
    @SneakyThrows
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list() {

        List<Product> products = productBusiness.list();

        StdSerializer<Product> productSerializer = new ProductSlimV1JsonSerializer(Product.class, false);
        ObjectMapper mapper = JsonUtils.getObjectMapper(Product.class, productSerializer, null);

        List<Object> serializedProducts = products.stream()
                .map(product -> {
                    try {
                        return mapper.valueToTree(product);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al serializar el objeto Product", e);
                    }
                }).toList();

        return new ResponseEntity<>(serializedProducts, HttpStatus.OK);
    }



    @Operation(operationId = "load-internal-product", summary = "Cargar producto", description = "Carga los datos de un producto")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Identificador del producto", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto cargado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))}),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes para acceder al recurso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
    })
    @SneakyThrows
    @GetMapping(value = "/{id}")
    public ResponseEntity<?> loadProduct(@PathVariable long id) {
        return new ResponseEntity<>(productBusiness.load(id), HttpStatus.OK);
    }


    @Operation(operationId = "load-internal-product-by-name", summary = "Cargar producto por nombre", description = "Carga los datos de un producto por nombre")
    @Parameter(in = ParameterIn.PATH, name = "product", description = "Nombre del producto", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto cargado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))}),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes para acceder al recurso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),

    })
    @SneakyThrows
    @GetMapping(value = "/by_name/{product}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loadProduct(@PathVariable String product) {
        return new ResponseEntity<>(productBusiness.load(product), HttpStatus.OK);
    }


    @Operation(operationId = "add-internal-product", summary = "Agregar producto", description = "Agrega un producto")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Objeto JSON que representa los datos del producto",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Product.class)
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto agregado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))}),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes para acceder al recurso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
    })
    @SneakyThrows
    @PostMapping(value = "")
    public ResponseEntity<?> addProduct(@Valid @RequestBody Product product) {
        Product response = productBusiness.add(product);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", Constants.URL_PRODUCTS + "/" + response.getId());
        return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
    }


    @Operation(operationId = "update-internal-product", summary = "Actualizar producto", description = "Actualiza un producto")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Objeto JSON que representa los datos del producto",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Product.class)
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes para acceder al recurso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
    })
    @SneakyThrows
    @PutMapping(value = "")
    public ResponseEntity<?> updateProduct(@RequestBody Product product) {
        productBusiness.update(product);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Operation(operationId = "delete-internal-product", summary = "Eliminar producto", description = "Elimina un producto")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Identificador del producto", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto eliminado"),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes para acceder al recurso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error interno", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StandartResponse.class))}),
    })
    @SneakyThrows
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable long id) {
        productBusiness.delete(id);
        return new ResponseEntity<String>(HttpStatus.OK);
    }
}