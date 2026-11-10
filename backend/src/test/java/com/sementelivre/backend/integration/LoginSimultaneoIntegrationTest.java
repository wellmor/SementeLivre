package com.sementelivre.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.jayway.jsonpath.JsonPath;

/**
 * Logins simultâneos da mesma conta (duplo clique em "Entrar", dois aparelhos ao
 * mesmo tempo). Cada login apaga o refresh token anterior da conta; quando dois
 * apagavam a mesma linha, o segundo recebia 500 (ObjectOptimisticLockingFailure).
 * Achado no teste de carga do #130.
 */
class LoginSimultaneoIntegrationTest extends AbstractAuthIntegrationTest {

    private static final int RODADAS = 15;
    private static final int LOGINS_POR_RODADA = 8;

    @Test
    void loginsSimultaneosDaMesmaContaNuncaDevolvem500() throws Exception {
        Conta conta = cadastrar();
        ExecutorService pool = Executors.newFixedThreadPool(LOGINS_POR_RODADA);
        List<Integer> statusRecebidos = new ArrayList<>();

        try {
            for (int rodada = 0; rodada < RODADAS; rodada++) {
                CountDownLatch largada = new CountDownLatch(1);
                List<Future<Integer>> logins = new ArrayList<>();
                for (int i = 0; i < LOGINS_POR_RODADA; i++) {
                    logins.add(pool.submit(() -> {
                        largada.await();
                        return mockMvc.perform(post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(loginJson(conta.email(), SENHA)))
                                .andReturn().getResponse().getStatus();
                    }));
                }
                largada.countDown();
                for (Future<Integer> login : logins) {
                    statusRecebidos.add(login.get(30, TimeUnit.SECONDS));
                }
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(statusRecebidos).hasSize(RODADAS * LOGINS_POR_RODADA).containsOnly(200);
    }

    @Test
    void depoisDeLoginsSimultaneosORefreshDoUltimoLoginContinuaValendo() throws Exception {
        Conta conta = cadastrar();
        ExecutorService pool = Executors.newFixedThreadPool(LOGINS_POR_RODADA);
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<Integer>> logins = new ArrayList<>();
            for (int i = 0; i < LOGINS_POR_RODADA; i++) {
                logins.add(pool.submit(() -> {
                    largada.await();
                    return mockMvc.perform(post("/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(loginJson(conta.email(), SENHA)))
                            .andReturn().getResponse().getStatus();
                }));
            }
            largada.countDown();
            for (Future<Integer> login : logins) {
                login.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        Tokens ultimo = loginTokens(conta.email(), SENHA);
        String resposta = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(ultimo.refreshToken())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat((String) JsonPath.read(resposta, "$.accessToken")).isNotBlank();
    }
}
