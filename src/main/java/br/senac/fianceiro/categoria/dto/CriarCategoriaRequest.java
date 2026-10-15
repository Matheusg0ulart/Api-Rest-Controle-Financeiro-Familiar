package br.senac.fianceiro.categoria.dto;

import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.categoria.entity.TipoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarCategoriaRequest {

    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(min = 2, max = 100, message = "O nome da categoria deve ter entre 2 e 100 caracteres")
    private String nome;

    @NotNull(message = "O tipo da categoria é obrigatório (RECEITA ou DESPESA)")
    private TipoCategoria tipo;

    @NotNull(message = "O escopo da categoria é obrigatório (PESSOAL ou FAMILIAR)")
    private EscopoRecurso escopo;

    private Long familiaId;
}
