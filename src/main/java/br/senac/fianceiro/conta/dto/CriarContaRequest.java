package br.senac.fianceiro.conta.dto;

import br.senac.fianceiro.conta.entity.TipoConta;
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
public class CriarContaRequest {

    @NotBlank(message = "O nome da conta é obrigatório")
    @Size(min = 2, max = 100, message = "O nome da conta deve ter entre 2 e 100 caracteres")
    private String nome;

    @NotNull(message = "O tipo da conta é obrigatório")
    private TipoConta tipo;

    @Builder.Default
    private String moeda = "BRL";

    @DecimalMin(value = "0.0", message = "O saldo inicial não pode ser negativo")
    @Builder.Default
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    private Long familiaId;
}
