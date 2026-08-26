package com.example.EstoqueFacil.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infraestrutura de mensageria para o sistema de notificações.
 *
 * Topologia:
 *
 *   estoque.notificacoes.exchange (topic)
 *       ├── estoque.baixo   -> estoque.baixo.queue
 *       ├── lote.vencido    -> lote.vencido.queue
 *       └── produto.parado  -> produto.parado.queue
 *
 * Cada fila de negócio está configurada com x-dead-letter-exchange
 * apontando para a estoque.notificacoes.dlx (fanout). Quando o consumer
 * dá NACK sem requeue, a mensagem cai automaticamente na
 * estoque.notificacoes.dlq, de onde pode ser inspecionada/reprocessada
 * manualmente.
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICACAO_EXCHANGE = "estoque.notificacoes.exchange";

    public static final String DLX_EXCHANGE = "estoque.notificacoes.dlx";
    public static final String DLQ_QUEUE = "estoque.notificacoes.dlq";

    public static final String ESTOQUE_BAIXO_QUEUE = "estoque.baixo.queue";
    public static final String LOTE_VENCIDO_QUEUE = "lote.vencido.queue";
    public static final String PRODUTO_PARADO_QUEUE = "produto.parado.queue";

    public static final String ESTOQUE_BAIXO_ROUTING_KEY = "estoque.baixo";
    public static final String LOTE_VENCIDO_ROUTING_KEY = "lote.vencido";
    public static final String PRODUTO_PARADO_ROUTING_KEY = "produto.parado";

    // ---------- Exchanges ----------

    @Bean
    public TopicExchange notificacaoExchange() {
        return ExchangeBuilder.topicExchange(NOTIFICACAO_EXCHANGE).durable(true).build();
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return ExchangeBuilder.fanoutExchange(DLX_EXCHANGE).durable(true).build();
    }

    // ---------- Dead-letter queue (compartilhada pelos 3 tipos de evento) ----------

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, FanoutExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange);
    }

    // ---------- Filas de negócio ----------

    @Bean
    public Queue estoqueBaixoQueue() {
        return QueueBuilder.durable(ESTOQUE_BAIXO_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .build();
    }

    @Bean
    public Queue loteVencidoQueue() {
        return QueueBuilder.durable(LOTE_VENCIDO_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .build();
    }

    @Bean
    public Queue produtoParadoQueue() {
        return QueueBuilder.durable(PRODUTO_PARADO_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .build();
    }

    @Bean
    public Binding estoqueBaixoBinding(Queue estoqueBaixoQueue, TopicExchange notificacaoExchange) {
        return BindingBuilder.bind(estoqueBaixoQueue).to(notificacaoExchange).with(ESTOQUE_BAIXO_ROUTING_KEY);
    }

    @Bean
    public Binding loteVencidoBinding(Queue loteVencidoQueue, TopicExchange notificacaoExchange) {
        return BindingBuilder.bind(loteVencidoQueue).to(notificacaoExchange).with(LOTE_VENCIDO_ROUTING_KEY);
    }

    @Bean
    public Binding produtoParadoBinding(Queue produtoParadoQueue, TopicExchange notificacaoExchange) {
        return BindingBuilder.bind(produtoParadoQueue).to(notificacaoExchange).with(PRODUTO_PARADO_ROUTING_KEY);
    }

    // ---------- Serialização JSON ----------

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);

        // mandatory + confirm/return callbacks: sem isso, uma falha de
        // publish (ex.: routing key sem fila) falha silenciosamente.
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("RabbitMQ recusou confirmar a publicação. correlationData: {}, causa: {}",
                        correlationData, cause);
            }
        });
        template.setReturnsCallback(returned -> log.error(
                "Mensagem não roteada para nenhuma fila - exchange: {}, routingKey: {}, replyCode: {}, replyText: {}",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode(), returned.getReplyText()));

        return template;
    }

    // ---------- Listener container: ACK manual ----------

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        factory.setPrefetchCount(10);
        return factory;
    }
}