package com.example.EstoqueFacil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender);
        ReflectionTestUtils.setField(emailService, "remetente", "no-reply@estoquefacil.com");
    }

    @Test
    void enviaEmailComOsDadosCorretos() {
        emailService.enviarEmail("alertas@estoquefacil.com", "Assunto", "Corpo");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage mensagem = captor.getValue();
        assertThat(mensagem.getTo()).containsExactly("alertas@estoquefacil.com");
        assertThat(mensagem.getSubject()).isEqualTo("Assunto");
        assertThat(mensagem.getText()).isEqualTo("Corpo");
        assertThat(mensagem.getFrom()).isEqualTo("no-reply@estoquefacil.com");
    }

    @Test
    void propagaExcecaoQuandoEnvioFalha() {
        doThrow(new MailSendException("smtp indisponível"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.enviarEmail("x@x.com", "Assunto", "Corpo"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao enviar e-mail");
    }
}