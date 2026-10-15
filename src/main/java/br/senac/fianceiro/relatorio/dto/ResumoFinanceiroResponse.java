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
public class ResumoFinanceiroResponse {
    private BigDecimal totalReceitas;
    private BigDecimal totalDespesas;
    private BigDecimal saldoLiquido;
    private BigDecimal saldoTotalContas;
    private Long quantidadeTransacoes;
    private Long quantidadeContas;
}
