package com.sementelivre.backend.service;

import com.sementelivre.backend.dto.*;
import com.sementelivre.backend.entity.*;
import com.sementelivre.backend.entity.enums.PerfilEnum;
import com.sementelivre.backend.repository.*;
import com.sementelivre.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenRecuperacaoSenhaRepository tokenRecuperacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${jwt.refresh-expiration-days:7}")
    private Long refreshExpirationDays;

    public AuthService(UsuarioRepository usuarioRepository, RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository, TokenRecuperacaoSenhaRepository tokenRecuperacaoRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtService jwtService, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenRecuperacaoRepository = tokenRecuperacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @Transactional
    public UsuarioResponseDTO cadastrar(CadastroRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema.");
        }

        Role roleUsuario = roleRepository.findByNome(PerfilEnum.ROLE_USUARIO)
                .orElseThrow(() -> new IllegalStateException("Role ROLE_USUARIO não encontrada."));

        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha())) // Criptografia segura com BCrypt
                .ativo(true)
                .build();
        usuario.getRoles().add(roleUsuario);

        Usuario salvo = usuarioRepository.save(usuario);
        return UsuarioResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public TokenResponseDTO login(LoginRequestDTO dto) {
        var authToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());
        var authentication = authenticationManager.authenticate(authToken);

        Usuario usuario = (Usuario) authentication.getPrincipal();

        String accessToken = jwtService.gerarToken(usuario);
        RefreshToken refreshToken = criarRefreshToken(usuario);

        return new TokenResponseDTO(accessToken, refreshToken.getToken(), 1800L);
    }

    @Transactional
    public TokenResponseDTO refreshToken(RefreshTokenRequestDTO dto) {
        RefreshToken tokenSalvo = refreshTokenRepository.findByToken(dto.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token não encontrado."));

        if (tokenSalvo.getRevogado() || tokenSalvo.getDataExpiracao().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expirado ou revogado.");
        }

        Usuario usuario = tokenSalvo.getUsuario();
        
        // Revoga o token atual (Refresh Token Rotation)
        tokenSalvo.setRevogado(true);
        refreshTokenRepository.save(tokenSalvo);

        // Gera novo par de tokens
        String novoAccessToken = jwtService.gerarToken(usuario);
        RefreshToken novoRefreshToken = criarRefreshToken(usuario);

        return new TokenResponseDTO(novoAccessToken, novoRefreshToken.getToken(), 1800L);
    }

    @Transactional
    public void solicitarRecuperacaoSenha(SolicitarRecuperacaoDTO dto) {
        var usuarioOpt = usuarioRepository.findByEmail(dto.email());
        if (usuarioOpt.isEmpty()) {
            return; // Retorno silencioso por segurança
        }

        Usuario usuario = usuarioOpt.get();
        String token = UUID.randomUUID().toString();

        TokenRecuperacaoSenha tokenEntity = TokenRecuperacaoSenha.builder()
                .token(token)
                .usuario(usuario)
                .dataExpiracao(Instant.now().plus(15, ChronoUnit.MINUTES))
                .usado(false)
                .build();

        tokenRecuperacaoRepository.save(tokenEntity);
        emailService.enviarEmailRecuperacaoSenha(usuario.getEmail(), token);
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaDTO dto) {
        TokenRecuperacaoSenha tokenEntity = tokenRecuperacaoRepository.findByToken(dto.token())
                .orElseThrow(() -> new IllegalArgumentException("Token de recuperação inválido."));

        if (tokenEntity.getUsado() || tokenEntity.getDataExpiracao().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token de recuperação expirado ou já utilizado.");
        }

        Usuario usuario = tokenEntity.getUsuario();
        usuario.setSenha(passwordEncoder.encode(dto.novaSenha())); // Atualiza a senha no banco com BCrypt
        usuarioRepository.save(usuario);

        tokenEntity.setUsado(true);
        tokenRecuperacaoRepository.save(tokenEntity);
    }

    private RefreshToken criarRefreshToken(Usuario usuario) {
        refreshTokenRepository.deleteByUsuario(usuario);

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(UUID.randomUUID().toString())
                .dataExpiracao(Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS))
                .revogado(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
