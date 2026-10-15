package br.senac.fianceiro.recorrente.dto;

import br.senac.fianceiro.recorrente.entity.StatusRecorrencia;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class AtualizarDespesaRecorrenteRequest {

    @Size(min = 2, max = 200, message = "A descrição deve ter entre 2 e 200 caracteres.")
    private String descricao;

    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
    private BigDecimal valor;

    @Min(value = 1, message = "O dia de vencimento deve ser entre 1 e 31.")
    @Max(value = 31, message = "O dia de vencimento deve ser entre 1 e 31.")
    private Integer diaVencimento;

    private LocalDate dataFim;

    private StatusRecorrencia status;

    private Long categoriaId;
}
