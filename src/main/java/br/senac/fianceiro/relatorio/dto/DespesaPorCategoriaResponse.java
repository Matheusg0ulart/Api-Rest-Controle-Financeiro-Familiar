package br.senac.fianceiro.relatorio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DespesaPorCategoriaResponse {
    private Long categoriaId;
    private String categoriaNome;
    private BigDecimal total;
    private BigDecimal porcentagem;
    private Long quantidade;
}
