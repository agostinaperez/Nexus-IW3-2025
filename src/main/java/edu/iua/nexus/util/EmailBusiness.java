package edu.iua.nexus.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import edu.iua.nexus.model.business.BusinessException;
import lombok.extern.slf4j.Slf4j;

/**
 * Fachada simple sobre {@link JavaMailSender} para centralizar el envio de correos desde la
 * aplicacion. La clase se mantiene enfocada en un unico metodo publico para construir y enviar
 * mensajes de texto plano, propagando cualquier error como {@link BusinessException} para que
 * la capa superior decida como reaccionar.
 *
 * Las credenciales y la direccion de origen se obtienen desde {@code application.properties},
 * usando la propiedad {@code mail.from} y su fallback {@code spring.mail.username}.
 */
@Component
@Slf4j
public class EmailBusiness {

    @Autowired
    private JavaMailSender emailSender;

    @Value("${mail.from:${spring.mail.username}}")
    private String from;

    /**
     * Envia un correo de texto plano.
     *
     * @param to destinatario
     * @param subject asunto que se vera en el cliente de mail
     * @param text cuerpo del mensaje en texto plano
     * @throws BusinessException si ocurre un problema al construir o enviar el correo
     */
    public void sendSimpleMessage(String to, String subject, String text) throws BusinessException {
        log.trace("Enviando mail subject={} a: {}", subject, to);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

}
