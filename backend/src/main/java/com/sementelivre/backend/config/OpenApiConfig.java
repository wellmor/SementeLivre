package com.sementelivre.backend.config;

import java.math.BigDecimal;

import org.springframework.context.annotation.Bean;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sementeLivreOpenAPI() {
        Components components = new Components()
                .addSchemas("ComunidadeDTO", comunidadeSchema())
                .addSchemas("PropriedadeDTO", propriedadeSchema())
                .addSchemas("UsuarioRequestDTO", usuarioRequestSchema())
                .addSchemas("UsuarioResponseDTO", usuarioResponseSchema())
                .addSchemas("StatusComunidade", statusComunidadeSchema());

        return new OpenAPI()
                .components(components)
                .info(new Info()
                        .title("Semente Livre - API")
                        .description("API de gestão de comunidades rurais do IF Sudeste MG, Campus Rio Pomba.")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Equipe Semente Livre")
                                .url("https://github.com/wellmor/SementeLivre")));
    }

    @Bean
    public GlobalOpenApiCustomizer dtoSchemasCustomizer() {
        return openAPI -> {
            Components components = openAPI.getComponents();
            if (components == null) {
                components = new Components();
                openAPI.setComponents(components);
            }
            components.addSchemas("ComunidadeDTO", comunidadeSchema())
                    .addSchemas("PropriedadeDTO", propriedadeSchema())
                    .addSchemas("UsuarioRequestDTO", usuarioRequestSchema())
                    .addSchemas("UsuarioResponseDTO", usuarioResponseSchema())
                    .addSchemas("StatusComunidade", statusComunidadeSchema());
        };
    }

    private Schema<Object> comunidadeSchema() {
        return new Schema<>()
                .type("object")
                .description("Dados de uma comunidade rural")
                .addProperties("id", uuidProperty("Identificador da comunidade"))
                .addProperties("nome", requiredStringProperty("Nome da comunidade", "Comunidade Boa Esperança"))
                .addProperties("status", new Schema<>()
                        .type("string")
                        .description("Situação da comunidade")
                        .example("ATIVA")
                        ._enum(java.util.List.of("ATIVA", "PENDENTE_APROVACAO", "REJEITADA")))
                .addProperties("dataSolicitacao", dateTimeProperty("Data da solicitação"))
                .addProperties("dataAprovacao", dateTimeProperty("Data da aprovação"))
                .addProperties("uf", requiredStringProperty("Unidade federativa do endereço", "MG"))
                .addProperties("municipio", requiredStringProperty("Município do endereço", "Rio Pomba"))
                .required(java.util.List.of("id", "nome", "status", "dataSolicitacao", "uf", "municipio"));
    }

    private Schema<Object> propriedadeSchema() {
        return new Schema<>()
                .type("object")
                .description("Dados de uma propriedade rural")
                .addProperties("id", uuidProperty("Identificador da propriedade"))
                .addProperties("nome", requiredStringProperty("Nome da propriedade", "Sítio Boa Vista"))
                .addProperties("tamanhoHectares", new Schema<>()
                        .type("number")
                        .format("double")
                        .description("Tamanho da propriedade em hectares")
                        .example(10.5)
                        .minimum(BigDecimal.ZERO))
                .addProperties("dataCadastro", dateTimeProperty("Data de cadastro"))
                .addProperties("dataUltimaAlteracao", dateTimeProperty("Data da última alteração"))
                .addProperties("proprietarioNome", requiredStringProperty("Nome do proprietário", "Maria da Silva"))
                .addProperties("comunidadeId", uuidProperty("Identificador da comunidade"))
                .addProperties("comunidadeNome", requiredStringProperty("Nome da comunidade", "Comunidade Boa Esperança"))
                .addProperties("logradouroUf", requiredStringProperty("UF do endereço", "MG"))
                .addProperties("logradouroMunicipio", requiredStringProperty("Município do endereço", "Rio Pomba"))
                .required(java.util.List.of("id", "nome", "dataCadastro", "dataUltimaAlteracao", "proprietarioNome",
                        "comunidadeId", "comunidadeNome", "logradouroUf", "logradouroMunicipio"));
    }

    private Schema<Object> usuarioRequestSchema() {
        return new Schema<>()
                .type("object")
                .description("Dados para cadastro ou atualização de usuário")
                .addProperties("nome", requiredStringProperty("Nome do usuário", "Maria da Silva"))
                .addProperties("email", requiredStringProperty("Email do usuário", "maria@example.com"))
                .required(java.util.List.of("nome", "email"));
    }

    private Schema<Object> usuarioResponseSchema() {
        return new Schema<>()
                .type("object")
                .description("Dados retornados de um usuário")
                .addProperties("id", uuidProperty("Identificador do usuário"))
                .addProperties("nome", new StringSchema()
                        .description("Nome do usuário")
                        .example("Maria da Silva")
                        .readOnly(true))
                .addProperties("email", new StringSchema()
                        .description("Email do usuário")
                        .example("maria@example.com")
                        .readOnly(true));
    }

    private Schema<Object> statusComunidadeSchema() {
        return new Schema<>()
                .type("string")
                .description("Valores possíveis para a situação da comunidade")
                ._enum(java.util.List.of("ATIVA", "PENDENTE_APROVACAO", "REJEITADA"));
    }

    private Schema<Object> uuidProperty(String description) {
        return new StringSchema()
                .format("uuid")
                .description(description)
                .example("550e8400-e29b-41d4-a716-446655440000");
    }

    private Schema<Object> dateTimeProperty(String description) {
        return new StringSchema()
                .format("date-time")
                .description(description)
                .example("2026-09-09T10:30:00");
    }

    private Schema<Object> requiredStringProperty(String description, String example) {
        return new StringSchema()
                .description(description)
                .example(example);
    }
}
