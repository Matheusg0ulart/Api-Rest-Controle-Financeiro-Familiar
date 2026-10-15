package br.senac.fianceiro.meta.controller;

import br.senac.fianceiro.common.dto.ApiResponse;
import br.senac.fianceiro.meta.dto.AporteResponse;
import br.senac.fianceiro.meta.dto.CriarAporteRequest;
import br.senac.fianceiro.meta.dto.CriarMetaRequest;
import br.senac.fianceiro.meta.dto.MetaResponse;
import br.senac.fianceiro.meta.service.MetaService;
import br.senac.fianceiro.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metas")
public class MetaController {

    private final MetaService metaService;

    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MetaResponse>>> listar(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal) {
        List<MetaResponse> lista = metaService.listar(usuarioPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.sucesso(lista));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MetaResponse>> criar(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody CriarMetaRequest request) {
        MetaResponse response = metaService.criar(usuarioPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Meta financeira cadastrada com sucesso.", response));
    }

    @PostMapping("/{id}/aportes")
    public ResponseEntity<ApiResponse<AporteResponse>> registrarAporte(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody CriarAporteRequest request) {
        AporteResponse response = metaService.registrarAporte(usuarioPrincipal.getId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Aporte registrado com sucesso.", response));
    }

    @GetMapping("/{id}/aportes")
    public ResponseEntity<ApiResponse<List<AporteResponse>>> listarAportes(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal) {
        List<AporteResponse> response = metaService.listarAportes(usuarioPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.sucesso(response));
    }
}
