package com.fiap.notification.infra.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Executa uma ação com política de retry: N tentativas com backoff exponencial.
 * Isolado do listener para ser testável e reutilizável.
 *
 * Ex.: maxAttempts=3, initial=1000ms, multiplier=2 → esperas de 1s, 2s antes
 * da 2ª e 3ª tentativas (a 1ª é imediata). Se todas falharem, relança a
 * última exceção para o chamador decidir (ex.: enviar à DLQ).
 */
@Component
public class RetryExecutor {

    private static final Logger log = LoggerFactory.getLogger(RetryExecutor.class);

    private final int maxAttempts;
    private final long initialIntervalMs;
    private final double multiplier;

    public RetryExecutor(
            @Value("${app.notification.retry.max-attempts:3}") int maxAttempts,
            @Value("${app.notification.retry.initial-interval-ms:1000}") long initialIntervalMs,
            @Value("${app.notification.retry.multiplier:2.0}") double multiplier) {
        this.maxAttempts = maxAttempts;
        this.initialIntervalMs = initialIntervalMs;
        this.multiplier = multiplier;
    }

    /**
     * @return true se a ação teve sucesso em alguma tentativa; false se esgotou as tentativas.
     */
    public boolean execute(Runnable action) {
        long interval = initialIntervalMs;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                action.run();
                return true;
            } catch (Exception e) {
                log.warn("Tentativa {}/{} falhou: {}", attempt, maxAttempts, e.getMessage());
                if (attempt == maxAttempts) {
                    log.error("Todas as {} tentativas falharam.", maxAttempts);
                    return false;
                }
                sleep(interval);
                interval = (long) (interval * multiplier);
            }
        }
        return false;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
