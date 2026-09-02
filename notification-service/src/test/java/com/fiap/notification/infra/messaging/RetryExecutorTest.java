package com.fiap.notification.infra.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryExecutorTest {

    // intervalos curtos para teste rápido
    private RetryExecutor executor(int maxAttempts) {
        return new RetryExecutor(maxAttempts, 1L, 2.0);
    }

    @Test
    @DisplayName("Sucesso na 1ª tentativa: executa 1 vez e retorna true")
    void successFirstAttempt() {
        AtomicInteger calls = new AtomicInteger();
        boolean ok = executor(3).execute(calls::incrementAndGet);

        assertTrue(ok);
        assertEquals(1, calls.get());
    }

    @Test
    @DisplayName("Sucesso na 3ª tentativa: retorna true após 3 execuções")
    void successThirdAttempt() {
        AtomicInteger calls = new AtomicInteger();
        boolean ok = executor(3).execute(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new RuntimeException("falha " + calls.get());
            }
        });

        assertTrue(ok);
        assertEquals(3, calls.get());
    }

    @Test
    @DisplayName("Falha sempre: 3 tentativas e retorna false (vai para DLQ)")
    void allAttemptsFail() {
        AtomicInteger calls = new AtomicInteger();
        boolean ok = executor(3).execute(() -> {
            calls.incrementAndGet();
            throw new RuntimeException("sempre falha");
        });

        assertFalse(ok);
        assertEquals(3, calls.get());
    }
}
