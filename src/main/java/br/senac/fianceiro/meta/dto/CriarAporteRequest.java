package br.senac.fianceiro.meta.dto;

import jakarta.validation.constraints.DecimalMin;
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
public class CriarAporteRequest {

    @NotNull(message = "O valor do aporte é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor do aporte deve ser positivo.")
    private BigDecimal valor;

    private LocalDate data;

    @Size(max = 255, message = "A observação não pode exceder 255 caracteres.")
    private String observacao;

    private Long origemContaId;
}
