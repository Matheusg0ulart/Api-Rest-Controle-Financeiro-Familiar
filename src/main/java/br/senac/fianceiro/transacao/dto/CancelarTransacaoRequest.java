package br.senac.fianceiro.transacao.dto;

import jakarta.validation.constraints.Size;

public class CancelarTransacaoRequest {

    @Size(max = 255, message = "O motivo do cancelamento não pode ultrapassar 255 caracteres")
    private String motivo;

    public CancelarTransacaoRequest() {}

    public CancelarTransacaoRequest(String motivo) {
        this.motivo = motivo;
    }

    public static CancelarTransacaoRequestBuilder builder() {
        return new CancelarTransacaoRequestBuilder();
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public static class CancelarTransacaoRequestBuilder {
        private String motivo;

        CancelarTransacaoRequestBuilder() {}

        public CancelarTransacaoRequestBuilder motivo(String motivo) {
            this.motivo = motivo;
            return this;
        }

        public CancelarTransacaoRequest build() {
            return new CancelarTransacaoRequest(motivo);
        }
    }
}
