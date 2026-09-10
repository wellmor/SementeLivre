package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sementeLivreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Semente Livre - API")
                        .description("API de gestão de comunidades rurais - IF Sudeste MG, Campus Rio Pomba")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("Equipe Semente Livre")
                                .url("https://github.com/wellmor/SementeLivre"))
                        .license(new License()
                                .name("Uso interno - Projeto acadêmico")));
    }
}
