package br.senac.fianceiro.relatorio.controller;

import br.senac.fianceiro.common.dto.ApiResponse;
import br.senac.fianceiro.relatorio.dto.DespesaPorCategoriaResponse;
import br.senac.fianceiro.relatorio.dto.EvolucaoMensalResponse;
import br.senac.fianceiro.relatorio.dto.ResumoFinanceiroResponse;
import br.senac.fianceiro.relatorio.service.RelatorioService;
import br.senac.fianceiro.security.UsuarioPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/resumo")
    public ResponseEntity<ApiResponse<ResumoFinanceiroResponse>> obterResumo(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        ResumoFinanceiroResponse resumo = relatorioService.obterResumo(usuarioPrincipal.getId(), inicio, fim);
        return ResponseEntity.ok(ApiResponse.sucesso(resumo));
    }

    @GetMapping("/despesas-por-categoria")
    public ResponseEntity<ApiResponse<List<DespesaPorCategoriaResponse>>> obterDespesasPorCategoria(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        List<DespesaPorCategoriaResponse> lista = relatorioService.obterDespesasPorCategoria(usuarioPrincipal.getId(), inicio, fim);
        return ResponseEntity.ok(ApiResponse.sucesso(lista));
    }

    @GetMapping("/evolucao-mensal")
    public ResponseEntity<ApiResponse<List<EvolucaoMensalResponse>>> obterEvolucaoMensal(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @RequestParam(required = false, defaultValue = "6") Integer meses) {
        List<EvolucaoMensalResponse> lista = relatorioService.obterEvolucaoMensal(usuarioPrincipal.getId(), meses);
        return ResponseEntity.ok(ApiResponse.sucesso(lista));
    }
}
