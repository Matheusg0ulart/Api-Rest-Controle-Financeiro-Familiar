package br.senac.fianceiro.meta.entity;

import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.familia.entity.Familia;
import br.senac.fianceiro.usuario.entity.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "metas_financeiras")
public class MetaFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAlvo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAtual = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate prazo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMeta status = StatusMeta.EM_ANDAMENTO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EscopoRecurso escopo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "titular_id", nullable = false)
    private Usuario titular;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "familia_id")
    private Familia familia;

    @OneToMany(mappedBy = "meta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AporteMeta> aportes = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public MetaFinanceira() {}

    public MetaFinanceira(Long id, String nome, String descricao, BigDecimal valorAlvo, BigDecimal valorAtual,
                          LocalDate prazo, StatusMeta status, EscopoRecurso escopo, Usuario titular,
                          Familia familia, List<AporteMeta> aportes, LocalDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.valorAlvo = valorAlvo;
        this.valorAtual = valorAtual != null ? valorAtual : BigDecimal.ZERO;
        this.prazo = prazo;
        this.status = status != null ? status : StatusMeta.EM_ANDAMENTO;
        this.escopo = escopo;
        this.titular = titular;
        this.familia = familia;
        this.aportes = aportes != null ? aportes : new ArrayList<>();
        this.criadoEm = criadoEm;
    }

    public static MetaFinanceiraBuilder builder() {
        return new MetaFinanceiraBuilder();
    }

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (valorAtual == null) {
            valorAtual = BigDecimal.ZERO;
        }
        if (status == null) {
            status = StatusMeta.EM_ANDAMENTO;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValorAlvo() { return valorAlvo; }
    public void setValorAlvo(BigDecimal valorAlvo) { this.valorAlvo = valorAlvo; }

    public BigDecimal getValorAtual() { return valorAtual; }
    public void setValorAtual(BigDecimal valorAtual) { this.valorAtual = valorAtual; }

    public LocalDate getPrazo() { return prazo; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }

    public StatusMeta getStatus() { return status; }
    public void setStatus(StatusMeta status) { this.status = status; }

    public EscopoRecurso getEscopo() { return escopo; }
    public void setEscopo(EscopoRecurso escopo) { this.escopo = escopo; }

    public Usuario getTitular() { return titular; }
    public void setTitular(Usuario titular) { this.titular = titular; }

    public Familia getFamilia() { return familia; }
    public void setFamilia(Familia familia) { this.familia = familia; }

    public List<AporteMeta> getAportes() { return aportes; }
    public void setAportes(List<AporteMeta> aportes) { this.aportes = aportes; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public static class MetaFinanceiraBuilder {
        private Long id;
        private String nome;
        private String descricao;
        private BigDecimal valorAlvo;
        private BigDecimal valorAtual = BigDecimal.ZERO;
        private LocalDate prazo;
        private StatusMeta status = StatusMeta.EM_ANDAMENTO;
        private EscopoRecurso escopo;
        private Usuario titular;
        private Familia familia;
        private List<AporteMeta> aportes = new ArrayList<>();
        private LocalDateTime criadoEm;

        MetaFinanceiraBuilder() {}

        public MetaFinanceiraBuilder id(Long id) { this.id = id; return this; }
        public MetaFinanceiraBuilder nome(String nome) { this.nome = nome; return this; }
        public MetaFinanceiraBuilder descricao(String descricao) { this.descricao = descricao; return this; }
        public MetaFinanceiraBuilder valorAlvo(BigDecimal valorAlvo) { this.valorAlvo = valorAlvo; return this; }
        public MetaFinanceiraBuilder valorAtual(BigDecimal valorAtual) { this.valorAtual = valorAtual; return this; }
        public MetaFinanceiraBuilder prazo(LocalDate prazo) { this.prazo = prazo; return this; }
        public MetaFinanceiraBuilder status(StatusMeta status) { this.status = status; return this; }
        public MetaFinanceiraBuilder escopo(EscopoRecurso escopo) { this.escopo = escopo; return this; }
        public MetaFinanceiraBuilder titular(Usuario titular) { this.titular = titular; return this; }
        public MetaFinanceiraBuilder familia(Familia familia) { this.familia = familia; return this; }
        public MetaFinanceiraBuilder aportes(List<AporteMeta> aportes) { this.aportes = aportes; return this; }
        public MetaFinanceiraBuilder criadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; return this; }

        public MetaFinanceira build() {
            return new MetaFinanceira(id, nome, descricao, valorAlvo, valorAtual, prazo, status, escopo, titular, familia, aportes, criadoEm);
        }
    }
}
