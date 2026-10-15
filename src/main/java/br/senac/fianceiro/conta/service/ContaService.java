package br.senac.fianceiro.conta.service;

import br.senac.fianceiro.conta.dto.ContaResponse;
import br.senac.fianceiro.conta.dto.CriarContaRequest;
import br.senac.fianceiro.conta.entity.ContaFinanceira;
import br.senac.fianceiro.conta.repository.ContaFinanceiraRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContaService {

    private final ContaFinanceiraRepository contaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FamiliaRepository familiaRepository;
    private final FamiliaMembroRepository familiaMembroRepository;

    public ContaService(ContaFinanceiraRepository contaRepository,
                        UsuarioRepository usuarioRepository,
                        FamiliaRepository familiaRepository,
                        FamiliaMembroRepository familiaMembroRepository) {
        this.contaRepository = contaRepository;
        this.usuarioRepository = usuarioRepository;
        this.familiaRepository = familiaRepository;
        this.familiaMembroRepository = familiaMembroRepository;
    }

    @Transactional(readOnly = true)
    public List<ContaResponse> listarContasAcessiveis(Long usuarioId) {
        return contaRepository.findContasAcessiveis(usuarioId).stream()
                .map(this::converterParaResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContaResponse criarConta(Long usuarioId, CriarContaRequest request) {
        Usuario titular = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        Familia familia = null;
        if (request.getFamiliaId() != null) {
            familia = familiaRepository.findById(request.getFamiliaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Família não encontrada."));

            boolean isMembroAtivo = familiaMembroRepository
                    .findByFamiliaIdAndUsuarioIdAndStatus(request.getFamiliaId(), usuarioId, StatusMembro.ATIVO)
                    .isPresent();

            if (!isMembroAtivo) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Você não tem permissão para associar uma conta a esta família.");
            }
        }

        BigDecimal saldoInicial = request.getSaldoInicial() != null ? request.getSaldoInicial() : BigDecimal.ZERO;

        ContaFinanceira conta = ContaFinanceira.builder()
                .nome(request.getNome().trim())
                .tipo(request.getTipo())
                .moeda(request.getMoeda() != null ? request.getMoeda().trim().toUpperCase() : "BRL")
                .saldo(saldoInicial)
                .titular(titular)
                .familia(familia)
                .ativa(true)
                .build();

        conta = contaRepository.save(conta);
        return converterParaResponse(conta);
    }

    @Transactional(readOnly = true)
    public ContaResponse obterContaPorId(Long usuarioId, Long contaId) {
        ContaFinanceira conta = validarAcessoConta(usuarioId, contaId);
        return converterParaResponse(conta);
    }

    public ContaFinanceira validarAcessoConta(Long usuarioId, Long contaId) {
        ContaFinanceira conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta financeira não encontrada com ID: " + contaId));

        if (conta.getTitular().getId().equals(usuarioId)) {
            return conta;
        }

        if (conta.isCompartilhada()) {
            boolean isMembro = familiaMembroRepository
                    .findByFamiliaIdAndUsuarioIdAndStatus(conta.getFamilia().getId(), usuarioId, StatusMembro.ATIVO)
                    .isPresent();
            if (isMembro) {
                return conta;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Acesso negado: você não tem permissão para acessar esta conta financeira.");
    }

    public ContaFinanceira validarPermissaoMovimentacao(Long usuarioId, Long contaId) {
        ContaFinanceira conta = validarAcessoConta(usuarioId, contaId);
        if (!conta.isAtiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta conta está inativa.");
        }
        return conta;
    }

    public ContaResponse converterParaResponse(ContaFinanceira conta) {
        return ContaResponse.builder()
                .id(conta.getId())
                .nome(conta.getNome())
                .tipo(conta.getTipo())
                .moeda(conta.getMoeda())
                .saldo(conta.getSaldo())
                .titularId(conta.getTitular().getId())
                .titularNome(conta.getTitular().getNome())
                .familiaId(conta.getFamilia() != null ? conta.getFamilia().getId() : null)
                .familiaNome(conta.getFamilia() != null ? conta.getFamilia().getNome() : null)
                .compartilhada(conta.isCompartilhada())
                .ativa(conta.isAtiva())
                .dataCriacao(conta.getDataCriacao())
                .build();
    }
}
