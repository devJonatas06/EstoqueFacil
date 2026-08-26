package com.example.EstoqueFacil.event;

import com.example.EstoqueFacil.config.RabbitMQConfig;
import com.example.EstoqueFacil.service.EmailService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consome os eventos de notificação e aciona o envio de e-mail.
 *
 * ACK manual: cada listener recebe o {@link Channel} e o deliveryTag da
 * mensagem. Em caso de sucesso, confirma com basicAck. Em caso de erro
 * (ex.: falha no envio do e-mail), rejeita com
 * basicNack(deliveryTag, false, false) — o "false, false" final significa
 * "não processar em lote" e "não recolocar na fila", o que faz a mensagem
 * ir direto para a estoque.notificacoes.dlq (configurada via
 * x-dead-letter-exchange na fila de origem).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacaoConsumer {

    private static final String EMAIL_DESTINATARIO_ALERTAS = "alertas@estoquefacil.com";

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.ESTOQUE_BAIXO_QUEUE)
    public void receberEstoqueBaixo(LowStockEvent evento, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("Consumindo evento de estoque baixo - produtoId: {}, produto: '{}', atual: {}, mínimo: {}",
                evento.getProdutoId(), evento.getNomeProduto(), evento.getQuantidadeAtual(), evento.getQuantidadeMinima());

        processar(channel, deliveryTag, () -> {
            String assunto = "Alerta de estoque baixo: " + evento.getNomeProduto();
            String corpo = String.format(
                    "O produto '%s' está com estoque abaixo do mínimo.%nAtual: %d | Mínimo: %d%nGerado em: %s",
                    evento.getNomeProduto(), evento.getQuantidadeAtual(), evento.getQuantidadeMinima(), evento.getDataEvento());
            emailService.enviarEmail(EMAIL_DESTINATARIO_ALERTAS, assunto, corpo);
        });
    }

    @RabbitListener(queues = RabbitMQConfig.LOTE_VENCIDO_QUEUE)
    public void receberLoteVencido(BatchExpiredEvent evento, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("Consumindo evento de lote vencido - produtoId: {}, produto: '{}', loteId: {}, validade: {}",
                evento.getProdutoId(), evento.getNomeProduto(), evento.getLoteId(), evento.getDataValidade());

        processar(channel, deliveryTag, () -> {
            String assunto = "Alerta de lote vencido: " + evento.getNomeProduto();
            String corpo = String.format(
                    "O lote %d do produto '%s' venceu em %s.%nQuantidade: %d",
                    evento.getLoteId(), evento.getNomeProduto(), evento.getDataValidade(), evento.getQuantidade());
            emailService.enviarEmail(EMAIL_DESTINATARIO_ALERTAS, assunto, corpo);
        });
    }

    @RabbitListener(queues = RabbitMQConfig.PRODUTO_PARADO_QUEUE)
    public void receberProdutoParado(ProductStopEvent evento, Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("Consumindo evento de produto parado - produtoId: {}, produto: '{}', dias sem venda: {}",
                evento.getProdutoId(), evento.getNomeProduto(), evento.getDiasSemVenda());

        processar(channel, deliveryTag, () -> {
            String assunto = "Produto parado: " + evento.getNomeProduto();
            String corpo = String.format(
                    "O produto '%s' está sem vendas há %d dias. Último preço de venda: %s",
                    evento.getNomeProduto(), evento.getDiasSemVenda(), evento.getUltimoPrecoVenda());
            emailService.enviarEmail(EMAIL_DESTINATARIO_ALERTAS, assunto, corpo);
        });
    }

    private void processar(Channel channel, long deliveryTag, Runnable acao) {
        try {
            acao.run();
            channel.basicAck(deliveryTag, false);
            log.debug("Mensagem confirmada (ACK) - deliveryTag: {}", deliveryTag);
        } catch (Exception e) {
            log.error("Erro ao processar mensagem (deliveryTag: {}). Enviando para DLQ. Erro: {}",
                    deliveryTag, e.getMessage(), e);
            nack(channel, deliveryTag);
        }
    }

    private void nack(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException ioException) {
            log.error("Falha ao enviar NACK para deliveryTag: {}. Erro: {}",
                    deliveryTag, ioException.getMessage(), ioException);
        }
    }
}