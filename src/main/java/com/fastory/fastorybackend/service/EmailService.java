package com.fastory.fastorybackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@lombok.RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void enviarCodigoRecuperacion(String destinatario, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinatario);
        message.setSubject("Código de Recuperación - La Espiga");
        message.setText("Hola,\n\n" +
                "Has solicitado restablecer tu contraseña en La Espiga.\n" +
                "Tu código de verificación es: " + codigo + "\n\n" +
                "Este código expira en 15 minutos.\n" +
                "Si no solicitaste esto, ignora este mensaje.\n\n" +
                "Saludos,\nEl equipo de La Espiga.");

        mailSender.send(message);
    }
}