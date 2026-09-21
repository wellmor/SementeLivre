package com.sementelivre.backend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS dos endpoints publicos (issue #91).
 *
 * O front-site roda em outra origem, entao sem isto o navegador bloqueia o
 * GET /produtores/** mesmo com a rota liberada no SecurityConfig.
 *
 * A CONFIRMAR: a origem real do front-site em homologacao e producao ainda nao
 * foi definida. O default abaixo cobre apenas o Next.js local
 * (http://localhost:3000); os ambientes reais devem sobrescrever via
 * app.cors.allowed-origins (lista separada por virgula), por exemplo numa
 * variavel de ambiente APP_CORS_ALLOWED_ORIGINS.
 *
 * Duas decisoes de seguranca deliberadas:
 *  - origens sao sempre explicitas, nunca "*";
 *  - allowCredentials fica false, porque estes endpoints sao anonimos e nao
 *    leem cookie nem header Authorization. Se algum dia precisarem de
 *    credencial, a combinacao "*" + credentials continua proibida.
 *
 * O registro e restrito a /produtores/**: as demais rotas continuam sem
 * nenhuma politica de CORS, exatamente como estavam antes desta issue.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private List<String> origensPermitidas;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(origensPermitidas);
        configuracao.setAllowedMethods(List.of("GET", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));
        configuracao.setAllowCredentials(false);
        configuracao.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/produtores/**", configuracao);
        return fonte;
    }
}
