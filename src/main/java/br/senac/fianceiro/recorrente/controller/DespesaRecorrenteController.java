package br.senac.fianceiro.recorrente.controller;

import br.senac.fianceiro.common.dto.ApiResponse;
import br.senac.fianceiro.recorrente.dto.AtualizarDespesaRecorrenteRequest;
import br.senac.fianceiro.recorrente.dto.CriarDespesaRecorrenteRequest;
import br.senac.fianceiro.recorrente.dto.DespesaRecorrenteResponse;
import br.senac.fianceiro.recorrente.service.DespesaRecorrenteService;
import br.senac.fianceiro.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/recorrentes", "/api/recorrencias"})
public class DespesaRecorrenteController {

    private final DespesaRecorrenteService service;

    public DespesaRecorrenteController(DespesaRecorrenteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DespesaRecorrenteResponse>>> listar(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal) {
        List<DespesaRecorrenteResponse> lista = service.listar(usuarioPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.sucesso(lista));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DespesaRecorrenteResponse>> criar(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody CriarDespesaRecorrenteRequest request) {
        DespesaRecorrenteResponse response = service.criar(usuarioPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Despesa recorrente cadastrada com sucesso.", response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<DespesaRecorrenteResponse>> atualizar(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @PathVariable("id") Long id,
            @Valid @RequestBody AtualizarDespesaRecorrenteRequest request) {
        DespesaRecorrenteResponse response = service.atualizar(usuarioPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.sucesso("Despesa recorrente atualizada com sucesso.", response));
    }

    @PostMapping("/processar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> processarRecorrencias(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal) {
        int geradas = service.processarRecorrencias(LocalDate.now());
        return ResponseEntity.ok(ApiResponse.sucesso("Processamento concluído.",
                Map.of("transacoesGeradas", geradas, "dataProcessamento", LocalDate.now().toString())));
    }
}
