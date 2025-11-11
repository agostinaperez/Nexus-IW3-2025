package edu.iua.nexus.controllers;

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

@RestController
@RequestMapping(Constants.URL_USERS)
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserRestController extends BaseRestController {

    @Autowired
    private IUserBusiness userBusiness;

    @Autowired
    private PasswordEncoder pEncoder;

    @SneakyThrows
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
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loadUser(@PathVariable Long id) {
        return new ResponseEntity<>(userBusiness.load(id), HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/name/{user}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loadUser(@PathVariable String user) {
        return new ResponseEntity<>(userBusiness.load(user), HttpStatus.OK);
    }

    @SneakyThrows
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
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@Valid @RequestBody User user) {
        userBusiness.update(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @SneakyThrows
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> delete(@PathVariable Long id) {
        userBusiness.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}