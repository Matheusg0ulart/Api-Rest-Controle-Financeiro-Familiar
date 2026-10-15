package br.senac.fianceiro.familia.entity;

import br.senac.fianceiro.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "familia_membros",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_familia_usuario", columnNames = {"familia_id", "usuario_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamiliaMembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "familia_id", nullable = false)
    private Familia familia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PapelMembro papel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatusMembro status = StatusMembro.ATIVO;

    @Column(name = "data_entrada", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dataEntrada = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (dataEntrada == null) {
            dataEntrada = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusMembro.ATIVO;
        }
    }
}
