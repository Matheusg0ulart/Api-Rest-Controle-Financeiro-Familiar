package br.senac.fianceiro.recorrente.dto;

import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.recorrente.entity.FrequenciaRecorrencia;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarDespesaRecorrenteRequest {

    @NotBlank(message = "A descrição é obrigatória.")
    @Size(min = 2, max = 200, message = "A descrição deve ter entre 2 e 200 caracteres.")
    private String descricao;

    @NotNull(message = "O valor é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
    private BigDecimal valor;

    @NotNull(message = "A frequência é obrigatória.")
    private FrequenciaRecorrencia frequencia;

    @NotNull(message = "O dia de vencimento é obrigatório.")
    @Min(value = 1, message = "O dia de vencimento deve ser entre 1 e 31.")
    @Max(value = 31, message = "O dia de vencimento deve ser entre 1 e 31.")
    private Integer diaVencimento;

    @NotNull(message = "A data de início é obrigatória.")
    private LocalDate dataInicio;

    private LocalDate dataFim;

    @NotNull(message = "O ID da conta é obrigatório.")
    private Long contaId;

    private Long categoriaId;

    @NotNull(message = "O escopo é obrigatório.")
    @Builder.Default
    private EscopoRecurso escopo = EscopoRecurso.PESSOAL;
}
