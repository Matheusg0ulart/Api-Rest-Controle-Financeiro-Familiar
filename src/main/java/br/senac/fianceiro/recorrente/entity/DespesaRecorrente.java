package br.senac.fianceiro.recorrente.entity;

import br.senac.fianceiro.categoria.entity.Categoria;
import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.conta.entity.ContaFinanceira;
import br.senac.fianceiro.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "despesas_recorrentes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DespesaRecorrente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FrequenciaRecorrencia frequencia;

    @Column(name = "dia_vencimento", nullable = false)
    private Integer diaVencimento;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusRecorrencia status = StatusRecorrencia.ATIVA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id", nullable = false)
    private ContaFinanceira conta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id", nullable = false)
    private Usuario criadoPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EscopoRecurso escopo = EscopoRecurso.PESSOAL;

    @Column(name = "proxima_geracao")
    private LocalDate proximaGeracao;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        if (proximaGeracao == null) {
            proximaGeracao = calcularProximaGeracao(dataInicio);
        }
    }

    /**
     * Calcula a próxima data de geração a partir de uma data-base.
     */
    public LocalDate calcularProximaGeracao(LocalDate dataBase) {
        if (dataBase == null) return null;
        return switch (frequencia) {
            case SEMANAL -> dataBase.plusWeeks(1);
            case QUINZENAL -> dataBase.plusWeeks(2);
            case MENSAL -> dataBase.plusMonths(1);
            case BIMESTRAL -> dataBase.plusMonths(2);
            case TRIMESTRAL -> dataBase.plusMonths(3);
            case SEMESTRAL -> dataBase.plusMonths(6);
            case ANUAL -> dataBase.plusYears(1);
        };
    }
}
