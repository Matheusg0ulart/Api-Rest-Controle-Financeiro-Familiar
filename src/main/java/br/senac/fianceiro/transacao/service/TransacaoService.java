package br.senac.fianceiro.transacao.service;

import br.senac.fianceiro.categoria.entity.Categoria;
import br.senac.fianceiro.categoria.service.CategoriaService;
import br.senac.fianceiro.conta.dto.ContaResponse;
import br.senac.fianceiro.conta.entity.ContaFinanceira;
import br.senac.fianceiro.conta.repository.ContaFinanceiraRepository;
import br.senac.fianceiro.conta.service.ContaService;
import br.senac.fianceiro.exception.BusinessException;
import br.senac.fianceiro.exception.ResourceNotFoundException;
import br.senac.fianceiro.transacao.dto.*;
import br.senac.fianceiro.transacao.entity.StatusTransacao;
import br.senac.fianceiro.transacao.entity.TipoTransacao;
import br.senac.fianceiro.transacao.entity.Transacao;
import br.senac.fianceiro.transacao.repository.TransacaoRepository;
import br.senac.fianceiro.usuario.entity.Usuario;
import br.senac.fianceiro.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaService contaService;
    private final ContaFinanceiraRepository contaRepository;
    private final CategoriaService categoriaService;
    private final UsuarioRepository usuarioRepository;

    public TransacaoService(TransacaoRepository transacaoRepository,
                            ContaService contaService,
                            ContaFinanceiraRepository contaRepository,
                            CategoriaService categoriaService,
                            UsuarioRepository usuarioRepository) {
        this.transacaoRepository = transacaoRepository;
        this.contaService = contaService;
        this.contaRepository = contaRepository;
        this.categoriaService = categoriaService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listarTransacoes(Long usuarioId,
                                                   LocalDate inicio,
                                                   LocalDate fim,
                                                   TipoTransacao tipo,
                                                   Long contaId,
                                                   Long categoriaId) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new BusinessException("A data de início não pode ser posterior à data de fim.");
        }

        List<ContaResponse> contasAcessiveis = contaService.listarContasAcessiveis(usuarioId);
        if (contasAcessiveis.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> contasIds = contasAcessiveis.stream()
                .map(ContaResponse::getId)
                .collect(Collectors.toList());

        if (contaId != null) {
            contaService.validarAcessoConta(usuarioId, contaId);
        }

        if (categoriaId != null) {
            categoriaService.validarAcessoCategoria(usuarioId, categoriaId);
        }

        return transacaoRepository.findComFiltros(contasIds, inicio, fim, tipo, contaId, categoriaId).stream()
                .map(this::converterParaResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransacaoResponse criarTransacao(Long usuarioId, CriarTransacaoRequest request) {
        if (request.getTipo() == TipoTransacao.TRANSFERENCIA) {
            throw new BusinessException("Para transferências entre contas, utilize o endpoint /api/transferencias.");
        }

        Usuario criadoPor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        ContaFinanceira conta = contaService.validarPermissaoMovimentacao(usuarioId, request.getContaId());

        Categoria categoria = null;
        if (request.getCategoriaId() != null) {
            categoria = categoriaService.validarAcessoCategoria(usuarioId, request.getCategoriaId());
        }

        // Regra Inegociável: Saldo Negativo é PROIBIDO
        if (request.getTipo() == TipoTransacao.DESPESA) {
            BigDecimal novoSaldo = conta.getSaldo().subtract(request.getValor());
            if (novoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(String.format(
                        "Saldo insuficiente na conta %s. Saldo atual: R$ %s. Valor da despesa: R$ %s.",
                        conta.getNome(), conta.getSaldo(), request.getValor()
                ));
            }
            conta.setSaldo(novoSaldo);
        } else if (request.getTipo() == TipoTransacao.RECEITA) {
            conta.setSaldo(conta.getSaldo().add(request.getValor()));
        }

        contaRepository.save(conta);

        Transacao transacao = Transacao.builder()
                .descricao(request.getDescricao().trim())
                .valor(request.getValor())
                .tipo(request.getTipo())
                .dataCompetencia(request.getDataCompetencia())
                .dataPagamento(request.getDataPagamento() != null ? request.getDataPagamento() : request.getDataCompetencia())
                .status(StatusTransacao.CONFIRMADA)
                .conta(conta)
                .categoria(categoria)
                .criadoPor(criadoPor)
                .escopo(request.getEscopo())
                .build();

        transacao = transacaoRepository.save(transacao);
        return converterParaResponse(transacao);
    }

    @Transactional
    public TransacaoResponse transferir(Long usuarioId, TransferenciaRequest request) {
        if (request.getContaOrigemId().equals(request.getContaDestinoId())) {
            throw new BusinessException("A conta de origem e a conta de destino não podem ser iguais.");
        }

        Usuario criadoPor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        ContaFinanceira contaOrigem = contaService.validarPermissaoMovimentacao(usuarioId, request.getContaOrigemId());
        ContaFinanceira contaDestino = contaService.validarPermissaoMovimentacao(usuarioId, request.getContaDestinoId());

        // Regra de Saldo Negativo na Origem
        BigDecimal novoSaldoOrigem = contaOrigem.getSaldo().subtract(request.getValor());
        if (novoSaldoOrigem.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(String.format(
                    "Saldo insuficiente na conta de origem %s. Saldo atual: R$ %s. Valor da transferência: R$ %s.",
                    contaOrigem.getNome(), contaOrigem.getSaldo(), request.getValor()
            ));
        }

        contaOrigem.setSaldo(novoSaldoOrigem);
        contaDestino.setSaldo(contaDestino.getSaldo().add(request.getValor()));

        contaRepository.save(contaOrigem);
        contaRepository.save(contaDestino);

        Transacao transacao = Transacao.builder()
                .descricao(request.getDescricao().trim())
                .valor(request.getValor())
                .tipo(TipoTransacao.TRANSFERENCIA)
                .dataCompetencia(request.getDataCompetencia())
                .dataPagamento(request.getDataPagamento() != null ? request.getDataPagamento() : request.getDataCompetencia())
                .status(StatusTransacao.CONFIRMADA)
                .conta(contaOrigem)
                .contaDestino(contaDestino)
                .criadoPor(criadoPor)
                .escopo(contaOrigem.isCompartilhada() ? contaOrigem.getFamilia() != null ?
                        br.senac.fianceiro.categoria.entity.EscopoRecurso.FAMILIAR : br.senac.fianceiro.categoria.entity.EscopoRecurso.PESSOAL
                        : br.senac.fianceiro.categoria.entity.EscopoRecurso.PESSOAL)
                .build();

        transacao = transacaoRepository.save(transacao);
        return converterParaResponse(transacao);
    }

    @Transactional
    public TransacaoResponse cancelarTransacao(Long usuarioId, Long transacaoId, CancelarTransacaoRequest request) {
        Transacao transacao = transacaoRepository.findById(transacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + transacaoId));

        contaService.validarAcessoConta(usuarioId, transacao.getConta().getId());

        if (transacao.getStatus() == StatusTransacao.CANCELADA) {
            throw new BusinessException("Esta transação já se encontra cancelada.");
        }

        // Reversão de Saldo com garantia contra Saldo Negativo
        if (transacao.getTipo() == TipoTransacao.RECEITA) {
            BigDecimal novoSaldo = transacao.getConta().getSaldo().subtract(transacao.getValor());
            if (novoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Não é possível cancelar esta receita, pois a conta ficaria com saldo negativo.");
            }
            transacao.getConta().setSaldo(novoSaldo);
            contaRepository.save(transacao.getConta());
        } else if (transacao.getTipo() == TipoTransacao.DESPESA) {
            transacao.getConta().setSaldo(transacao.getConta().getSaldo().add(transacao.getValor()));
            contaRepository.save(transacao.getConta());
        } else if (transacao.getTipo() == TipoTransacao.TRANSFERENCIA) {
            BigDecimal novoSaldoDestino = transacao.getContaDestino().getSaldo().subtract(transacao.getValor());
            if (novoSaldoDestino.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Não é possível cancelar a transferência, pois a conta de destino ficaria com saldo negativo.");
            }
            transacao.getContaDestino().setSaldo(novoSaldoDestino);
            transacao.getConta().setSaldo(transacao.getConta().getSaldo().add(transacao.getValor()));
            contaRepository.save(transacao.getContaDestino());
            contaRepository.save(transacao.getConta());
        }

        transacao.setStatus(StatusTransacao.CANCELADA);
        transacao.setDataCancelamento(LocalDateTime.now());
        transacao.setMotivoCancelamento(request != null ? request.getMotivo() : null);

        transacao = transacaoRepository.save(transacao);
        return converterParaResponse(transacao);
    }

    @Transactional
    public TransacaoResponse atualizarParcialmente(Long usuarioId, Long transacaoId, AtualizarTransacaoRequest request) {
        Transacao transacao = transacaoRepository.findById(transacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + transacaoId));

        contaService.validarAcessoConta(usuarioId, transacao.getConta().getId());

        if (transacao.getStatus() == StatusTransacao.CANCELADA) {
            throw new BusinessException("Não é permitido editar uma transação cancelada.");
        }

        if (request.getDescricao() != null && !request.getDescricao().isBlank()) {
            transacao.setDescricao(request.getDescricao().trim());
        }
        if (request.getDataCompetencia() != null) {
            transacao.setDataCompetencia(request.getDataCompetencia());
        }
        if (request.getDataPagamento() != null) {
            transacao.setDataPagamento(request.getDataPagamento());
        }
        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaService.validarAcessoCategoria(usuarioId, request.getCategoriaId());
            transacao.setCategoria(categoria);
        }

        transacao = transacaoRepository.save(transacao);
        return converterParaResponse(transacao);
    }

    public TransacaoResponse converterParaResponse(Transacao t) {
        return TransacaoResponse.builder()
                .id(t.getId())
                .descricao(t.getDescricao())
                .valor(t.getValor())
                .tipo(t.getTipo())
                .status(t.getStatus())
                .dataCompetencia(t.getDataCompetencia())
                .dataPagamento(t.getDataPagamento())
                .contaId(t.getConta().getId())
                .contaNome(t.getConta().getNome())
                .contaDestinoId(t.getContaDestino() != null ? t.getContaDestino().getId() : null)
                .contaDestinoNome(t.getContaDestino() != null ? t.getContaDestino().getNome() : null)
                .categoriaId(t.getCategoria() != null ? t.getCategoria().getId() : null)
                .categoriaNome(t.getCategoria() != null ? t.getCategoria().getNome() : null)
                .criadoPorId(t.getCriadoPor().getId())
                .criadoPorNome(t.getCriadoPor().getNome())
                .escopo(t.getEscopo())
                .dataCriacao(t.getDataCriacao())
                .dataCancelamento(t.getDataCancelamento())
                .motivoCancelamento(t.getMotivoCancelamento())
                .build();
    }
}
