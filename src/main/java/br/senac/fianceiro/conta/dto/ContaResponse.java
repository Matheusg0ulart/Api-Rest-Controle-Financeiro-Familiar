package br.senac.fianceiro.conta.dto;

import br.senac.fianceiro.conta.entity.TipoConta;
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
public class ContaResponse {
    private Long id;
    private String nome;
    private TipoConta tipo;
    private String moeda;
    private BigDecimal saldo;
    private Long titularId;
    private String titularNome;
    private Long familiaId;
    private String familiaNome;
    private boolean compartilhada;
    private boolean ativa;
    private LocalDateTime dataCriacao;
}
