package br.senac.fianceiro.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private boolean sucesso;
    private int status;
    private String erro;
    private String mensagem;
    private String caminho;
    private List<ValidationErrorItem> errosValidacao;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
