package br.senac.fianceiro.meta.entity;

import br.senac.fianceiro.conta.entity.ContaFinanceira;
import br.senac.fianceiro.usuario.entity.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "aportes_meta")
public class AporteMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meta_id", nullable = false)
    private MetaFinanceira meta;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;

    @Column(length = 255)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origem_conta_id")
    private ContaFinanceira origemConta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public AporteMeta() {}

    public AporteMeta(Long id, MetaFinanceira meta, BigDecimal valor, LocalDate data, String observacao,
                      ContaFinanceira origemConta, Usuario usuario, LocalDateTime criadoEm) {
        this.id = id;
        this.meta = meta;
        this.valor = valor;
        this.data = data;
        this.observacao = observacao;
        this.origemConta = origemConta;
        this.usuario = usuario;
        this.criadoEm = criadoEm;
    }

    public static AporteMetaBuilder builder() {
        return new AporteMetaBuilder();
    }

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (data == null) {
            data = LocalDate.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MetaFinanceira getMeta() { return meta; }
    public void setMeta(MetaFinanceira meta) { this.meta = meta; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public ContaFinanceira getOrigemConta() { return origemConta; }
    public void setOrigemConta(ContaFinanceira origemConta) { this.origemConta = origemConta; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public static class AporteMetaBuilder {
        private Long id;
        private MetaFinanceira meta;
        private BigDecimal valor;
        private LocalDate data;
        private String observacao;
        private ContaFinanceira origemConta;
        private Usuario usuario;
        private LocalDateTime criadoEm;

        AporteMetaBuilder() {}

        public AporteMetaBuilder id(Long id) { this.id = id; return this; }
        public AporteMetaBuilder meta(MetaFinanceira meta) { this.meta = meta; return this; }
        public AporteMetaBuilder valor(BigDecimal valor) { this.valor = valor; return this; }
        public AporteMetaBuilder data(LocalDate data) { this.data = data; return this; }
        public AporteMetaBuilder observacao(String observacao) { this.observacao = observacao; return this; }
        public AporteMetaBuilder origemConta(ContaFinanceira origemConta) { this.origemConta = origemConta; return this; }
        public AporteMetaBuilder usuario(Usuario usuario) { this.usuario = usuario; return this; }
        public AporteMetaBuilder criadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; return this; }

        public AporteMeta build() {
            return new AporteMeta(id, meta, valor, data, observacao, origemConta, usuario, criadoEm);
        }
    }
}
