package br.senac.fianceiro.categoria.controller;

import br.senac.fianceiro.categoria.dto.CategoriaResponse;
import br.senac.fianceiro.categoria.dto.CriarCategoriaRequest;
import br.senac.fianceiro.categoria.entity.TipoCategoria;
import br.senac.fianceiro.categoria.service.CategoriaService;
import br.senac.fianceiro.common.dto.ApiResponse;
import br.senac.fianceiro.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listarCategorias(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @RequestParam(name = "tipo", required = false) TipoCategoria tipo) {
        List<CategoriaResponse> categorias = categoriaService.listarCategorias(usuarioPrincipal.getId(), tipo);
        return ResponseEntity.ok(ApiResponse.sucesso(categorias));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaResponse>> criarCategoria(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody CriarCategoriaRequest request) {
        CategoriaResponse categoria = categoriaService.criarCategoria(usuarioPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Categoria criada com sucesso.", categoria));
    }
}
