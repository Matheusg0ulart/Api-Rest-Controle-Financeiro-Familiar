package br.senac.fianceiro.familia.service;

import br.senac.fianceiro.exception.BusinessException;
import br.senac.fianceiro.exception.ResourceNotFoundException;
import br.senac.fianceiro.familia.dto.AdicionarMembroRequest;
import br.senac.fianceiro.familia.dto.CriarFamiliaRequest;
import br.senac.fianceiro.familia.dto.FamiliaMembroResponse;
import br.senac.fianceiro.familia.dto.FamiliaResponse;
import br.senac.fianceiro.familia.entity.Familia;
import br.senac.fianceiro.familia.entity.FamiliaMembro;
import br.senac.fianceiro.familia.entity.PapelMembro;
import br.senac.fianceiro.familia.entity.StatusMembro;
import br.senac.fianceiro.familia.repository.FamiliaMembroRepository;
import br.senac.fianceiro.familia.repository.FamiliaRepository;
import br.senac.fianceiro.usuario.entity.Usuario;
import br.senac.fianceiro.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FamiliaService {

    private final FamiliaRepository familiaRepository;
    private final FamiliaMembroRepository familiaMembroRepository;
    private final UsuarioRepository usuarioRepository;

    public FamiliaService(FamiliaRepository familiaRepository,
                          FamiliaMembroRepository familiaMembroRepository,
                          UsuarioRepository usuarioRepository) {
        this.familiaRepository = familiaRepository;
        this.familiaMembroRepository = familiaMembroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<FamiliaResponse> listarFamiliasDoUsuario(Long usuarioId) {
        List<FamiliaMembro> membros = familiaMembroRepository.findAll().stream()
                .filter(fm -> fm.getUsuario().getId().equals(usuarioId) && fm.getStatus() == StatusMembro.ATIVO)
                .collect(Collectors.toList());

        return membros.stream()
                .map(fm -> FamiliaResponse.builder()
                        .id(fm.getFamilia().getId())
                        .nome(fm.getFamilia().getNome())
                        .dataCriacao(fm.getFamilia().getDataCriacao())
                        .meuPapel(fm.getPapel())
                        .meuStatus(fm.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public FamiliaResponse criarFamilia(Long usuarioId, CriarFamiliaRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        Familia familia = Familia.builder()
                .nome(request.getNome().trim())
                .build();
        familia = familiaRepository.save(familia);

        FamiliaMembro membroAdmin = FamiliaMembro.builder()
                .familia(familia)
                .usuario(usuario)
                .papel(PapelMembro.ADMINISTRADOR)
                .status(StatusMembro.ATIVO)
                .build();
        familiaMembroRepository.save(membroAdmin);

        return FamiliaResponse.builder()
                .id(familia.getId())
                .nome(familia.getNome())
                .dataCriacao(familia.getDataCriacao())
                .meuPapel(PapelMembro.ADMINISTRADOR)
                .meuStatus(StatusMembro.ATIVO)
                .build();
    }

    @Transactional(readOnly = true)
    public List<FamiliaMembroResponse> listarMembros(Long usuarioId, Long familiaId) {
        validarPertencimentoAFamilia(usuarioId, familiaId);

        return familiaMembroRepository.findByFamiliaIdWithUsuario(familiaId).stream()
                .map(this::converterMembroParaResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FamiliaMembroResponse adicionarMembro(Long usuarioAutenticadoId, Long familiaId, AdicionarMembroRequest request) {
        FamiliaMembro membroExecutor = familiaMembroRepository
                .findByFamiliaIdAndUsuarioIdAndStatus(familiaId, usuarioAutenticadoId, StatusMembro.ATIVO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não faz parte desta família."));

        if (membroExecutor.getPapel() != PapelMembro.ADMINISTRADOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores podem gerenciar integrantes da família.");
        }

        Familia familia = familiaRepository.findById(familiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Família não encontrada."));

        String emailNormalizado = request.getEmail().trim().toLowerCase();
        Usuario novoUsuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum usuário cadastrado com o e-mail: " + emailNormalizado));

        if (familiaMembroRepository.existsByFamiliaIdAndUsuarioId(familiaId, novoUsuario.getId())) {
            throw new BusinessException("Este usuário já faz parte do grupo familiar.");
        }

        FamiliaMembro novoMembro = FamiliaMembro.builder()
                .familia(familia)
                .usuario(novoUsuario)
                .papel(request.getPapel())
                .status(StatusMembro.ATIVO)
                .build();

        novoMembro = familiaMembroRepository.save(novoMembro);
        return converterMembroParaResponse(novoMembro);
    }

    public FamiliaMembro validarPertencimentoAFamilia(Long usuarioId, Long familiaId) {
        return familiaMembroRepository.findByFamiliaIdAndUsuarioIdAndStatus(familiaId, usuarioId, StatusMembro.ATIVO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado: você não pertence a esta família."));
    }

    public FamiliaMembro validarPermissaoAdministrador(Long usuarioId, Long familiaId) {
        FamiliaMembro membro = validarPertencimentoAFamilia(usuarioId, familiaId);
        if (membro.getPapel() != PapelMembro.ADMINISTRADOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ação restrita a administradores da família.");
        }
        return membro;
    }

    private FamiliaMembroResponse converterMembroParaResponse(FamiliaMembro fm) {
        return FamiliaMembroResponse.builder()
                .id(fm.getId())
                .usuarioId(fm.getUsuario().getId())
                .nome(fm.getUsuario().getNome())
                .email(fm.getUsuario().getEmail())
                .papel(fm.getPapel())
                .status(fm.getStatus())
                .dataEntrada(fm.getDataEntrada())
                .build();
    }
}
