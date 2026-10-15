package br.senac.fianceiro.transacao.controller;

import br.senac.fianceiro.common.dto.ApiResponse;
import br.senac.fianceiro.security.UsuarioPrincipal;
import br.senac.fianceiro.transacao.dto.AtualizarTransacaoRequest;
import br.senac.fianceiro.transacao.dto.CancelarTransacaoRequest;
import br.senac.fianceiro.transacao.dto.CriarTransacaoRequest;
import br.senac.fianceiro.transacao.dto.TransacaoResponse;
import br.senac.fianceiro.transacao.entity.TipoTransacao;
import br.senac.fianceiro.transacao.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransacaoResponse>>> listarTransacoes(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @RequestParam(name = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(name = "tipo", required = false) TipoTransacao tipo,
            @RequestParam(name = "contaId", required = false) Long contaId,
            @RequestParam(name = "categoriaId", required = false) Long categoriaId) {

        List<TransacaoResponse> transacoes = transacaoService.listarTransacoes(
                usuarioPrincipal.getId(), inicio, fim, tipo, contaId, categoriaId
        );
        return ResponseEntity.ok(ApiResponse.sucesso(transacoes));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransacaoResponse>> criarTransacao(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody CriarTransacaoRequest request) {

        TransacaoResponse response = transacaoService.criarTransacao(usuarioPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Lançamento registrado com sucesso.", response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TransacaoResponse>> atualizarParcialmente(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @PathVariable("id") Long id,
            @Valid @RequestBody AtualizarTransacaoRequest request) {

        TransacaoResponse response = transacaoService.atualizarParcialmente(usuarioPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.sucesso("Transação atualizada com sucesso.", response));
    }

    @PostMapping("/{id}/cancelamento")
    public ResponseEntity<ApiResponse<TransacaoResponse>> cancelarTransacao(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @PathVariable("id") Long id,
            @RequestBody(required = false) CancelarTransacaoRequest request) {

        TransacaoResponse response = transacaoService.cancelarTransacao(usuarioPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.sucesso("Lançamento cancelado com sucesso.", response));
    }
}
