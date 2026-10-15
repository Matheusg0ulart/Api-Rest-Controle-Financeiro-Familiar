package br.senac.fianceiro.familia.dto;

import br.senac.fianceiro.familia.entity.PapelMembro;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdicionarMembroRequest {

    @NotBlank(message = "O e-mail do usuário é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    private String email;

    @NotNull(message = "O papel do membro é obrigatório")
    private PapelMembro papel;
}
