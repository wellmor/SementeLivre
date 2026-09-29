package com.sementelivre.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import com.jayway.jsonpath.JsonPath;
import com.sementelivre.backend.BackendApplication;
import com.sementelivre.backend.entity.Admin;
import com.sementelivre.backend.entity.enums.PerfilEnum;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import com.sementelivre.backend.repository.PessoaRepository;
import com.sementelivre.backend.service.UsuarioService;

/**
 * Base dos testes de autenticacao/autorizacao que passam pela cadeia de seguranca de
 * verdade (MockMvc + Postgres do Testcontainers). Cada teste cria suas proprias contas
 * (e-mail/CPF/RG unicos), porque as requisicoes do MockMvc commitam no banco compartilhado.
 */
@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
abstract class AbstractAuthIntegrationTest extends AbstractPostgresIntegrationTest {

    protected static final String SENHA = "Senha1234";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    protected record Conta(String id, String email, String documento, String rg) {}

    protected record Tokens(String accessToken, String refreshToken) {

        String bearer() {
            return "Bearer " + accessToken;
        }
    }

    /** Gera um CPF com digitos verificadores validos. */
    protected static String cpfValido() {
        int[] d = new int[11];
        for (int i = 0; i < 9; i++) {
            d[i] = ThreadLocalRandom.current().nextInt(10);
        }
        d[0] = 1 + ThreadLocalRandom.current().nextInt(9); // evita todos os digitos iguais
        for (int pos = 9; pos < 11; pos++) {
            int soma = 0;
            for (int i = 0; i < pos; i++) {
                soma += d[i] * (pos + 1 - i);
            }
            int resto = soma % 11;
            d[pos] = resto < 2 ? 0 : 11 - resto;
        }
        StringBuilder cpf = new StringBuilder();
        for (int digito : d) {
            cpf.append(digito);
        }
        return cpf.toString();
    }

    protected static String sufixoUnico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    protected static String cadastroJson(String email, String documento, String rg) {
        return """
                {
                  "nome": "Proprietario Perfil",
                  "tipoDocumento": "CPF",
                  "documento": "%s",
                  "rg": "%s",
                  "telefone": "32999998888",
                  "email": "%s",
                  "senha": "%s",
                  "endereco": {
                    "logradouro": "Rua das Sementes",
                    "numero": "10",
                    "bairro": "Centro",
                    "municipio": "Rio Pomba",
                    "uf": "MG",
                    "cep": "36180000"
                  }
                }
                """.formatted(documento, rg, email, SENHA);
    }

    protected static String atualizacaoJson(String nome, String email) {
        return """
                {
                  "nome": "%s",
                  "telefone": "32988887777",
                  "email": "%s",
                  "endereco": {
                    "logradouro": "Rua Nova",
                    "numero": "20",
                    "municipio": "Mercês",
                    "uf": "MG",
                    "cep": "36190000"
                  }
                }
                """.formatted(nome, email);
    }

    protected static String loginJson(String email, String senha) {
        return "{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha);
    }

    /** Cadastra um proprietario pelo POST /auth/cadastrar. */
    protected Conta cadastrar() throws Exception {
        String sufixo = sufixoUnico();
        String email = "perfil-" + sufixo + "@teste.com";
        String documento = cpfValido();
        String rg = "MG-" + sufixo;

        String resposta = mockMvc.perform(post("/auth/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson(email, documento, rg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return new Conta(JsonPath.read(resposta, "$.id"), email, documento, rg);
    }

    /** Cria um admin com conta de login (o admin do seed nao tem senha conhecida) e devolve o e-mail. */
    protected String criarAdmin() {
        String email = "admin-" + sufixoUnico() + "@teste.com";
        transactionTemplate.executeWithoutResult(status -> {
            Admin admin = new Admin();
            admin.setTipoDocumento(TipoDocumento.CPF);
            admin.setDocumento(cpfValido());
            admin.setNome("Admin Teste");
            admin.setEmail(email);
            usuarioService.criarConta(pessoaRepository.save(admin), SENHA, PerfilEnum.ROLE_ADMIN);
        });
        return email;
    }

    protected Tokens loginTokens(String email, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new Tokens(JsonPath.read(resposta, "$.accessToken"), JsonPath.read(resposta, "$.refreshToken"));
    }

    /** Faz login e devolve o valor do header Authorization ("Bearer ..."). */
    protected String login(String email, String senha) throws Exception {
        return loginTokens(email, senha).bearer();
    }

    protected String loginAdmin() throws Exception {
        return login(criarAdmin(), SENHA);
    }
}
