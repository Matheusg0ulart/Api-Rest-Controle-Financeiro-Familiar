package br.senac.fianceiro.orcamento.dto;

import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarOrcamentoRequest {

    @NotBlank(message = "O nome do orçamento é obrigatório.")
    @Size(max = 100, message = "O nome não pode ter mais de 100 caracteres.")
    private String nome;

    @NotNull(message = "O valor limite é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor limite deve ser maior que zero.")
    private BigDecimal valorLimite;

    @NotBlank(message = "O período é obrigatório (ex: 2026-10).")
    @Size(max = 20, message = "O período não pode exceder 20 caracteres.")
    private String periodo;

    @NotNull(message = "O escopo é obrigatório (PESSOAL ou FAMILIAR).")
    private EscopoRecurso escopo;

    private Long categoriaId;

    private Long familiaId;
}
