package br.senac.fianceiro.transacao.controller;

import br.senac.fianceiro.common.dto.ApiResponse;
import br.senac.fianceiro.security.UsuarioPrincipal;
import br.senac.fianceiro.transacao.dto.TransacaoResponse;
import br.senac.fianceiro.transacao.dto.TransferenciaRequest;
import br.senac.fianceiro.transacao.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final TransacaoService transacaoService;

    public TransferenciaController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransacaoResponse>> transferir(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody TransferenciaRequest request) {

        TransacaoResponse response = transacaoService.transferir(usuarioPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Transferência realizada com sucesso.", response));
    }
}
