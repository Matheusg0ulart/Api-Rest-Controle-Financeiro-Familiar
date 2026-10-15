package br.senac.fianceiro.categoria.dto;

import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.categoria.entity.TipoCategoria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponse {
    private Long id;
    private String nome;
    private TipoCategoria tipo;
    private EscopoRecurso escopo;
    private Long usuarioId;
    private Long familiaId;
    private LocalDateTime dataCriacao;
}
