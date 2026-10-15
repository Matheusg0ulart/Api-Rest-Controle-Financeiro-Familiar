package br.senac.fianceiro.familia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarFamiliaRequest {

    @NotBlank(message = "O nome da família é obrigatório")
    @Size(min = 2, max = 150, message = "O nome da família deve ter entre 2 e 150 caracteres")
    private String nome;
}
