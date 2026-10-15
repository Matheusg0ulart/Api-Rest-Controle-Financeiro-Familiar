package br.senac.fianceiro.transacao;

import br.senac.fianceiro.categoria.dto.CriarCategoriaRequest;
import br.senac.fianceiro.categoria.entity.EscopoRecurso;
import br.senac.fianceiro.categoria.entity.TipoCategoria;
import br.senac.fianceiro.conta.dto.ContaResponse;
import br.senac.fianceiro.conta.dto.CriarContaRequest;
import br.senac.fianceiro.conta.entity.TipoConta;
import br.senac.fianceiro.conta.service.ContaService;
import br.senac.fianceiro.familia.dto.CriarFamiliaRequest;
import br.senac.fianceiro.familia.dto.FamiliaResponse;
import br.senac.fianceiro.familia.service.FamiliaService;
import br.senac.fianceiro.transacao.dto.CancelarTransacaoRequest;
import br.senac.fianceiro.transacao.dto.CriarTransacaoRequest;
import br.senac.fianceiro.transacao.dto.TransferenciaRequest;
import br.senac.fianceiro.transacao.entity.TipoTransacao;
import br.senac.fianceiro.usuario.dto.AuthResponse;
import br.senac.fianceiro.usuario.dto.RegistroUsuarioRequest;
import br.senac.fianceiro.usuario.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ContasETransacoesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private FamiliaService familiaService;

    @Autowired
    private ContaService contaService;

    private AuthResponse authUser;
    private FamiliaResponse familia;
    private ContaResponse contaCorrente;
    private ContaResponse contaPoupanca;

    @BeforeEach
    void setUp() {
        // Cadastra usuário
        authUser = authService.registrar(RegistroUsuarioRequest.builder()
                .nome("Fernando Rocha")
                .email("fernando@teste.com")
                .senha("senhaForte123")
                .build());

        // Cria família
        familia = familiaService.criarFamilia(authUser.getUsuario().getId(),
                CriarFamiliaRequest.builder().nome("Família Rocha").build());

        // Cria conta pessoal com saldo inicial de 1000.00
        contaCorrente = contaService.criarConta(authUser.getUsuario().getId(),
                CriarContaRequest.builder()
                        .nome("Conta Corrente Principal")
                        .tipo(TipoConta.CORRENTE)
                        .saldoInicial(new BigDecimal("1000.00"))
                        .build());

        // Cria conta compartilhada com saldo inicial de 500.00
        contaPoupanca = contaService.criarConta(authUser.getUsuario().getId(),
                CriarContaRequest.builder()
                        .nome("Poupança Familiar")
                        .tipo(TipoConta.POUPANCA)
                        .familiaId(familia.getId())
                        .saldoInicial(new BigDecimal("500.00"))
                        .build());
    }

    @Test
    @DisplayName("Deve registrar receita e despesa e atualizar o saldo da conta com precisão")
    void deveRegistrarReceitaEDespesaEAtualizarSaldo() throws Exception {
        // 1. Cria Categoria de Salário
        CriarCategoriaRequest catReceita = CriarCategoriaRequest.builder()
                .nome("Salário")
                .tipo(TipoCategoria.RECEITA)
                .escopo(EscopoRecurso.PESSOAL)
                .build();

        MvcResult resultCat = mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + authUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catReceita)))
                .andExpect(status().isCreated())
                .andReturn();

        Long catReceitaId = objectMapper.readTree(resultCat.getResponse().getContentAsString())
                .get("dados").get("id").asLong();

        // 2. Lança Receita de 250.00
        CriarTransacaoRequest receita = CriarTransacaoRequest.builder()
                .descricao("Adiantamento Salarial")
                .valor(new BigDecimal("250.00"))
                .tipo(TipoTransacao.RECEITA)
                .dataCompetencia(LocalDate.now())
                .contaId(contaCorrente.getId())
                .categoriaId(catReceitaId)
                .escopo(EscopoRecurso.PESSOAL)
                .build();

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + authUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receita)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.valor").value(250.00))
                .andExpect(jsonPath("$.dados.tipo").value("RECEITA"));

        // Verifica saldo atualizado (1000.00 + 250.00 = 1250.00)
        mockMvc.perform(get("/api/contas/" + contaCorrente.getId())
                        .header("Authorization", "Bearer " + authUser.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.saldo").value(1250.00));

        // 3. Lança Despesa de 400.00
        CriarTransacaoRequest despesa = CriarTransacaoRequest.builder()
                .descricao("Compras Supermercado")
                .valor(new BigDecimal("400.00"))
                .tipo(TipoTransacao.DESPESA)
                .dataCompetencia(LocalDate.now())
                .contaId(contaCorrente.getId())
                .escopo(EscopoRecurso.PESSOAL)
                .build();

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + authUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(despesa)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.valor").value(400.00))
                .andExpect(jsonPath("$.dados.tipo").value("DESPESA"));

        // Verifica saldo atualizado (1250.00 - 400.00 = 850.00)
        mockMvc.perform(get("/api/contas/" + contaCorrente.getId())
                        .header("Authorization", "Bearer " + authUser.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.saldo").value(850.00));
    }

    @Test
    @DisplayName("Regra Obrigatória: Deve BLOQUEAR despesa que cause saldo negativo")
    void deveBloquearDespesaQueCauseSaldoNegativo() throws Exception {
        // Tenta lançar despesa de 1200.00 em conta com saldo de 1000.00
        CriarTransacaoRequest despesaExcessiva = CriarTransacaoRequest.builder()
                .descricao("Compra de Computador")
                .valor(new BigDecimal("1200.00"))
                .tipo(TipoTransacao.DESPESA)
                .dataCompetencia(LocalDate.now())
                .contaId(contaCorrente.getId())
                .escopo(EscopoRecurso.PESSOAL)
                .build();

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + authUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(despesaExcessiva)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("Saldo insuficiente")));

        // Saldo deve permanecer intacto em 1000.00
        mockMvc.perform(get("/api/contas/" + contaCorrente.getId())
                        .header("Authorization", "Bearer " + authUser.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.saldo").value(1000.00));
    }

    @Test
    @DisplayName("Deve realizar transferência atômica entre contas e bloquear saldo insuficiente")
    void deveRealizarTransferenciaAtomicaEntreContas() throws Exception {
        // 1. Transfere 300.00 da Conta Corrente (1000.00) para Poupança (500.00)
        TransferenciaRequest transferencia = TransferenciaRequest.builder()
                .descricao("Transferência para Reserva")
                .valor(new BigDecimal("300.00"))
                .dataCompetencia(LocalDate.now())
                .contaOrigemId(contaCorrente.getId())
                .contaDestinoId(contaPoupanca.getId())
                .build();

        mockMvc.perform(post("/api/transferencias")
                        .header("Authorization", "Bearer " + authUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferencia)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.tipo").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$.dados.valor").value(300.00));

        // Saldo Origem: 1000.00 - 300.00 = 700.00
        mockMvc.perform(get("/api/contas/" + contaCorrente.getId())
                        .header("Authorization", "Bearer " + authUser.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.saldo").value(700.00));

        // Saldo Destino: 500.00 + 300.00 = 800.00
        mockMvc.perform(get("/api/contas/" + contaPoupanca.getId())
                        .header("Authorization", "Bearer " + authUser.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.saldo").value(800.00));

        // 2. Tenta transferir 800.00 da Conta Corrente (saldo atual: 700.00) -> Deve Bloquear!
        TransferenciaRequest transfExcessiva = TransferenciaRequest.builder()
                .descricao("Transferência Inválida")
                .valor(new BigDecimal("800.00"))
                .dataCompetencia(LocalDate.now())
                .contaOrigemId(contaCorrente.getId())
                .contaDestinoId(contaPoupanca.getId())
                .build();

        mockMvc.perform(post("/api/transferencias")
                        .header("Authorization", "Bearer " + authUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfExcessiva)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("Saldo insuficiente")));
    }

    @Test
    @DisplayName("Cancelamento Rastreável: Deve reverter o saldo e marcar transação como CANCELADA sem excluir")
    void deveCancelarTransacaoEReverterSaldo() throws Exception {
        // Lança despesa de 200.00 (saldo vai de 1000.00 para 800.00)
        CriarTransacaoRequest despesa = CriarTransacaoRequest.builder()
                .descricao("Aluguel Garagem")
                .valor(new BigDecimal("200.00"))
                .tipo(TipoTransacao.DESPESA)
                .dataCompetencia(LocalDate.now())
                .contaId(contaCorrente.getId())
                .escopo(EscopoRecurso.PESSOAL)
                .build();

        MvcResult resultDespesa = mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + authUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(despesa)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(resultDespesa.getResponse().getContentAsString());
        Long transacaoId = jsonNode.get("dados").get("id").asLong();

        // Cancela a transação via POST /api/transacoes/{id}/cancelamento
        CancelarTransacaoRequest cancelamento = CancelarTransacaoRequest.builder()
                .motivo("Cobrança indevida estornada")
                .build();

        mockMvc.perform(post("/api/transacoes/" + transacaoId + "/cancelamento")
                        .header("Authorization", "Bearer " + authUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelamento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.status").value("CANCELADA"))
                .andExpect(jsonPath("$.dados.motivoCancelamento").value("Cobrança indevida estornada"))
                .andExpect(jsonPath("$.dados.dataCancelamento").isNotEmpty());

        // Saldo deve ser estornado de volta para 1000.00
        mockMvc.perform(get("/api/contas/" + contaCorrente.getId())
                        .header("Authorization", "Bearer " + authUser.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.saldo").value(1000.00));
    }
}
