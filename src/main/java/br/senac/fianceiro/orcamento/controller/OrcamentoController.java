package br.senac.fianceiro.orcamento.controller;

import br.senac.fianceiro.common.dto.ApiResponse;
import br.senac.fianceiro.orcamento.dto.CriarOrcamentoRequest;
import br.senac.fianceiro.orcamento.dto.OrcamentoResponse;
import br.senac.fianceiro.orcamento.service.OrcamentoService;
import br.senac.fianceiro.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrcamentoResponse>>> listar(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal) {
        List<OrcamentoResponse> lista = orcamentoService.listar(usuarioPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.sucesso(lista));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrcamentoResponse>> criar(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody CriarOrcamentoRequest request) {
        OrcamentoResponse response = orcamentoService.criar(usuarioPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Orçamento cadastrado com sucesso.", response));
    }
}
