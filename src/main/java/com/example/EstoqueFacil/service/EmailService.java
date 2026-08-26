package com.example.EstoqueFacil.service;

public interface EmailService {

    /**
     * Envia um e-mail simples (texto puro). Lança exceção em caso de falha
     * — isso é intencional: o {@code NotificacaoConsumer} depende disso
     * para decidir entre ACK e NACK.
     */
    void enviarEmail(String destinatario, String assunto, String corpo);
}