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
public class EvolucaoMensalResponse {
    private String mesAno; // "2026-05"
    private String rotulo; // "Mai/2026"
    private BigDecimal receitas;
    private BigDecimal despesas;
    private BigDecimal saldo;
}
