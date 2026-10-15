package br.senac.fianceiro.health;

import br.senac.fianceiro.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Value("${spring.application.name:fianceiro}")
    private String aplicacao;

    @GetMapping
    public ResponseEntity<ApiResponse<HealthResponse>> verificarSaude() {
        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .aplicacao(aplicacao)
                .versao("1.0.0")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(ApiResponse.sucesso(response));
    }
}
