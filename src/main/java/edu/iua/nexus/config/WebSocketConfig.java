package edu.iua.nexus.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuracion central de WebSocket/STOMP para la aplicacion. Se habilita un broker de mensajes
 * en memoria y se expone un unico endpoint para que los clientes negocien la conexion WebSocket.
 * Las rutas de suscripcion utilizan el prefijo {@code /topic}, mientras que los productores envian
 * mensajes a los destinos definidos en los controladores mediante {@code @MessageMapping}.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Broker simple en memoria; los clientes se suscriben con prefijo /topic
        config.enableSimpleBroker("/topic");

        // Si en un futuro se agregan destinos de aplicacion, descomentar para exigir el prefijo /ws
        // config.setApplicationDestinationPrefixes("/ws");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Punto de handshake usado por los clientes STOMP para levantar la conexion WebSocket
        registry.addEndpoint("/notifier").setAllowedOrigins("http://localhost:5173");
    }

}
