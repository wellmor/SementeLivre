package com.sementelivre.backend.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.sementelivre.backend.entity.Admin;
import com.sementelivre.backend.entity.Pessoa;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.entity.enums.NivelAcesso;
import com.sementelivre.backend.repository.PessoaRepository;
import com.sementelivre.backend.repository.UsuarioRepository;

/**
 * Senha do admin principal (SUPER_ADMIN do seed) vinda do ambiente.
 *
 * A V14 deixa esse admin sem senha (o hash do seed é público). Ao subir, se
 * ADMIN_INICIAL_SENHA estiver definida e o admin ainda estiver sem senha, ele
 * recebe essa senha e, se ADMIN_INICIAL_EMAIL vier, esse e-mail (um que a
 * equipe leia, para o "esqueci a senha" funcionar). Depois disso as variáveis
 * são ignoradas: trocar a senha pelo app não é desfeito no próximo deploy.
 */
@Component
public class AdminInicial implements ApplicationRunner {

    /** Marcador gravado pela V14: não é um hash BCrypt, então nunca confere com senha nenhuma. */
    public static final String SENHA_NAO_DEFINIDA = "SENHA_NAO_DEFINIDA";

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;
    private final String email;
    private final String senha;

    public AdminInicial(UsuarioRepository usuarioRepository, PessoaRepository pessoaRepository,
                        PasswordEncoder passwordEncoder, TransactionTemplate transactionTemplate,
                        @Value("${app.admin-inicial.email:}") String email,
                        @Value("${app.admin-inicial.senha:}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionTemplate = transactionTemplate;
        this.email = email == null ? "" : email.trim();
        this.senha = senha == null ? "" : senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> configurar());
    }

    void configurar() {
        Optional<Usuario> pendente = adminPrincipalSemSenha();
        if (pendente.isEmpty()) {
            return; // já tem senha (ou não existe): nada a fazer
        }
        Usuario usuario = pendente.get();

        if (senha.isBlank()) {
            log.warn("O admin principal ({}) ainda não tem senha. Defina ADMIN_INICIAL_SENHA "
                    + "(e ADMIN_INICIAL_EMAIL, se quiser outro e-mail) e reinicie o backend.", usuario.getEmail());
            return;
        }
        // Mesma regra da troca de senha (AlterarSenhaDTO). Configuração errada
        // impede o backend de subir, para não passar despercebida.
        if (senha.length() < 8 || senha.length() > 72) {
            throw new IllegalStateException("ADMIN_INICIAL_SENHA deve ter entre 8 e 72 caracteres.");
        }

        Pessoa pessoa = usuario.getPessoa();
        if (!email.isBlank() && !email.equalsIgnoreCase(pessoa.getEmail())) {
            if (!email.contains("@")) {
                throw new IllegalStateException("ADMIN_INICIAL_EMAIL não é um e-mail válido.");
            }
            if (pessoaRepository.existsByEmailIgnoreCase(Pessoa.normalizarEmail(email))) {
                throw new IllegalStateException("ADMIN_INICIAL_EMAIL já é usado por outra conta: " + email);
            }
            pessoa.setEmail(email);
            pessoaRepository.save(pessoa);
        }

        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
        log.info("Senha do admin principal definida a partir de ADMIN_INICIAL_SENHA. Login: {}", pessoa.getEmail());
    }

    private Optional<Usuario> adminPrincipalSemSenha() {
        return usuarioRepository.findBySenhaHash(SENHA_NAO_DEFINIDA).stream()
                .filter(u -> u.getPessoa() instanceof Admin admin
                        && admin.getNivelAcesso() == NivelAcesso.SUPER_ADMIN)
                .findFirst();
    }
}
