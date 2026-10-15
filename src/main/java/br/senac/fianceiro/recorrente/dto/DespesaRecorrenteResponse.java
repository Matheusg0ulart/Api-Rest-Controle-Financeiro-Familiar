package br.senac.fianceiro.recorrente.dto;

import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.recorrente.entity.FrequenciaRecorrencia;
import br.senac.fianceiro.recorrente.entity.StatusRecorrencia;
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
public class DespesaRecorrenteResponse {
    private Long id;
    private String descricao;
    private BigDecimal valor;
    private FrequenciaRecorrencia frequencia;
    private Integer diaVencimento;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private StatusRecorrencia status;
    private Long contaId;
    private String contaNome;
    private Long categoriaId;
    private String categoriaNome;
    private Long criadoPorId;
    private String criadoPorNome;
    private EscopoRecurso escopo;
    private LocalDate proximaGeracao;
    private LocalDateTime dataCriacao;
}
