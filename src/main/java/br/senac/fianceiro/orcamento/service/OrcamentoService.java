package br.senac.fianceiro.orcamento.service;

import br.senac.fianceiro.categoria.entity.Categoria;
import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.categoria.repository.CategoriaRepository;
import br.senac.fianceiro.exception.BusinessException;
import br.senac.fianceiro.exception.ResourceNotFoundException;
import br.senac.fianceiro.familia.entity.Familia;
import br.senac.fianceiro.familia.entity.PapelMembro;
import br.senac.fianceiro.familia.repository.FamiliaMembroRepository;
import br.senac.fianceiro.familia.repository.FamiliaRepository;
import br.senac.fianceiro.orcamento.dto.CriarOrcamentoRequest;
import br.senac.fianceiro.orcamento.dto.OrcamentoResponse;
import br.senac.fianceiro.orcamento.entity.Orcamento;
import br.senac.fianceiro.orcamento.repository.OrcamentoRepository;
import br.senac.fianceiro.transacao.entity.StatusTransacao;
import br.senac.fianceiro.transacao.entity.TipoTransacao;
import br.senac.fianceiro.transacao.entity.Transacao;
import br.senac.fianceiro.transacao.repository.TransacaoRepository;
import br.senac.fianceiro.usuario.entity.Usuario;
import br.senac.fianceiro.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final FamiliaRepository familiaRepository;
    private final FamiliaMembroRepository familiaMembroRepository;
    private final TransacaoRepository transacaoRepository;

    public OrcamentoService(OrcamentoRepository orcamentoRepository,
                            UsuarioRepository usuarioRepository,
                            CategoriaRepository categoriaRepository,
                            FamiliaRepository familiaRepository,
                            FamiliaMembroRepository familiaMembroRepository,
                            TransacaoRepository transacaoRepository) {
        this.orcamentoRepository = orcamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.familiaRepository = familiaRepository;
        this.familiaMembroRepository = familiaMembroRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listar(Long usuarioId) {
        List<Orcamento> orcamentos = orcamentoRepository.listarAcessiveis(usuarioId);
        return orcamentos.stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrcamentoResponse criar(Long usuarioId, CriarOrcamentoRequest request) {
        Usuario titular = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        Categoria categoria = null;
        if (request.getCategoriaId() != null) {
            categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
        }

        Familia familia = null;
        if (request.getEscopo() == EscopoRecurso.FAMILIAR) {
            if (request.getFamiliaId() == null) {
                throw new BusinessException("Identificador da família é obrigatório para orçamentos familiares.");
            }
            familia = familiaRepository.findById(request.getFamiliaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Família não encontrada."));

            var membroOpt = familiaMembroRepository.findByFamiliaIdAndUsuarioId(familia.getId(), usuarioId);
            if (membroOpt.isEmpty() || membroOpt.get().getStatus() != br.senac.fianceiro.familia.entity.StatusMembro.ATIVO) {
                throw new BusinessException("Usuário não é integrante ativo da família especificada.");
            }
            if (membroOpt.get().getPapel() != PapelMembro.ADMINISTRADOR) {
                throw new BusinessException("Apenas administradores da família podem criar orçamentos familiares.");
            }
        }

        Orcamento orcamento = Orcamento.builder()
                .nome(request.getNome().trim())
                .valorLimite(request.getValorLimite())
                .periodo(request.getPeriodo().trim())
                .escopo(request.getEscopo())
                .categoria(categoria)
                .titular(titular)
                .familia(familia)
                .ativo(true)
                .build();

        Orcamento salvo = orcamentoRepository.save(orcamento);
        return toResponse(salvo);
    }

    private OrcamentoResponse toResponse(Orcamento orcamento) {
        BigDecimal gasto = calcularGasto(orcamento);
        BigDecimal disponivel = orcamento.getValorLimite().subtract(gasto);
        BigDecimal percentual = BigDecimal.ZERO;
        if (orcamento.getValorLimite().compareTo(BigDecimal.ZERO) > 0) {
            percentual = gasto.multiply(BigDecimal.valueOf(100))
                    .divide(orcamento.getValorLimite(), 2, RoundingMode.HALF_UP);
        }

        return OrcamentoResponse.builder()
                .id(orcamento.getId())
                .nome(orcamento.getNome())
                .valorLimite(orcamento.getValorLimite())
                .valorGasto(gasto)
                .valorDisponivel(disponivel)
                .porcentagemConsumida(percentual)
                .periodo(orcamento.getPeriodo())
                .escopo(orcamento.getEscopo())
                .categoriaId(orcamento.getCategoria() != null ? orcamento.getCategoria().getId() : null)
                .categoriaNome(orcamento.getCategoria() != null ? orcamento.getCategoria().getNome() : null)
                .titularId(orcamento.getTitular().getId())
                .titularNome(orcamento.getTitular().getNome())
                .familiaId(orcamento.getFamilia() != null ? orcamento.getFamilia().getId() : null)
                .familiaNome(orcamento.getFamilia() != null ? orcamento.getFamilia().getNome() : null)
                .ativo(orcamento.getAtivo())
                .criadoEm(orcamento.getCriadoEm())
                .build();
    }

    private BigDecimal calcularGasto(Orcamento orcamento) {
        LocalDate inicio;
        LocalDate fim;

        try {
            if (orcamento.getPeriodo().matches("\\d{4}-\\d{2}")) {
                YearMonth ym = YearMonth.parse(orcamento.getPeriodo());
                inicio = ym.atDay(1);
                fim = ym.atEndOfMonth();
            } else {
                YearMonth ym = YearMonth.now();
                inicio = ym.atDay(1);
                fim = ym.atEndOfMonth();
            }
        } catch (Exception e) {
            inicio = LocalDate.now().withDayOfMonth(1);
            fim = LocalDate.now();
        }

        List<Transacao> todas = transacaoRepository.findAll();
        BigDecimal totalGasto = BigDecimal.ZERO;

        for (Transacao t : todas) {
            if (t.getTipo() == TipoTransacao.DESPESA && t.getStatus() == StatusTransacao.CONFIRMADA) {
                LocalDate data = t.getDataCompetencia();
                if ((data.isEqual(inicio) || data.isAfter(inicio)) && (data.isEqual(fim) || data.isBefore(fim))) {
                    boolean categoriaMatch = (orcamento.getCategoria() == null) ||
                            (t.getCategoria() != null && t.getCategoria().getId().equals(orcamento.getCategoria().getId()));

                    boolean titularMatch = false;
                    if (orcamento.getEscopo() == EscopoRecurso.PESSOAL) {
                        titularMatch = t.getCriadoPor() != null && t.getCriadoPor().getId().equals(orcamento.getTitular().getId());
                    } else if (orcamento.getFamilia() != null && t.getConta() != null && t.getConta().getFamilia() != null) {
                        titularMatch = t.getConta().getFamilia().getId().equals(orcamento.getFamilia().getId());
                    }

                    if (categoriaMatch && titularMatch) {
                        totalGasto = totalGasto.add(t.getValor());
                    }
                }
            }
        }
        return totalGasto;
    }
}
