package edu.iua.nexus.auth.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import edu.iua.nexus.auth.util.UserSlimV1Response;
import edu.iua.nexus.util.JsonUtils;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import edu.iua.nexus.auth.custom.CustomAuthenticationManager;
import edu.iua.nexus.auth.filters.AuthConstants;
import edu.iua.nexus.auth.model.User;
import edu.iua.nexus.auth.model.serializers.UserSlimV1JsonSerializer;
import edu.iua.nexus.controllers.BaseRestController;
import edu.iua.nexus.Constants;
import edu.iua.nexus.util.IStandartResponseBusiness;

@RestController
public class AuthRestController extends BaseRestController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private IStandartResponseBusiness response;

    @Autowired
    private PasswordEncoder pEncoder;

    /**
     * Punto de acceso diseñado para integraciones externas que únicamente requieren un JWT (JSON WEB TOKEN).
     */
    @PostMapping(value = Constants.URL_EXTERNAL_LOGIN, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> loginExternalOnlyToken(@RequestParam String username, @RequestParam String password) {
        Authentication auth = null;

        try {
            auth = authManager.authenticate(((CustomAuthenticationManager) authManager).authWrap(username, password));
        } catch (AuthenticationServiceException e0) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e0, e0.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(response.build(HttpStatus.UNAUTHORIZED, e, e.getMessage()),
                    HttpStatus.UNAUTHORIZED);
        }

        // en principal tenemos la version simplificada de User
        User user = (User) auth.getPrincipal();
        String token = JWT.create().withSubject(user.getUsername())
                .withClaim("internalid", user.getIdUser())
                .withClaim("roles", new ArrayList<String>(user.getAuthoritiesStr()))
                .withClaim("email", user.getEmail())
                .withClaim("version", "1.0.0")
                .withExpiresAt(new Date(System.currentTimeMillis() + AuthConstants.EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(AuthConstants.SECRET.getBytes()));
        return new ResponseEntity<String>(token, HttpStatus.OK);
    }

    @SneakyThrows
    /**
     * Endpoint usado por la UI interna: devuelve el token y una representación ligera del usuario
     * para poblar el store del front sin exponer datos sensibles.
     */
    @PostMapping(value = Constants.URL_INTERNAL_LOGIN, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginInternal(@RequestBody User user) {
        Authentication auth = null;

        try {
            auth = authManager.authenticate(((CustomAuthenticationManager) authManager).authWrap(user.getUsername(), user.getPassword()));
        } catch (AuthenticationServiceException e0) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e0, e0.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(response.build(HttpStatus.UNAUTHORIZED, e, e.getMessage()),
                    HttpStatus.UNAUTHORIZED);
        }

        // en principal tenemos la version simplificada de User
        User newUser = (User) auth.getPrincipal();
        String token = JWT.create().withSubject(newUser.getUsername())
                .withClaim("internalid", newUser.getIdUser())
                .withClaim("roles", new ArrayList<String>(newUser.getAuthoritiesStr()))
                .withClaim("email", newUser.getEmail())
                .withClaim("version", "1.0.0")
                .withExpiresAt(new Date(System.currentTimeMillis() + AuthConstants.EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(AuthConstants.SECRET.getBytes()));

        // Crear la respuesta simplificada del usuario
        UserSlimV1Response userResponse = new UserSlimV1Response(
                newUser.getUsername(),
                newUser.getEmail(),
                newUser.getAuthoritiesStr()
        );

        // Devolver el token y el usuario simplificado en un Map
        Map<String, Object> response = Map.of(
                "token", token,
                "user", userResponse
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @SneakyThrows
    /**
     * Permite a un cliente validar el token vigente y recuperar sus propios datos en formato slim.
     */
    @GetMapping(value = Constants.URL_TOKEN_VALIDATE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> validateToken() {
        User user = getUserLogged();
        StdSerializer<User> ser = new UserSlimV1JsonSerializer(User.class, false);
        String result = JsonUtils.getObjectMapper(User.class, ser, null).writeValueAsString(user);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/demo/encodepass", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> encodepass(@RequestParam String password) {
        try {
            return new ResponseEntity<String>(pEncoder.encode(password), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}