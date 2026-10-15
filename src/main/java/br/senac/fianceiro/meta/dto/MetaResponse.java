package br.senac.fianceiro.meta.dto;

import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.meta.entity.StatusMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaResponse {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal valorAlvo;
    private BigDecimal valorAtual;
    private BigDecimal valorRestante;
    private BigDecimal progressoPorcentagem;
    private LocalDate prazo;
    private StatusMeta status;
    private EscopoRecurso escopo;
    private Long titularId;
    private String titularNome;
    private Long familiaId;
    private String familiaNome;
    private LocalDateTime criadoEm;
}
