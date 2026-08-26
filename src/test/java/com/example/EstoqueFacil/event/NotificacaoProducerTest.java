package com.example.EstoqueFacil.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.EstoqueFacil.config.RabbitMQConfig.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacaoProducer")
class NotificacaoProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private NotificacaoProducer producer;

    @BeforeEach
    void setUp() {
        producer = new NotificacaoProducer(rabbitTemplate);
    }

    @Test
    @DisplayName("publicarEstoqueBaixo envia para o exchange e routing key corretos")
    void publicarEstoqueBaixoUsaExchangeERoutingKeyCorretos() {
        LowStockEvent evento = new LowStockEvent(1L, "Produto X", 5, 20, LocalDateTime.now());

        producer.publicarEstoqueBaixo(evento);

        verify(rabbitTemplate).convertAndSend(NOTIFICACAO_EXCHANGE, ESTOQUE_BAIXO_ROUTING_KEY, evento);
    }

    @Test
    @DisplayName("publicarLoteVencido envia para o exchange e routing key corretos")
    void publicarLoteVencidoUsaExchangeERoutingKeyCorretos() {
        BatchExpiredEvent evento = new BatchExpiredEvent(2L, "Produto Y", 99L, LocalDate.now(), 10, LocalDateTime.now());

        producer.publicarLoteVencido(evento);

        verify(rabbitTemplate).convertAndSend(NOTIFICACAO_EXCHANGE, LOTE_VENCIDO_ROUTING_KEY, evento);
    }

    @Test
    @DisplayName("publicarProdutoParado envia para o exchange e routing key corretos")
    void publicarProdutoParadoUsaExchangeERoutingKeyCorretos() {
        ProductStopEvent evento = new ProductStopEvent(3L, "Produto Z", 45L, 19.9, LocalDateTime.now());

        producer.publicarProdutoParado(evento);

        verify(rabbitTemplate).convertAndSend(NOTIFICACAO_EXCHANGE, PRODUTO_PARADO_ROUTING_KEY, evento);
    }

    @Test
    @DisplayName("não lança exceção quando o RabbitTemplate falha ao publicar")
    void naoLancaExcecaoQuandoRabbitTemplateFalha() {
        LowStockEvent evento = new LowStockEvent(1L, "Produto X", 5, 20, LocalDateTime.now());
        doThrow(new RuntimeException("broker fora do ar"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // não deve lançar — o producer captura e loga, quem chamou nunca vê a exceção
        producer.publicarEstoqueBaixo(evento);

        verify(rabbitTemplate).convertAndSend(NOTIFICACAO_EXCHANGE, ESTOQUE_BAIXO_ROUTING_KEY, evento);
    }
}