package br.senac.fianceiro.transacao.dto;

import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.transacao.entity.TipoTransacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CriarTransacaoRequest {

    @NotBlank(message = "A descrição é obrigatória")
    @Size(min = 2, max = 255, message = "A descrição deve ter entre 2 e 255 caracteres")
    private String descricao;

    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "O tipo da transação é obrigatório (RECEITA ou DESPESA)")
    private TipoTransacao tipo;

    @NotNull(message = "A data de competência é obrigatória")
    private LocalDate dataCompetencia;

    private LocalDate dataPagamento;

    @NotNull(message = "O ID da conta financeira é obrigatório")
    private Long contaId;

    private Long categoriaId;

    @NotNull(message = "O escopo é obrigatório (PESSOAL ou FAMILIAR)")
    private EscopoRecurso escopo;

    public CriarTransacaoRequest() {}

    public CriarTransacaoRequest(String descricao, BigDecimal valor, TipoTransacao tipo, LocalDate dataCompetencia,
                                 LocalDate dataPagamento, Long contaId, Long categoriaId, EscopoRecurso escopo) {
        this.descricao = descricao;
        this.valor = valor;
        this.tipo = tipo;
        this.dataCompetencia = dataCompetencia;
        this.dataPagamento = dataPagamento;
        this.contaId = contaId;
        this.categoriaId = categoriaId;
        this.escopo = escopo;
    }

    public static CriarTransacaoRequestBuilder builder() {
        return new CriarTransacaoRequestBuilder();
    }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public TipoTransacao getTipo() { return tipo; }
    public void setTipo(TipoTransacao tipo) { this.tipo = tipo; }

    public LocalDate getDataCompetencia() { return dataCompetencia; }
    public void setDataCompetencia(LocalDate dataCompetencia) { this.dataCompetencia = dataCompetencia; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public Long getContaId() { return contaId; }
    public void setContaId(Long contaId) { this.contaId = contaId; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public EscopoRecurso getEscopo() { return escopo; }
    public void setEscopo(EscopoRecurso escopo) { this.escopo = escopo; }

    public static class CriarTransacaoRequestBuilder {
        private String descricao;
        private BigDecimal valor;
        private TipoTransacao tipo;
        private LocalDate dataCompetencia;
        private LocalDate dataPagamento;
        private Long contaId;
        private Long categoriaId;
        private EscopoRecurso escopo;

        CriarTransacaoRequestBuilder() {}

        public CriarTransacaoRequestBuilder descricao(String descricao) { this.descricao = descricao; return this; }
        public CriarTransacaoRequestBuilder valor(BigDecimal valor) { this.valor = valor; return this; }
        public CriarTransacaoRequestBuilder tipo(TipoTransacao tipo) { this.tipo = tipo; return this; }
        public CriarTransacaoRequestBuilder dataCompetencia(LocalDate dataCompetencia) { this.dataCompetencia = dataCompetencia; return this; }
        public CriarTransacaoRequestBuilder dataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; return this; }
        public CriarTransacaoRequestBuilder contaId(Long contaId) { this.contaId = contaId; return this; }
        public CriarTransacaoRequestBuilder categoriaId(Long categoriaId) { this.categoriaId = categoriaId; return this; }
        public CriarTransacaoRequestBuilder escopo(EscopoRecurso escopo) { this.escopo = escopo; return this; }

        public CriarTransacaoRequest build() {
            return new CriarTransacaoRequest(descricao, valor, tipo, dataCompetencia, dataPagamento, contaId, categoriaId, escopo);
        }
    }
}
