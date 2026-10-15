package br.senac.fianceiro.transacao.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class AtualizarTransacaoRequest {

    @Size(min = 2, max = 255, message = "A descrição deve ter entre 2 e 255 caracteres")
    private String descricao;

    private LocalDate dataCompetencia;

    private LocalDate dataPagamento;

    private Long categoriaId;

    public AtualizarTransacaoRequest() {}

    public AtualizarTransacaoRequest(String descricao, LocalDate dataCompetencia, LocalDate dataPagamento, Long categoriaId) {
        this.descricao = descricao;
        this.dataCompetencia = dataCompetencia;
        this.dataPagamento = dataPagamento;
        this.categoriaId = categoriaId;
    }

    public static AtualizarTransacaoRequestBuilder builder() {
        return new AtualizarTransacaoRequestBuilder();
    }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getDataCompetencia() { return dataCompetencia; }
    public void setDataCompetencia(LocalDate dataCompetencia) { this.dataCompetencia = dataCompetencia; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public static class AtualizarTransacaoRequestBuilder {
        private String descricao;
        private LocalDate dataCompetencia;
        private LocalDate dataPagamento;
        private Long categoriaId;

        AtualizarTransacaoRequestBuilder() {}

        public AtualizarTransacaoRequestBuilder descricao(String descricao) { this.descricao = descricao; return this; }
        public AtualizarTransacaoRequestBuilder dataCompetencia(LocalDate dataCompetencia) { this.dataCompetencia = dataCompetencia; return this; }
        public AtualizarTransacaoRequestBuilder dataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; return this; }
        public AtualizarTransacaoRequestBuilder categoriaId(Long categoriaId) { this.categoriaId = categoriaId; return this; }

        public AtualizarTransacaoRequest build() {
            return new AtualizarTransacaoRequest(descricao, dataCompetencia, dataPagamento, categoriaId);
        }
    }
}
