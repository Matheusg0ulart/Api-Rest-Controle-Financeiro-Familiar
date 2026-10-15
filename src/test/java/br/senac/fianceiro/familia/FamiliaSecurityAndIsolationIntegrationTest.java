package br.senac.fianceiro.familia;

import br.senac.fianceiro.familia.dto.AdicionarMembroRequest;
import br.senac.fianceiro.familia.dto.CriarFamiliaRequest;
import br.senac.fianceiro.familia.entity.PapelMembro;
import br.senac.fianceiro.usuario.dto.AuthResponse;
import br.senac.fianceiro.usuario.dto.RegistroUsuarioRequest;
import br.senac.fianceiro.usuario.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FamiliaSecurityAndIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("Requisições sem token devem retornar 401 Unauthorized")
    void deveRejeitarRequisicaoSemToken() throws Exception {
        mockMvc.perform(get("/api/familias")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Deve garantir isolamento estrito entre famílias e autorização por papel")
    void deveGarantirIsolamentoEAutorizacaoEntreFamilias() throws Exception {
        // 1. Cadastra Usuário A (Admin da Família A)
        AuthResponse authA = authService.registrar(RegistroUsuarioRequest.builder()
                .nome("Admin Silva")
                .email("admin.silva@teste.com")
                .senha("senha123")
                .build());

        // 2. Cadastra Usuário B (Outro usuário)
        AuthResponse authB = authService.registrar(RegistroUsuarioRequest.builder()
                .nome("Bruno Oliveira")
                .email("bruno@teste.com")
                .senha("senha123")
                .build());

        // 3. Cadastra Usuário C (Novo integrante a ser convidado)
        authService.registrar(RegistroUsuarioRequest.builder()
                .nome("Clara Silva")
                .email("clara.silva@teste.com")
                .senha("senha123")
                .build());

        // 4. Usuário A cria a família "Família Silva"
        CriarFamiliaRequest criarFamilia = CriarFamiliaRequest.builder()
                .nome("Família Silva")
                .build();

        MvcResult resultFamilia = mockMvc.perform(post("/api/familias")
                        .header("Authorization", "Bearer " + authA.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarFamilia)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.nome").value("Família Silva"))
                .andExpect(jsonPath("$.dados.meuPapel").value("ADMINISTRADOR"))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(resultFamilia.getResponse().getContentAsString());
        Long familiaId = jsonNode.get("dados").get("id").asLong();

        // 5. Usuário B lista suas famílias -> Não deve ver a Família Silva (Isolamento garantido)
        mockMvc.perform(get("/api/familias")
                        .header("Authorization", "Bearer " + authB.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados", hasSize(0)));

        // 6. Usuário B tenta ver os membros da Família Silva sem pertencer a ela -> 403 Forbidden
        mockMvc.perform(get("/api/familias/" + familiaId + "/membros")
                        .header("Authorization", "Bearer " + authB.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // 7. Usuário A (Admin) adiciona Usuário B como MEMBRO
        AdicionarMembroRequest addB = AdicionarMembroRequest.builder()
                .email("bruno@teste.com")
                .papel(PapelMembro.MEMBRO)
                .build();

        mockMvc.perform(post("/api/familias/" + familiaId + "/membros")
                        .header("Authorization", "Bearer " + authA.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addB)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.papel").value("MEMBRO"))
                .andExpect(jsonPath("$.dados.email").value("bruno@teste.com"));

        // 8. Agora Usuário B vê a Família Silva como MEMBRO
        mockMvc.perform(get("/api/familias")
                        .header("Authorization", "Bearer " + authB.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados", hasSize(1)))
                .andExpect(jsonPath("$.dados[0].meuPapel").value("MEMBRO"));

        // 9. Usuário B (MEMBRO) tenta adicionar Usuário C -> 403 Forbidden (Apenas Administrador pode)
        AdicionarMembroRequest addC = AdicionarMembroRequest.builder()
                .email("clara.silva@teste.com")
                .papel(PapelMembro.MEMBRO)
                .build();

        mockMvc.perform(post("/api/familias/" + familiaId + "/membros")
                        .header("Authorization", "Bearer " + authB.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addC)))
                .andExpect(status().isForbidden());

        // 10. Usuário A (ADMINISTRADOR) adiciona Usuário C -> Sucesso (201 Created)
        mockMvc.perform(post("/api/familias/" + familiaId + "/membros")
                        .header("Authorization", "Bearer " + authA.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addC)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.email").value("clara.silva@teste.com"));
    }
}
