package br.senac.fianceiro.familia.dto;

import br.senac.fianceiro.familia.entity.PapelMembro;
import br.senac.fianceiro.familia.entity.StatusMembro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamiliaMembroResponse {
    private Long id;
    private Long usuarioId;
    private String nome;
    private String email;
    private PapelMembro papel;
    private StatusMembro status;
    private LocalDateTime dataEntrada;
}
