package br.senac.fianceiro.recorrente.service;

import br.senac.fianceiro.categoria.entity.Categoria;
import br.senac.fianceiro.categoria.service.CategoriaService;
import br.senac.fianceiro.conta.dto.ContaResponse;
import br.senac.fianceiro.conta.entity.ContaFinanceira;
import br.senac.fianceiro.conta.service.ContaService;
import br.senac.fianceiro.exception.BusinessException;
import br.senac.fianceiro.exception.ResourceNotFoundException;
import br.senac.fianceiro.recorrente.dto.AtualizarDespesaRecorrenteRequest;
import br.senac.fianceiro.recorrente.dto.CriarDespesaRecorrenteRequest;
import br.senac.fianceiro.recorrente.dto.DespesaRecorrenteResponse;
import br.senac.fianceiro.recorrente.entity.DespesaRecorrente;
import br.senac.fianceiro.recorrente.entity.StatusRecorrencia;
import br.senac.fianceiro.recorrente.repository.DespesaRecorrenteRepository;
import br.senac.fianceiro.transacao.dto.CriarTransacaoRequest;
import br.senac.fianceiro.transacao.entity.TipoTransacao;
import br.senac.fianceiro.transacao.service.TransacaoService;
import br.senac.fianceiro.usuario.entity.Usuario;
import br.senac.fianceiro.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DespesaRecorrenteService {

    private final DespesaRecorrenteRepository repository;
    private final ContaService contaService;
    private final CategoriaService categoriaService;
    private final TransacaoService transacaoService;
    private final UsuarioRepository usuarioRepository;

    public DespesaRecorrenteService(DespesaRecorrenteRepository repository,
                                     ContaService contaService,
                                     CategoriaService categoriaService,
                                     TransacaoService transacaoService,
                                     UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.contaService = contaService;
        this.categoriaService = categoriaService;
        this.transacaoService = transacaoService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<DespesaRecorrenteResponse> listar(Long usuarioId) {
        List<ContaResponse> contasAcessiveis = contaService.listarContasAcessiveis(usuarioId);
        if (contasAcessiveis.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> contasIds = contasAcessiveis.stream()
                .map(ContaResponse::getId)
                .collect(Collectors.toList());

        return repository.findByContaIdIn(contasIds).stream()
                .map(this::converterParaResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DespesaRecorrenteResponse criar(Long usuarioId, CriarDespesaRecorrenteRequest request) {
        Usuario criador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        ContaFinanceira conta = contaService.validarPermissaoMovimentacao(usuarioId, request.getContaId());

        Categoria categoria = null;
        if (request.getCategoriaId() != null) {
            categoria = categoriaService.validarAcessoCategoria(usuarioId, request.getCategoriaId());
        }

        if (request.getDataFim() != null && request.getDataFim().isBefore(request.getDataInicio())) {
            throw new BusinessException("A data de fim não pode ser anterior à data de início.");
        }

        DespesaRecorrente despesa = DespesaRecorrente.builder()
                .descricao(request.getDescricao().trim())
                .valor(request.getValor())
                .frequencia(request.getFrequencia())
                .diaVencimento(request.getDiaVencimento())
                .dataInicio(request.getDataInicio())
                .dataFim(request.getDataFim())
                .status(StatusRecorrencia.ATIVA)
                .conta(conta)
                .categoria(categoria)
                .criadoPor(criador)
                .escopo(request.getEscopo())
                .proximaGeracao(request.getDataInicio())
                .build();

        despesa = repository.save(despesa);
        return converterParaResponse(despesa);
    }

    @Transactional
    public DespesaRecorrenteResponse atualizar(Long usuarioId, Long id, AtualizarDespesaRecorrenteRequest request) {
        DespesaRecorrente despesa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa recorrente não encontrada com ID: " + id));

        contaService.validarAcessoConta(usuarioId, despesa.getConta().getId());

        if (despesa.getStatus() == StatusRecorrencia.ENCERRADA) {
            throw new BusinessException("Não é possível editar uma despesa recorrente encerrada.");
        }

        if (request.getDescricao() != null && !request.getDescricao().isBlank()) {
            despesa.setDescricao(request.getDescricao().trim());
        }
        if (request.getValor() != null) {
            despesa.setValor(request.getValor());
        }
        if (request.getDiaVencimento() != null) {
            despesa.setDiaVencimento(request.getDiaVencimento());
        }
        if (request.getDataFim() != null) {
            despesa.setDataFim(request.getDataFim());
        }
        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaService.validarAcessoCategoria(usuarioId, request.getCategoriaId());
            despesa.setCategoria(categoria);
        }
        if (request.getStatus() != null) {
            despesa.setStatus(request.getStatus());
        }

        despesa = repository.save(despesa);
        return converterParaResponse(despesa);
    }

    /**
     * Processa as despesas recorrentes ativas cuja próxima data de geração
     * é hoje ou anterior. Para cada uma, gera uma transação de despesa
     * e avança a data de próxima geração.
     */
    @Transactional
    public int processarRecorrencias(LocalDate dataReferencia) {
        List<DespesaRecorrente> pendentes = repository.findAtivasParaGeracao(
                StatusRecorrencia.ATIVA, dataReferencia);

        int geradas = 0;
        for (DespesaRecorrente dr : pendentes) {
            // Verificar se ultrapassou data-fim
            if (dr.getDataFim() != null && dr.getProximaGeracao().isAfter(dr.getDataFim())) {
                dr.setStatus(StatusRecorrencia.ENCERRADA);
                repository.save(dr);
                continue;
            }

            try {
                // Gera transação de despesa
                CriarTransacaoRequest txRequest = CriarTransacaoRequest.builder()
                        .descricao("[Recorrente] " + dr.getDescricao())
                        .valor(dr.getValor())
                        .tipo(TipoTransacao.DESPESA)
                        .dataCompetencia(dr.getProximaGeracao())
                        .contaId(dr.getConta().getId())
                        .categoriaId(dr.getCategoria() != null ? dr.getCategoria().getId() : null)
                        .escopo(dr.getEscopo())
                        .build();

                transacaoService.criarTransacao(dr.getCriadoPor().getId(), txRequest);

                // Avança a próxima geração
                LocalDate novaProximaGeracao = dr.calcularProximaGeracao(dr.getProximaGeracao());
                dr.setProximaGeracao(novaProximaGeracao);

                // Encerrar se ultrapassou data-fim
                if (dr.getDataFim() != null && novaProximaGeracao.isAfter(dr.getDataFim())) {
                    dr.setStatus(StatusRecorrencia.ENCERRADA);
                }

                repository.save(dr);
                geradas++;
            } catch (BusinessException e) {
                // Se saldo insuficiente, pausa a recorrência
                dr.setStatus(StatusRecorrencia.PAUSADA);
                repository.save(dr);
            }
        }

        return geradas;
    }

    private DespesaRecorrenteResponse converterParaResponse(DespesaRecorrente dr) {
        return DespesaRecorrenteResponse.builder()
                .id(dr.getId())
                .descricao(dr.getDescricao())
                .valor(dr.getValor())
                .frequencia(dr.getFrequencia())
                .diaVencimento(dr.getDiaVencimento())
                .dataInicio(dr.getDataInicio())
                .dataFim(dr.getDataFim())
                .status(dr.getStatus())
                .contaId(dr.getConta().getId())
                .contaNome(dr.getConta().getNome())
                .categoriaId(dr.getCategoria() != null ? dr.getCategoria().getId() : null)
                .categoriaNome(dr.getCategoria() != null ? dr.getCategoria().getNome() : null)
                .criadoPorId(dr.getCriadoPor().getId())
                .criadoPorNome(dr.getCriadoPor().getNome())
                .escopo(dr.getEscopo())
                .proximaGeracao(dr.getProximaGeracao())
                .dataCriacao(dr.getDataCriacao())
                .build();
    }
}
