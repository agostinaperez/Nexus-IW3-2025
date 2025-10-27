package edu.iua.nexus.auth.model.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import edu.iua.nexus.auth.model.Role;
import edu.iua.nexus.auth.model.User;

import java.io.IOException;

/**
 * Serializador Jackson que define exactamente qué campos del usuario se envían
 * cuando se valida el token. Evita exponer flags internos o hashes de contraseña.
 */
public class UserSlimV1JsonSerializer extends StdSerializer<User> {

    public UserSlimV1JsonSerializer(Class<?> t, boolean dummy) {
        super(t, dummy);
    }

    /**
     * Genera un JSON liviano con username, email y la lista de roles (solo sus nombres).
     */
    @Override
    public void serialize(User user, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("username", user.getUsername());
        jsonGenerator.writeStringField("email", user.getEmail());
        jsonGenerator.writeArrayFieldStart("roles");
        for (Role role : user.getRoles()) {
            jsonGenerator.writeString(role.getName());
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}