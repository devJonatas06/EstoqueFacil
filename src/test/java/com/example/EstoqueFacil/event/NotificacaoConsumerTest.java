package com.example.EstoqueFacil.event;

import com.example.EstoqueFacil.service.EmailService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacaoConsumer")
class NotificacaoConsumerTest {

    private static final String DESTINATARIO_ALERTAS = "alertas@estoquefacil.com";

    @Mock
    private EmailService emailService;

    @Mock
    private Channel channel;

    private NotificacaoConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new NotificacaoConsumer(emailService);
    }

    @Nested
    class CaminhoFeliz {

        @Test
        @DisplayName("estoque baixo: envia e-mail e confirma com ACK")
        void receberEstoqueBaixoConfirmaComAck() throws IOException {
            LowStockEvent evento = new LowStockEvent(1L, "Produto X", 5, 20, LocalDateTime.now());

            consumer.receberEstoqueBaixo(evento, channel, 10L);

            verify(emailService).enviarEmail(eq(DESTINATARIO_ALERTAS), contains("Produto X"), anyString());
            verify(channel).basicAck(10L, false);
            verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
        }

        @Test
        @DisplayName("lote vencido: envia e-mail e confirma com ACK")
        void receberLoteVencidoConfirmaComAck() throws IOException {
            BatchExpiredEvent evento = new BatchExpiredEvent(
                    2L, "Produto Y", 99L, LocalDate.now().minusDays(3), 10, LocalDateTime.now());

            consumer.receberLoteVencido(evento, channel, 11L);

            verify(emailService).enviarEmail(eq(DESTINATARIO_ALERTAS), contains("Produto Y"), anyString());
            verify(channel).basicAck(11L, false);
        }

        @Test
        @DisplayName("produto parado: envia e-mail e confirma com ACK")
        void receberProdutoParadoConfirmaComAck() throws IOException {
            ProductStopEvent evento = new ProductStopEvent(3L, "Produto Z", 45L, 19.9, LocalDateTime.now());

            consumer.receberProdutoParado(evento, channel, 12L);

            verify(emailService).enviarEmail(eq(DESTINATARIO_ALERTAS), contains("Produto Z"), anyString());
            verify(channel).basicAck(12L, false);
        }
    }

    @Nested
    class CaminhoDeErro {

        @Test
        @DisplayName("quando o envio de e-mail falha, rejeita com NACK (sem requeue) em vez de ACK")
        void enviaParaDlqQuandoEmailFalha() throws IOException {
            LowStockEvent evento = new LowStockEvent(1L, "Produto X", 5, 20, LocalDateTime.now());
            doThrow(new RuntimeException("smtp indisponível"))
                    .when(emailService).enviarEmail(any(), any(), any());

            consumer.receberEstoqueBaixo(evento, channel, 10L);

            // multiple=false, requeue=false -> mensagem vai para a DLQ, não volta pra fila original
            verify(channel).basicNack(10L, false, false);
            verify(channel, never()).basicAck(anyLong(), anyBoolean());
        }

        @Test
        @DisplayName("se até o NACK falhar, apenas loga — não deixa a exceção escapar do listener")
        void naoLancaExcecaoQuandoNackTambemFalha() throws IOException {
            LowStockEvent evento = new LowStockEvent(1L, "Produto X", 5, 20, LocalDateTime.now());
            doThrow(new RuntimeException("falha no envio")).when(emailService).enviarEmail(any(), any(), any());
            doThrow(new IOException("canal fechado")).when(channel).basicNack(anyLong(), anyBoolean(), anyBoolean());

            // não deve lançar exceção para fora do listener, mesmo com o NACK falhando
            consumer.receberEstoqueBaixo(evento, channel, 10L);

            verify(channel).basicNack(10L, false, false);
        }
    }
}