package br.senac.fianceiro.meta.dto;

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
public class AporteResponse {
    private Long id;
    private Long metaId;
    private BigDecimal valor;
    private LocalDate data;
    private String observacao;
    private Long origemContaId;
    private String origemContaNome;
    private Long usuarioId;
    private String usuarioNome;
    private LocalDateTime criadoEm;
}
