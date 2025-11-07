package edu.iua.nexus.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import edu.iua.nexus.Constants;
import edu.iua.nexus.auth.model.User;
import edu.iua.nexus.model.business.interfaces.IUserBusiness;
import edu.iua.nexus.model.serializers.UserSlimV1JsonSerializer;
import edu.iua.nexus.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuarios (Admin)", description = "CRUD de usuarios para administradores.")
@RestController
@RequestMapping(Constants.URL_USERS)
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserRestController extends BaseRestController {

    @Autowired
    private IUserBusiness userBusiness;

    @Autowired
    private PasswordEncoder pEncoder;

    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(summary = "Listar todos los usuarios")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios (formato 'slim')")
    // --- Fin de la Documentación ---
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list() {

        List<User> users = userBusiness.list();

        StdSerializer<User> userSerializer = new UserSlimV1JsonSerializer(User.class, false);
        ObjectMapper mapper = JsonUtils.getObjectMapper(User.class, userSerializer, null);

        List<Object> serializedUsers = users.stream()
                .map(user -> {
                    try {
                        return mapper.valueToTree(user);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al serializar el objeto User", e);
                    }
                }).toList();

        return new ResponseEntity<>(serializedUsers, HttpStatus.OK);
    }


    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(summary = "Obtener usuario por ID")
    @Parameter(name = "id", description = "ID del usuario", in = ParameterIn.PATH, required = true)
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    // --- Fin de la Documentación ---
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loadUser(@PathVariable Long id) {
        return new ResponseEntity<>(userBusiness.load(id), HttpStatus.OK);
    }

    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(summary = "Obtener usuario por Username")
    @Parameter(name = "user", description = "Nombre de usuario", in = ParameterIn.PATH, required = true)
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    // --- Fin de la Documentación ---
    @GetMapping(value = "/name/{user}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loadUser(@PathVariable String user) {
        return new ResponseEntity<>(userBusiness.load(user), HttpStatus.OK);
    }

    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(summary = "Crear un nuevo usuario")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "409", description = "Conflicto (ej: username ya existe)")
    // --- Fin de la Documentación ---
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> add(@Valid @RequestBody User user) {

        user.setPassword(pEncoder.encode(user.getPassword()));
        User response = userBusiness.add(user);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Location", Constants.URL_USERS + "/" + response.getIdUser());
        //return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        return new ResponseEntity<>(response, responseHeaders, HttpStatus.CREATED);
    }

    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(summary = "Actualizar un usuario existente")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @ApiResponse(responseCode = "409", description = "Conflicto (ej: username duplicado)")
    // --- Fin de la Documentación ---
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@Valid @RequestBody User user) {
        userBusiness.update(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @SneakyThrows
    // --- Documentación de Swagger ---
    @Operation(summary = "Eliminar un usuario por ID")
    @Parameter(name = "id", description = "ID del usuario a eliminar", in = ParameterIn.PATH, required = true)
    @ApiResponse(responseCode = "200", description = "Usuario eliminado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    // --- Fin de la Documentación ---
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> delete(@PathVariable Long id) {
        userBusiness.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}