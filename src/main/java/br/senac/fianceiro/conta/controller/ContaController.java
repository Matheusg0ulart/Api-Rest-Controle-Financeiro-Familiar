package br.senac.fianceiro.conta.controller;

import br.senac.fianceiro.common.dto.ApiResponse;
import br.senac.fianceiro.conta.dto.ContaResponse;
import br.senac.fianceiro.conta.dto.CriarContaRequest;
import br.senac.fianceiro.conta.service.ContaService;
import br.senac.fianceiro.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContaResponse>>> listarContas(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal) {
        List<ContaResponse> contas = contaService.listarContasAcessiveis(usuarioPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.sucesso(contas));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContaResponse>> criarConta(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody CriarContaRequest request) {
        ContaResponse conta = contaService.criarConta(usuarioPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Conta financeira criada com sucesso.", conta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContaResponse>> obterConta(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @PathVariable("id") Long contaId) {
        ContaResponse conta = contaService.obterContaPorId(usuarioPrincipal.getId(), contaId);
        return ResponseEntity.ok(ApiResponse.sucesso(conta));
    }
}
