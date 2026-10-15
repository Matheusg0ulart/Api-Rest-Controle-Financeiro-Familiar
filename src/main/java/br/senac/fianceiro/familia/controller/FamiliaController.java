package br.senac.fianceiro.familia.controller;

import br.senac.fianceiro.common.dto.ApiResponse;
import br.senac.fianceiro.familia.dto.AdicionarMembroRequest;
import br.senac.fianceiro.familia.dto.CriarFamiliaRequest;
import br.senac.fianceiro.familia.dto.FamiliaMembroResponse;
import br.senac.fianceiro.familia.dto.FamiliaResponse;
import br.senac.fianceiro.familia.service.FamiliaService;
import br.senac.fianceiro.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/familias")
public class FamiliaController {

    private final FamiliaService familiaService;

    public FamiliaController(FamiliaService familiaService) {
        this.familiaService = familiaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FamiliaResponse>>> listarFamilias(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal) {
        List<FamiliaResponse> familias = familiaService.listarFamiliasDoUsuario(usuarioPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.sucesso(familias));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FamiliaResponse>> criarFamilia(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody CriarFamiliaRequest request) {
        FamiliaResponse familia = familiaService.criarFamilia(usuarioPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Família criada com sucesso.", familia));
    }

    @GetMapping("/{id}/membros")
    public ResponseEntity<ApiResponse<List<FamiliaMembroResponse>>> listarMembros(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @PathVariable("id") Long familiaId) {
        List<FamiliaMembroResponse> membros = familiaService.listarMembros(usuarioPrincipal.getId(), familiaId);
        return ResponseEntity.ok(ApiResponse.sucesso(membros));
    }

    @PostMapping("/{id}/membros")
    public ResponseEntity<ApiResponse<FamiliaMembroResponse>> adicionarMembro(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @PathVariable("id") Long familiaId,
            @Valid @RequestBody AdicionarMembroRequest request) {
        FamiliaMembroResponse novoMembro = familiaService.adicionarMembro(usuarioPrincipal.getId(), familiaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Integrante adicionado com sucesso.", novoMembro));
    }
}
