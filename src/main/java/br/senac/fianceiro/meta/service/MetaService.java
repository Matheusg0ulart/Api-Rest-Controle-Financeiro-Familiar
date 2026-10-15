package br.senac.fianceiro.meta.service;

import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.conta.entity.ContaFinanceira;
import br.senac.fianceiro.conta.repository.ContaFinanceiraRepository;
import br.senac.fianceiro.exception.BusinessException;
import br.senac.fianceiro.exception.ResourceNotFoundException;
import br.senac.fianceiro.familia.entity.Familia;
import br.senac.fianceiro.familia.entity.PapelMembro;
import br.senac.fianceiro.familia.entity.StatusMembro;
import br.senac.fianceiro.familia.repository.FamiliaMembroRepository;
import br.senac.fianceiro.familia.repository.FamiliaRepository;
import br.senac.fianceiro.meta.dto.AporteResponse;
import br.senac.fianceiro.meta.dto.CriarAporteRequest;
import br.senac.fianceiro.meta.dto.CriarMetaRequest;
import br.senac.fianceiro.meta.dto.MetaResponse;
import br.senac.fianceiro.meta.entity.AporteMeta;
import br.senac.fianceiro.meta.entity.MetaFinanceira;
import br.senac.fianceiro.meta.entity.StatusMeta;
import br.senac.fianceiro.meta.repository.AporteMetaRepository;
import br.senac.fianceiro.meta.repository.MetaFinanceiraRepository;
import br.senac.fianceiro.usuario.entity.Usuario;
import br.senac.fianceiro.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class MetaService {

    private final MetaFinanceiraRepository metaRepository;
    private final AporteMetaRepository aporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final FamiliaRepository familiaRepository;
    private final FamiliaMembroRepository familiaMembroRepository;
    private final ContaFinanceiraRepository contaRepository;

    public MetaService(MetaFinanceiraRepository metaRepository,
                       AporteMetaRepository aporteRepository,
                       UsuarioRepository usuarioRepository,
                       FamiliaRepository familiaRepository,
                       FamiliaMembroRepository familiaMembroRepository,
                       ContaFinanceiraRepository contaRepository) {
        this.metaRepository = metaRepository;
        this.aporteRepository = aporteRepository;
        this.usuarioRepository = usuarioRepository;
        this.familiaRepository = familiaRepository;
        this.familiaMembroRepository = familiaMembroRepository;
        this.contaRepository = contaRepository;
    }

    @Transactional(readOnly = true)
    public List<MetaResponse> listar(Long usuarioId) {
        return metaRepository.listarAcessiveis(usuarioId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MetaResponse criar(Long usuarioId, CriarMetaRequest request) {
        Usuario titular = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        Familia familia = null;
        if (request.getEscopo() == EscopoRecurso.FAMILIAR) {
            if (request.getFamiliaId() == null) {
                throw new BusinessException("Identificador da família é obrigatório para metas familiares.");
            }
            familia = familiaRepository.findById(request.getFamiliaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Família não encontrada."));

            var membroOpt = familiaMembroRepository.findByFamiliaIdAndUsuarioId(familia.getId(), usuarioId);
            if (membroOpt.isEmpty() || membroOpt.get().getStatus() != StatusMembro.ATIVO) {
                throw new BusinessException("Usuário não participa ativamente da família informada.");
            }
            if (membroOpt.get().getPapel() != PapelMembro.ADMINISTRADOR) {
                throw new BusinessException("Apenas administradores podem criar metas familiares.");
            }
        }

        MetaFinanceira meta = MetaFinanceira.builder()
                .nome(request.getNome().trim())
                .descricao(request.getDescricao())
                .valorAlvo(request.getValorAlvo())
                .valorAtual(BigDecimal.ZERO)
                .prazo(request.getPrazo())
                .status(StatusMeta.EM_ANDAMENTO)
                .escopo(request.getEscopo())
                .titular(titular)
                .familia(familia)
                .build();

        MetaFinanceira salva = metaRepository.save(meta);
        return toResponse(salva);
    }

    @Transactional
    public AporteResponse registrarAporte(Long usuarioId, Long metaId, CriarAporteRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        MetaFinanceira meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta financeira não encontrada."));

        // Validar permissão de acesso à meta
        validarAcessoMeta(usuarioId, meta);

        if (meta.getStatus() == StatusMeta.CANCELADA) {
            throw new BusinessException("Não é possível realizar aportes em uma meta cancelada.");
        }

        ContaFinanceira contaOrigem = null;
        if (request.getOrigemContaId() != null) {
            contaOrigem = contaRepository.findById(request.getOrigemContaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conta de origem não encontrada."));

            if (contaOrigem.getSaldo().compareTo(request.getValor()) < 0) {
                throw new BusinessException("Saldo insuficiente na conta selecionada para realizar o aporte.");
            }
            contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(request.getValor()));
            contaRepository.save(contaOrigem);
        }

        AporteMeta aporte = AporteMeta.builder()
                .meta(meta)
                .valor(request.getValor())
                .data(request.getData() != null ? request.getData() : LocalDate.now())
                .observacao(request.getObservacao())
                .origemConta(contaOrigem)
                .usuario(usuario)
                .build();

        AporteMeta salvo = aporteRepository.save(aporte);

        // Atualizar valor acumulado na meta
        BigDecimal novoValor = meta.getValorAtual().add(request.getValor());
        meta.setValorAtual(novoValor);
        if (novoValor.compareTo(meta.getValorAlvo()) >= 0) {
            meta.setStatus(StatusMeta.ATINGIDA);
        }
        metaRepository.save(meta);

        return toAporteResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<AporteResponse> listarAportes(Long usuarioId, Long metaId) {
        MetaFinanceira meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta não encontrada."));
        validarAcessoMeta(usuarioId, meta);

        return aporteRepository.findByMetaIdOrderByDataDescCriadoEmDesc(metaId).stream()
                .map(this::toAporteResponse)
                .toList();
    }

    private void validarAcessoMeta(Long usuarioId, MetaFinanceira meta) {
        if (meta.getEscopo() == EscopoRecurso.PESSOAL) {
            if (!meta.getTitular().getId().equals(usuarioId)) {
                throw new BusinessException("Acesso não autorizado a esta meta pessoal.");
            }
        } else {
            if (meta.getFamilia() == null) {
                throw new BusinessException("Meta compartilhada inconsistente.");
            }
            var membro = familiaMembroRepository.findByFamiliaIdAndUsuarioId(meta.getFamilia().getId(), usuarioId);
            if (membro.isEmpty() || membro.get().getStatus() != StatusMembro.ATIVO) {
                throw new BusinessException("Usuário não tem acesso à família vinculada a esta meta.");
            }
        }
    }

    private MetaResponse toResponse(MetaFinanceira meta) {
        BigDecimal restante = meta.getValorAlvo().subtract(meta.getValorAtual());
        if (restante.compareTo(BigDecimal.ZERO) < 0) {
            restante = BigDecimal.ZERO;
        }

        BigDecimal progresso = BigDecimal.ZERO;
        if (meta.getValorAlvo().compareTo(BigDecimal.ZERO) > 0) {
            progresso = meta.getValorAtual().multiply(BigDecimal.valueOf(100))
                    .divide(meta.getValorAlvo(), 2, RoundingMode.HALF_UP);
        }

        return MetaResponse.builder()
                .id(meta.getId())
                .nome(meta.getNome())
                .descricao(meta.getDescricao())
                .valorAlvo(meta.getValorAlvo())
                .valorAtual(meta.getValorAtual())
                .valorRestante(restante)
                .progressoPorcentagem(progresso)
                .prazo(meta.getPrazo())
                .status(meta.getStatus())
                .escopo(meta.getEscopo())
                .titularId(meta.getTitular().getId())
                .titularNome(meta.getTitular().getNome())
                .familiaId(meta.getFamilia() != null ? meta.getFamilia().getId() : null)
                .familiaNome(meta.getFamilia() != null ? meta.getFamilia().getNome() : null)
                .criadoEm(meta.getCriadoEm())
                .build();
    }

    private AporteResponse toAporteResponse(AporteMeta aporte) {
        return AporteResponse.builder()
                .id(aporte.getId())
                .metaId(aporte.getMeta().getId())
                .valor(aporte.getValor())
                .data(aporte.getData())
                .observacao(aporte.getObservacao())
                .origemContaId(aporte.getOrigemConta() != null ? aporte.getOrigemConta().getId() : null)
                .origemContaNome(aporte.getOrigemConta() != null ? aporte.getOrigemConta().getNome() : null)
                .usuarioId(aporte.getUsuario().getId())
                .usuarioNome(aporte.getUsuario().getNome())
                .criadoEm(aporte.getCriadoEm())
                .build();
    }
}
