package br.senac.fianceiro.transacao.dto;

import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.transacao.entity.StatusTransacao;
import br.senac.fianceiro.transacao.entity.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransacaoResponse {
    private Long id;
    private String descricao;
    private BigDecimal valor;
    private TipoTransacao tipo;
    private StatusTransacao status;
    private LocalDate dataCompetencia;
    private LocalDate dataPagamento;
    private Long contaId;
    private String contaNome;
    private Long contaDestinoId;
    private String contaDestinoNome;
    private Long categoriaId;
    private String categoriaNome;
    private Long criadoPorId;
    private String criadoPorNome;
    private EscopoRecurso escopo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataCancelamento;
    private String motivoCancelamento;

    public TransacaoResponse() {}

    public TransacaoResponse(Long id, String descricao, BigDecimal valor, TipoTransacao tipo, StatusTransacao status,
                             LocalDate dataCompetencia, LocalDate dataPagamento, Long contaId, String contaNome,
                             Long contaDestinoId, String contaDestinoNome, Long categoriaId, String categoriaNome,
                             Long criadoPorId, String criadoPorNome, EscopoRecurso escopo, LocalDateTime dataCriacao,
                             LocalDateTime dataCancelamento, String motivoCancelamento) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.tipo = tipo;
        this.status = status;
        this.dataCompetencia = dataCompetencia;
        this.dataPagamento = dataPagamento;
        this.contaId = contaId;
        this.contaNome = contaNome;
        this.contaDestinoId = contaDestinoId;
        this.contaDestinoNome = contaDestinoNome;
        this.categoriaId = categoriaId;
        this.categoriaNome = categoriaNome;
        this.criadoPorId = criadoPorId;
        this.criadoPorNome = criadoPorNome;
        this.escopo = escopo;
        this.dataCriacao = dataCriacao;
        this.dataCancelamento = dataCancelamento;
        this.motivoCancelamento = motivoCancelamento;
    }

    public static TransacaoResponseBuilder builder() {
        return new TransacaoResponseBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public TipoTransacao getTipo() { return tipo; }
    public void setTipo(TipoTransacao tipo) { this.tipo = tipo; }

    public StatusTransacao getStatus() { return status; }
    public void setStatus(StatusTransacao status) { this.status = status; }

    public LocalDate getDataCompetencia() { return dataCompetencia; }
    public void setDataCompetencia(LocalDate dataCompetencia) { this.dataCompetencia = dataCompetencia; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public Long getContaId() { return contaId; }
    public void setContaId(Long contaId) { this.contaId = contaId; }

    public String getContaNome() { return contaNome; }
    public void setContaNome(String contaNome) { this.contaNome = contaNome; }

    public Long getContaDestinoId() { return contaDestinoId; }
    public void setContaDestinoId(Long contaDestinoId) { this.contaDestinoId = contaDestinoId; }

    public String getContaDestinoNome() { return contaDestinoNome; }
    public void setContaDestinoNome(String contaDestinoNome) { this.contaDestinoNome = contaDestinoNome; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoriaNome() { return categoriaNome; }
    public void setCategoriaNome(String categoriaNome) { this.categoriaNome = categoriaNome; }

    public Long getCriadoPorId() { return criadoPorId; }
    public void setCriadoPorId(Long criadoPorId) { this.criadoPorId = criadoPorId; }

    public String getCriadoPorNome() { return criadoPorNome; }
    public void setCriadoPorNome(String criadoPorNome) { this.criadoPorNome = criadoPorNome; }

    public EscopoRecurso getEscopo() { return escopo; }
    public void setEscopo(EscopoRecurso escopo) { this.escopo = escopo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataCancelamento() { return dataCancelamento; }
    public void setDataCancelamento(LocalDateTime dataCancelamento) { this.dataCancelamento = dataCancelamento; }

    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }

    public static class TransacaoResponseBuilder {
        private Long id;
        private String descricao;
        private BigDecimal valor;
        private TipoTransacao tipo;
        private StatusTransacao status;
        private LocalDate dataCompetencia;
        private LocalDate dataPagamento;
        private Long contaId;
        private String contaNome;
        private Long contaDestinoId;
        private String contaDestinoNome;
        private Long categoriaId;
        private String categoriaNome;
        private Long criadoPorId;
        private String criadoPorNome;
        private EscopoRecurso escopo;
        private LocalDateTime dataCriacao;
        private LocalDateTime dataCancelamento;
        private String motivoCancelamento;

        TransacaoResponseBuilder() {}

        public TransacaoResponseBuilder id(Long id) { this.id = id; return this; }
        public TransacaoResponseBuilder descricao(String descricao) { this.descricao = descricao; return this; }
        public TransacaoResponseBuilder valor(BigDecimal valor) { this.valor = valor; return this; }
        public TransacaoResponseBuilder tipo(TipoTransacao tipo) { this.tipo = tipo; return this; }
        public TransacaoResponseBuilder status(StatusTransacao status) { this.status = status; return this; }
        public TransacaoResponseBuilder dataCompetencia(LocalDate dataCompetencia) { this.dataCompetencia = dataCompetencia; return this; }
        public TransacaoResponseBuilder dataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; return this; }
        public TransacaoResponseBuilder contaId(Long contaId) { this.contaId = contaId; return this; }
        public TransacaoResponseBuilder contaNome(String contaNome) { this.contaNome = contaNome; return this; }
        public TransacaoResponseBuilder contaDestinoId(Long contaDestinoId) { this.contaDestinoId = contaDestinoId; return this; }
        public TransacaoResponseBuilder contaDestinoNome(String contaDestinoNome) { this.contaDestinoNome = contaDestinoNome; return this; }
        public TransacaoResponseBuilder categoriaId(Long categoriaId) { this.categoriaId = categoriaId; return this; }
        public TransacaoResponseBuilder categoriaNome(String categoriaNome) { this.categoriaNome = categoriaNome; return this; }
        public TransacaoResponseBuilder criadoPorId(Long criadoPorId) { this.criadoPorId = criadoPorId; return this; }
        public TransacaoResponseBuilder criadoPorNome(String criadoPorNome) { this.criadoPorNome = criadoPorNome; return this; }
        public TransacaoResponseBuilder escopo(EscopoRecurso escopo) { this.escopo = escopo; return this; }
        public TransacaoResponseBuilder dataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; return this; }
        public TransacaoResponseBuilder dataCancelamento(LocalDateTime dataCancelamento) { this.dataCancelamento = dataCancelamento; return this; }
        public TransacaoResponseBuilder motivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; return this; }

        public TransacaoResponse build() {
            return new TransacaoResponse(id, descricao, valor, tipo, status, dataCompetencia, dataPagamento, contaId,
                    contaNome, contaDestinoId, contaDestinoNome, categoriaId, categoriaNome, criadoPorId, criadoPorNome,
                    escopo, dataCriacao, dataCancelamento, motivoCancelamento);
        }
    }
}
