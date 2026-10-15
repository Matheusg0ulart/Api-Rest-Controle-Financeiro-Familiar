package br.senac.fianceiro.transacao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransferenciaRequest {

    @NotBlank(message = "A descrição da transferência é obrigatória")
    @Size(min = 2, max = 255, message = "A descrição deve ter entre 2 e 255 caracteres")
    private String descricao;

    @NotNull(message = "O valor da transferência é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "A data de competência é obrigatória")
    private LocalDate dataCompetencia;

    private LocalDate dataPagamento;

    @NotNull(message = "A conta de origem é obrigatória")
    private Long contaOrigemId;

    @NotNull(message = "A conta de destino é obrigatória")
    private Long contaDestinoId;

    public TransferenciaRequest() {}

    public TransferenciaRequest(String descricao, BigDecimal valor, LocalDate dataCompetencia,
                                LocalDate dataPagamento, Long contaOrigemId, Long contaDestinoId) {
        this.descricao = descricao;
        this.valor = valor;
        this.dataCompetencia = dataCompetencia;
        this.dataPagamento = dataPagamento;
        this.contaOrigemId = contaOrigemId;
        this.contaDestinoId = contaDestinoId;
    }

    public static TransferenciaRequestBuilder builder() {
        return new TransferenciaRequestBuilder();
    }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getDataCompetencia() { return dataCompetencia; }
    public void setDataCompetencia(LocalDate dataCompetencia) { this.dataCompetencia = dataCompetencia; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public Long getContaOrigemId() { return contaOrigemId; }
    public void setContaOrigemId(Long contaOrigemId) { this.contaOrigemId = contaOrigemId; }

    public Long getContaDestinoId() { return contaDestinoId; }
    public void setContaDestinoId(Long contaDestinoId) { this.contaDestinoId = contaDestinoId; }

    public static class TransferenciaRequestBuilder {
        private String descricao;
        private BigDecimal valor;
        private LocalDate dataCompetencia;
        private LocalDate dataPagamento;
        private Long contaOrigemId;
        private Long contaDestinoId;

        TransferenciaRequestBuilder() {}

        public TransferenciaRequestBuilder descricao(String descricao) { this.descricao = descricao; return this; }
        public TransferenciaRequestBuilder valor(BigDecimal valor) { this.valor = valor; return this; }
        public TransferenciaRequestBuilder dataCompetencia(LocalDate dataCompetencia) { this.dataCompetencia = dataCompetencia; return this; }
        public TransferenciaRequestBuilder dataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; return this; }
        public TransferenciaRequestBuilder contaOrigemId(Long contaOrigemId) { this.contaOrigemId = contaOrigemId; return this; }
        public TransferenciaRequestBuilder contaDestinoId(Long contaDestinoId) { this.contaDestinoId = contaDestinoId; return this; }

        public TransferenciaRequest build() {
            return new TransferenciaRequest(descricao, valor, dataCompetencia, dataPagamento, contaOrigemId, contaDestinoId);
        }
    }
}
