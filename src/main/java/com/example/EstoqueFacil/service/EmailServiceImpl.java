package com.example.EstoqueFacil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@estoquefacil.com}")
    private String remetente;

    @Override
    public void enviarEmail(String destinatario, String assunto, String corpo) {
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(remetente);
            mensagem.setTo(destinatario);
            mensagem.setSubject(assunto);
            mensagem.setText(corpo);

            mailSender.send(mensagem);
            log.info("E-mail enviado para {}", destinatario);
        } catch (MailException e) {
            // Propaga para que o NotificacaoConsumer dê NACK e a mensagem
            // vá para a DLQ — assim nenhuma notificação se perde
            // silenciosamente por indisponibilidade do SMTP.
            log.error("Falha ao enviar e-mail para {}. Assunto: '{}'. Erro: {}",
                    destinatario, assunto, e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar e-mail para " + destinatario, e);
        }
    }
}