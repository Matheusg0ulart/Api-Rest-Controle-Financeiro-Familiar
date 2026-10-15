package br.senac.fianceiro.meta.dto;

import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CriarMetaRequest {

    @NotBlank(message = "O nome da meta é obrigatório.")
    @Size(max = 100, message = "O nome não pode exceder 100 caracteres.")
    private String nome;

    @Size(max = 255, message = "A descrição não pode exceder 255 caracteres.")
    private String descricao;

    @NotNull(message = "O valor alvo é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor alvo deve ser positivo.")
    private BigDecimal valorAlvo;

    @NotNull(message = "O prazo da meta é obrigatório.")
    @FutureOrPresent(message = "O prazo deve ser uma data presente ou futura.")
    private LocalDate prazo;

    @NotNull(message = "O escopo é obrigatório (PESSOAL ou FAMILIAR).")
    private EscopoRecurso escopo;

    private Long familiaId;
}
