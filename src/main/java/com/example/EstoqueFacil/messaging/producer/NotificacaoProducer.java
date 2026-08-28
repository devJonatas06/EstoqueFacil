package com.example.EstoqueFacil.messaging.producer;


import com.example.EstoqueFacil.config.RabbitMQConfig;
import com.example.EstoqueFacil.messaging.event.BatchExpiredEvent;
import com.example.EstoqueFacil.messaging.event.LowStockEvent;
import com.example.EstoqueFacil.messaging.event.ProductStopEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacaoProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publicarEstoqueBaixo(LowStockEvent evento) {
        publicar(RabbitMQConfig.ESTOQUE_BAIXO_ROUTING_KEY, evento, "estoque baixo",
                evento.getProdutoId(), evento.getNomeProduto());
    }

    public void publicarLoteVencido(BatchExpiredEvent evento) {
        publicar(RabbitMQConfig.LOTE_VENCIDO_ROUTING_KEY, evento, "lote vencido",
                evento.getProdutoId(), evento.getNomeProduto());
    }

    public void publicarProdutoParado(ProductStopEvent evento) {
        publicar(RabbitMQConfig.PRODUTO_PARADO_ROUTING_KEY, evento, "produto parado",
                evento.getProdutoId(), evento.getNomeProduto());
    }

    private void publicar(String routingKey, Object evento, String tipoEvento, Long produtoId, String nomeProduto) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICACAO_EXCHANGE, routingKey, evento);
            log.info("Evento publicado - tipo: '{}', produtoId: {}, produto: '{}', routingKey: '{}'",
                    tipoEvento, produtoId, nomeProduto, routingKey);
        } catch (Exception e) {
            log.error("Falha ao publicar evento de '{}' para produtoId: {}, produto: '{}'. Erro: {}",
                    tipoEvento, produtoId, nomeProduto, e.getMessage(), e);
        }
    }
}

