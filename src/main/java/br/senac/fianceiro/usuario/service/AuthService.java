package br.senac.fianceiro.usuario.service;

import br.senac.fianceiro.exception.BusinessException;
import br.senac.fianceiro.security.JwtTokenService;
import br.senac.fianceiro.usuario.dto.AuthResponse;
import br.senac.fianceiro.usuario.dto.LoginRequest;
import br.senac.fianceiro.usuario.dto.RegistroUsuarioRequest;
import br.senac.fianceiro.usuario.dto.UsuarioResponse;
import br.senac.fianceiro.usuario.entity.Usuario;
import br.senac.fianceiro.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResponse registrar(RegistroUsuarioRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome().trim())
                .email(emailNormalizado)
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .ativo(true)
                .build();

        usuario = usuarioRepository.save(usuario);

        String token = jwtTokenService.gerarToken(usuario.getId(), usuario.getEmail(), usuario.getNome());

        return AuthResponse.builder()
                .token(token)
                .tipoToken("Bearer")
                .usuario(converterParaResponse(usuario))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos."));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenhaHash())) {
            throw new BusinessException("E-mail ou senha inválidos.");
        }

        if (!usuario.isAtivo()) {
            throw new BusinessException("Usuário inativo. Contate o suporte.");
        }

        String token = jwtTokenService.gerarToken(usuario.getId(), usuario.getEmail(), usuario.getNome());

        return AuthResponse.builder()
                .token(token)
                .tipoToken("Bearer")
                .usuario(converterParaResponse(usuario))
                .build();
    }

    public UsuarioResponse converterParaResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .ativo(usuario.isAtivo())
                .dataCriacao(usuario.getDataCriacao())
                .build();
    }
}
