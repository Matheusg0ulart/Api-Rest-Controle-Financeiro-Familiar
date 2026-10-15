package br.senac.fianceiro.orcamento.dto;

import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoResponse {
    private Long id;
    private String nome;
    private BigDecimal valorLimite;
    private BigDecimal valorGasto;
    private BigDecimal valorDisponivel;
    private BigDecimal porcentagemConsumida;
    private String periodo;
    private EscopoRecurso escopo;
    private Long categoriaId;
    private String categoriaNome;
    private Long titularId;
    private String titularNome;
    private Long familiaId;
    private String familiaNome;
    private Boolean ativo;
    private LocalDateTime criadoEm;
}
