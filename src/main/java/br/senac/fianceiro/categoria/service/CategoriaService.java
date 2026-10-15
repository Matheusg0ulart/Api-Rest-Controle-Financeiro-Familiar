package br.senac.fianceiro.categoria.service;

import br.senac.fianceiro.categoria.dto.CategoriaResponse;
import br.senac.fianceiro.categoria.dto.CriarCategoriaRequest;
import br.senac.fianceiro.categoria.entity.Categoria;
import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.categoria.entity.TipoCategoria;
import br.senac.fianceiro.categoria.repository.CategoriaRepository;
import br.senac.fianceiro.exception.BusinessException;
import br.senac.fianceiro.exception.ResourceNotFoundException;
import br.senac.fianceiro.familia.entity.Familia;
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
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FamiliaRepository familiaRepository;
    private final FamiliaMembroRepository familiaMembroRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            UsuarioRepository usuarioRepository,
                            FamiliaRepository familiaRepository,
                            FamiliaMembroRepository familiaMembroRepository) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.familiaRepository = familiaRepository;
        this.familiaMembroRepository = familiaMembroRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias(Long usuarioId, TipoCategoria tipo) {
        List<Categoria> categorias;
        if (tipo != null) {
            categorias = categoriaRepository.findCategoriasAcessiveisPorTipo(usuarioId, tipo);
        } else {
            categorias = categoriaRepository.findCategoriasAcessiveis(usuarioId);
        }

        return categorias.stream()
                .map(this::converterParaResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoriaResponse criarCategoria(Long usuarioId, CriarCategoriaRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        Familia familia = null;
        if (request.getEscopo() == EscopoRecurso.FAMILIAR) {
            if (request.getFamiliaId() == null) {
                throw new BusinessException("É obrigatório informar o familiaId para categorias de escopo familiar.");
            }
            familia = familiaRepository.findById(request.getFamiliaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Família não encontrada."));

            boolean isMembro = familiaMembroRepository
                    .findByFamiliaIdAndUsuarioIdAndStatus(request.getFamiliaId(), usuarioId, StatusMembro.ATIVO)
                    .isPresent();

            if (!isMembro) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Você não tem permissão para cadastrar categorias nesta família.");
            }
        }

        Categoria categoria = Categoria.builder()
                .nome(request.getNome().trim())
                .tipo(request.getTipo())
                .escopo(request.getEscopo())
                .usuario(usuario)
                .familia(familia)
                .build();

        categoria = categoriaRepository.save(categoria);
        return converterParaResponse(categoria);
    }

    public Categoria validarAcessoCategoria(Long usuarioId, Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + categoriaId));

        if (categoria.getUsuario().getId().equals(usuarioId)) {
            return categoria;
        }

        if (categoria.getEscopo() == EscopoRecurso.FAMILIAR && categoria.getFamilia() != null) {
            boolean isMembro = familiaMembroRepository
                    .findByFamiliaIdAndUsuarioIdAndStatus(categoria.getFamilia().getId(), usuarioId, StatusMembro.ATIVO)
                    .isPresent();
            if (isMembro) {
                return categoria;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Acesso negado: você não tem permissão para utilizar esta categoria.");
    }

    public CategoriaResponse converterParaResponse(Categoria categoria) {
        return CategoriaResponse.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .tipo(categoria.getTipo())
                .escopo(categoria.getEscopo())
                .usuarioId(categoria.getUsuario().getId())
                .familiaId(categoria.getFamilia() != null ? categoria.getFamilia().getId() : null)
                .dataCriacao(categoria.getDataCriacao())
                .build();
    }
}
