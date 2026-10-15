package br.senac.fianceiro.familia.dto;

import br.senac.fianceiro.familia.entity.PapelMembro;
import br.senac.fianceiro.familia.entity.StatusMembro;

import java.time.LocalDateTime;

public class FamiliaResponse {
    private Long id;
    private String nome;
    private LocalDateTime dataCriacao;
    private PapelMembro meuPapel;
    private StatusMembro meuStatus;

    public FamiliaResponse() {}

    public FamiliaResponse(Long id, String nome, LocalDateTime dataCriacao, PapelMembro meuPapel, StatusMembro meuStatus) {
        this.id = id;
        this.nome = nome;
        this.dataCriacao = dataCriacao;
        this.meuPapel = meuPapel;
        this.meuStatus = meuStatus;
    }

    public static FamiliaResponseBuilder builder() {
        return new FamiliaResponseBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public PapelMembro getMeuPapel() { return meuPapel; }
    public void setMeuPapel(PapelMembro meuPapel) { this.meuPapel = meuPapel; }

    public StatusMembro getMeuStatus() { return meuStatus; }
    public void setMeuStatus(StatusMembro meuStatus) { this.meuStatus = meuStatus; }

    public static class FamiliaResponseBuilder {
        private Long id;
        private String nome;
        private LocalDateTime dataCriacao;
        private PapelMembro meuPapel;
        private StatusMembro meuStatus;

        FamiliaResponseBuilder() {}

        public FamiliaResponseBuilder id(Long id) { this.id = id; return this; }
        public FamiliaResponseBuilder nome(String nome) { this.nome = nome; return this; }
        public FamiliaResponseBuilder dataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; return this; }
        public FamiliaResponseBuilder meuPapel(PapelMembro meuPapel) { this.meuPapel = meuPapel; return this; }
        public FamiliaResponseBuilder meuStatus(StatusMembro meuStatus) { this.meuStatus = meuStatus; return this; }

        public FamiliaResponse build() {
            return new FamiliaResponse(id, nome, dataCriacao, meuPapel, meuStatus);
        }
    }
}
